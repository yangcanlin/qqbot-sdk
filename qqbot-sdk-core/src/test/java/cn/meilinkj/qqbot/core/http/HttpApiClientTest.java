package cn.meilinkj.qqbot.core.http;

import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.exception.ApiException;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * HTTP 客户端测试：鉴权头、错误码解析、429 重试、401 自动刷新、201 等待审核。
 * 错误结构见 https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html
 */
class HttpApiClientTest {

    private MockWebServer server;
    private HttpApiClient http;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        QQBotConfig config = QQBotConfig.builder("app-id", "app-secret")
                .baseUrl(server.url("/").toString().replaceAll("/$", ""))
                .maxRetries(2)
                .retryBaseDelayMs(10)
                .retryMaxDelayMs(20)
                .requestsPerSecond(1000)
                .build();
        OkHttpClient client = new OkHttpClient();
        AccessTokenManager tokenManager = new AccessTokenManager(config, client);
        http = new HttpApiClient(config, tokenManager, client);
    }

    @AfterEach
    void tearDown() throws IOException {
        http.shutdown();
        server.shutdown();
    }

    private void enqueueToken(String token) {
        server.enqueue(new MockResponse().setBody(
                "{\"access_token\":\"" + token + "\",\"expires_in\":\"7200\"}"));
    }

    @Test
    void successWithAuthorizationHeader() throws Exception {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setBody("{\"id\":\"MSG1\",\"timestamp\":\"2024-02-20T14:33:47+08:00\"}"));

        var node = http.post("/v2/groups/G1/messages", Map.of("msg_type", 0, "content", "hi"),
                com.fasterxml.jackson.databind.JsonNode.class);
        assertEquals("MSG1", node.get("id").asText());

        server.takeRequest(); // token
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v2/groups/G1/messages", request.getPath());
        assertEquals("QQBot T1", request.getHeader("Authorization"));
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals(0, body.get("msg_type").asInt());
        assertEquals("hi", body.get("content").asText());
    }

    @Test
    void parsesPlatformErrorBody() {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(400)
                .setHeader("X-Tps-trace-ID", "TRACE-1")
                .setBody("{\"err_code\":630001,\"message\":\"param invalid\"}"));

        ApiException ex = assertThrows(ApiException.class,
                () -> http.post("/v2/groups/G1/messages", Map.of(), Void.class));
        assertEquals(400, ex.getHttpStatus());
        assertEquals(630001, ex.getErrCode());
        assertTrue(ex.getMessage().contains("param invalid"));
        assertEquals("TRACE-1", ex.getTraceId());
    }

    @Test
    void pendingAuditOn201() {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(201)
                .setBody("{\"err_code\":304023,\"message\":\"push message waiting audit\"}"));

        ApiException ex = assertThrows(ApiException.class,
                () -> http.post("/v2/groups/G1/messages", Map.of(), Void.class));
        assertTrue(ex.isPendingAudit());
    }

    @Test
    void retriesOn429ThenSucceeds() {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{\"code\":429,\"message\":\"rate limit\"}"));
        server.enqueue(new MockResponse().setBody("{\"ok\":true}"));

        var node = http.executeRaw("GET", "/gateway", null);
        assertTrue(node.get("ok").asBoolean());
        assertEquals(3, server.getRequestCount());
    }

    @Test
    void refreshesTokenOn401() throws Exception {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(401).setBody("{\"code\":401,\"message\":\"auth failed\"}"));
        enqueueToken("T2");
        server.enqueue(new MockResponse().setBody("{\"ok\":true}"));

        var node = http.executeRaw("GET", "/gateway", null);
        assertTrue(node.get("ok").asBoolean());

        server.takeRequest(); // token T1
        server.takeRequest(); // 401
        server.takeRequest(); // token T2
        RecordedRequest retried = server.takeRequest();
        assertEquals("QQBot T2", retried.getHeader("Authorization"));
    }

    @Test
    void rateLimitedFlag() {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{\"code\":429,\"message\":\"rate limit\"}"));
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{\"code\":429,\"message\":\"rate limit\"}"));
        server.enqueue(new MockResponse().setResponseCode(429).setBody("{\"code\":429,\"message\":\"rate limit\"}"));

        ApiException ex = assertThrows(ApiException.class, () -> http.executeRaw("GET", "/gateway", null));
        assertTrue(ex.isRateLimited());
    }

    @Test
    void deleteWithoutBody() throws Exception {
        enqueueToken("T1");
        server.enqueue(new MockResponse().setResponseCode(200));
        http.delete("/v2/groups/G1/messages/M1");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/v2/groups/G1/messages/M1", request.getPath());
    }
}