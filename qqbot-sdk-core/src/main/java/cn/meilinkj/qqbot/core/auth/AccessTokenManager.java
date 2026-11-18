package cn.meilinkj.qqbot.core.auth;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.QQBotConfig;
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
import java.util.function.LongSupplier;

/**
 * AccessToken 凭证管理器，对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/api-use.html#获取调用凭证">获取调用凭证</a>
 *
 * <ul>
 *   <li>POST https://api.bot.qq.com/app/getAppAccessToken，入参 appId/clientSecret</li>
 *   <li>返回 access_token / expires_in（秒，7200 之内）</li>
 *   <li>token 生命周期内重复请求不会刷新；临近过期 60 秒内请求会获得新 token，
 *       老 token 在这 60 秒内仍有效。本管理器在剩余有效期不足 60 秒时自动刷新。</li>
 * </ul>
 */
public class AccessTokenManager {

    private static final Logger log = LoggerFactory.getLogger(AccessTokenManager.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    /** 官方：上一个 token 接近过期 60 秒内获取会得到新 token。 */
    private static final long REFRESH_WINDOW_MS = 60_000;

    private final QQBotConfig config;
    private final OkHttpClient httpClient;
    private final LongSupplier clock;

    private volatile String accessToken;
    private volatile long expiresAtMs;

    public AccessTokenManager(QQBotConfig config, OkHttpClient httpClient) {
        this(config, httpClient, System::currentTimeMillis);
    }

    AccessTokenManager(QQBotConfig config, OkHttpClient httpClient, LongSupplier clock) {
        this.config = config;
        this.httpClient = httpClient;
        this.clock = clock;
    }

    /** 获取有效 token，必要时自动刷新。 */
    public String getToken() {
        String token = accessToken;
        if (token != null && clock.getAsLong() < expiresAtMs - REFRESH_WINDOW_MS) {
            return token;
        }
        synchronized (this) {
            if (accessToken != null && clock.getAsLong() < expiresAtMs - REFRESH_WINDOW_MS) {
                return accessToken;
            }
            return refresh();
        }
    }

    /** 强制刷新 token（如接口返回 401 时）。 */
    public synchronized String refresh() {
        String body = JsonUtil.toJson(new TokenRequest(config.getAppId(), config.getAppSecret()));
        Request request = new Request.Builder()
                .url(config.getBaseUrl() + "/app/getAppAccessToken")
                .post(RequestBody.create(body, JSON))
                .build();
        try (Response response = httpClient.newCall(request).execute()) {
            String respBody = response.body() == null ? "" : response.body().string();
            if (!response.isSuccessful()) {
                throw new QQBotException("获取 access_token 失败 http=" + response.code() + " body=" + respBody);
            }
            JsonNode node = JsonUtil.tree(respBody);
            JsonNode tokenNode = node.get("access_token");
            JsonNode expiresNode = node.get("expires_in");
            if (tokenNode == null || tokenNode.asText().isEmpty()) {
                throw new QQBotException("获取 access_token 失败，响应缺少 access_token: " + respBody);
            }
            // 官方示例中 expires_in 可能为字符串（"7200"）或数字
            long expiresIn = expiresNode == null ? 7200 : Long.parseLong(expiresNode.asText());
            this.accessToken = tokenNode.asText();
            this.expiresAtMs = clock.getAsLong() + expiresIn * 1000;
            log.debug("access_token 已刷新，expires_in={}s", expiresIn);
            return this.accessToken;
        } catch (IOException e) {
            throw new QQBotException("获取 access_token 网络异常", e);
        }
    }

    /** 使当前缓存失效（下次 getToken 触发刷新）。 */
    public void invalidate() {
        synchronized (this) {
            this.expiresAtMs = 0;
        }
    }

    /** Authorization 头值："QQBot {ACCESS_TOKEN}"。 */
    public String getAuthorization() {
        return "QQBot " + getToken();
    }

    private record TokenRequest(String appId, String clientSecret) {
    }
}