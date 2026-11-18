package cn.meilinkj.qqbot.core.http;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.exception.ApiException;
import cn.meilinkj.qqbot.core.exception.QQBotException;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

/**
 * OpenAPI HTTP 客户端：统一鉴权头、同步/异步调用、指数退避重试、客户端限流、平台错误码解析。
 *
 * <p>错误结构与 HTTP 状态码约定对齐
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html">错误与调试</a>：
 * 200/204 成功；201/202 异步成功但返回 error body（如 304023/304024 等待审核）；
 * 401 认证失败（自动刷新 token 重试）；429 限频；500/504 处理失败。</p>
 */
public class HttpApiClient {

    private static final Logger log = LoggerFactory.getLogger(HttpApiClient.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final QQBotConfig config;
    private final AccessTokenManager tokenManager;
    private final OkHttpClient httpClient;
    private final RateLimiter rateLimiter;
    private final ExecutorService asyncExecutor;

    public HttpApiClient(QQBotConfig config, AccessTokenManager tokenManager, OkHttpClient httpClient) {
        this.config = config;
        this.tokenManager = tokenManager;
        this.httpClient = httpClient;
        this.rateLimiter = new RateLimiter(config.getRequestsPerSecond());
        this.asyncExecutor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "qqbot-api-async");
            t.setDaemon(true);
            return t;
        });
    }

    public <T> T get(String path, Class<T> responseType) {
        return execute("GET", path, null, responseType);
    }

    public <T> T post(String path, Object body, Class<T> responseType) {
        return execute("POST", path, body, responseType);
    }

    public <T> T put(String path, Object body, Class<T> responseType) {
        return execute("PUT", path, body, responseType);
    }

    /** DELETE 等无响应体接口（成功 200/204）。 */
    public void delete(String path) {
        execute("DELETE", path, null, Void.class);
    }

    public void put(String path, Object body) {
        execute("PUT", path, body, Void.class);
    }

    public <T> CompletableFuture<T> getAsync(String path, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> get(path, responseType), asyncExecutor);
    }

    public <T> CompletableFuture<T> postAsync(String path, Object body, Class<T> responseType) {
        return CompletableFuture.supplyAsync(() -> post(path, body, responseType), asyncExecutor);
    }

    public CompletableFuture<Void> deleteAsync(String path) {
        return CompletableFuture.runAsync(() -> delete(path), asyncExecutor);
    }

    public CompletableFuture<Void> putAsync(String path, Object body) {
        return CompletableFuture.runAsync(() -> put(path, body), asyncExecutor);
    }

    /** 返回原始 JSON（用于需要读取 data 等扩展字段的场景）。 */
    public JsonNode executeRaw(String method, String path, Object body) {
        return execute(method, path, body, JsonNode.class);
    }

    /**
     * 向预签名 URL 直传分片数据（HTTP PUT，不带鉴权头），用于富媒体分片上传。
     * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/rich-media.html#分片上传-推荐">分片上传流程</a>
     */
    public void putBytes(String presignedUrl, byte[] data) {
        Request request = new Request.Builder()
                .url(presignedUrl)
                .put(RequestBody.create(data, MediaType.get("application/octet-stream")))
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String respBody = response.body() == null ? "" : response.body().string();
                throw new QQBotException("分片上传失败 http=" + response.code() + " body=" + respBody);
            }
        } catch (IOException e) {
            throw new QQBotException("分片上传网络异常", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T execute(String method, String path, Object body, Class<T> responseType) {
        String url = path.startsWith("http") ? path : config.getBaseUrl() + path;
        String jsonBody = body == null ? null : JsonUtil.toJson(body);
        IOException lastIoError = null;

        for (int attempt = 0; attempt <= config.getMaxRetries(); attempt++) {
            if (attempt > 0) {
                sleepBackoff(attempt);
            }
            rateLimiter.acquire();
            Request.Builder builder = new Request.Builder()
                    .url(url)
                    .header("Authorization", tokenManager.getAuthorization())
                    .header("Content-Type", "application/json");
            RequestBody requestBody = jsonBody == null
                    ? (method.equals("GET") || method.equals("DELETE") ? null : RequestBody.create(new byte[0], JSON))
                    : RequestBody.create(jsonBody, JSON);
            builder.method(method, requestBody);

            try (Response response = httpClient.newCall(builder.build()).execute()) {
                int status = response.code();
                String respBody = response.body() == null ? "" : response.body().string();
                String traceId = response.header("X-Tps-trace-ID");

                if (status == 401) {
                    log.warn("access_token 认证失败(401)，刷新后重试 path={}", path);
                    tokenManager.invalidate();
                    continue;
                }
                if (status == 429 || status >= 500) {
                    log.warn("请求失败 http={} path={}，准备重试 {}/{}", status, path, attempt + 1, config.getMaxRetries());
                    if (attempt == config.getMaxRetries()) {
                        throw toApiException(status, respBody, traceId);
                    }
                    continue;
                }
                if (status == 200 || status == 204) {
                    if (responseType == Void.class || respBody.isEmpty()) {
                        return null;
                    }
                    if (responseType == JsonNode.class) {
                        return (T) JsonUtil.tree(respBody);
                    }
                    return JsonUtil.fromJson(respBody, responseType);
                }
                // 201/202：异步成功但携带 error body（如 304023/304024 等待审核）
                throw toApiException(status, respBody, traceId);
            } catch (IOException e) {
                lastIoError = e;
                log.warn("请求网络异常 path={} attempt={}: {}", path, attempt, e.getMessage());
            }
        }
        throw new QQBotException("请求重试耗尽: " + method + " " + path, lastIoError);
    }

    private ApiException toApiException(int status, String respBody, String traceIdHeader) {
        long errCode = 0;
        String message = respBody;
        String traceId = traceIdHeader;
        try {
            JsonNode node = JsonUtil.tree(respBody);
            JsonNode codeNode = node.has("err_code") ? node.get("err_code") : node.get("code");
            if (codeNode != null) {
                errCode = codeNode.asLong();
            }
            if (node.has("message")) {
                message = node.get("message").asText();
            }
            if (traceId == null && node.has("trace_id")) {
                traceId = node.get("trace_id").asText();
            }
        } catch (QQBotException ignored) {
            // 非 JSON 错误体，保留原文
        }
        return new ApiException(status, errCode, message, traceId, respBody);
    }

    private void sleepBackoff(int attempt) {
        long delay = Math.min(config.getRetryMaxDelayMs(),
                config.getRetryBaseDelayMs() * (1L << (attempt - 1)));
        delay += ThreadLocalRandom.current().nextLong(delay / 2 + 1);
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new QQBotException("重试等待被中断", e);
        }
    }

    public void shutdown() {
        asyncExecutor.shutdown();
    }
}