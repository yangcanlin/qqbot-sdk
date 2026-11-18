package cn.meilinkj.qqbot.core.auth;

import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * AccessToken 管理器测试：缓存、60 秒刷新窗口、expires_in 字符串兼容、失效重取。
 * 协议见 https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/api-use.html
 */
class AccessTokenManagerTest {

    private MockWebServer server;
    private OkHttpClient httpClient;
    private AtomicLong clock;
    private AccessTokenManager manager;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        httpClient = new OkHttpClient();
        clock = new AtomicLong(1_000_000L);
        QQBotConfig config = QQBotConfig.builder("app-id", "app-secret")
                .baseUrl(server.url("/").toString().replaceAll("/$", ""))
                .build();
        manager = new AccessTokenManager(config, httpClient, clock::get);
    }

    @AfterEach
    void tearDown() throws IOException {
        server.shutdown();
    }

    private void enqueueToken(String token, String expiresIn) {
        server.enqueue(new MockResponse().setBody(
                "{\"access_token\":\"" + token + "\",\"expires_in\":\"" + expiresIn + "\"}"));
    }

    @Test
    void fetchesTokenWithOfficialRequestBody() throws Exception {
        enqueueToken("T1", "7200");
        assertEquals("T1", manager.getToken());

        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/app/getAppAccessToken", request.getPath());
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals("app-id", body.get("appId").asText());
        assertEquals("app-secret", body.get("clientSecret").asText());
    }

    @Test
    void cachesTokenWithinLifetime() {
        enqueueToken("T1", "7200");
        assertEquals("T1", manager.getToken());
        clock.addAndGet(3600_000L);
        assertEquals("T1", manager.getToken());
        assertEquals(1, server.getRequestCount());
    }

    @Test
    void refreshesWithinLast60Seconds() {
        enqueueToken("T1", "7200");
        enqueueToken("T2", "7200");
        assertEquals("T1", manager.getToken());
        // 距过期不足 60 秒 → 触发刷新
        clock.addAndGet(7200_000L - 59_000L);
        assertEquals("T2", manager.getToken());
        assertEquals(2, server.getRequestCount());
    }

    @Test
    void parsesNumericExpiresIn() {
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"T1\",\"expires_in\":100}"));
        assertEquals("T1", manager.getToken());
        clock.addAndGet(41_000L);
        // 100s 有效期，41s 后剩余 59s < 60s 窗口 → 需要刷新
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"T2\",\"expires_in\":100}"));
        assertEquals("T2", manager.getToken());
    }

    @Test
    void invalidateForcesRefresh() {
        enqueueToken("T1", "7200");
        enqueueToken("T2", "7200");
        assertEquals("T1", manager.getToken());
        manager.invalidate();
        assertEquals("T2", manager.getToken());
        assertNotEquals("T1", manager.getToken());
    }

    @Test
    void authorizationHeaderFormat() {
        enqueueToken("T1", "7200");
        assertEquals("QQBot T1", manager.getAuthorization());
    }
}