package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.Menu;
import cn.meilinkj.qqbot.api.request.MenuItem;
import cn.meilinkj.qqbot.api.request.SubMenuItem;
import cn.meilinkj.qqbot.api.response.MenuResponse;
import cn.meilinkj.qqbot.api.response.VersionResponse;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import com.fasterxml.jackson.databind.JsonNode;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 自定义菜单接口测试，对齐官方 GET /v2/menu 与 PUT /v2/menu 端点。
 */
class MenuApiTest {

    private MockWebServer server;
    private HttpApiClient http;
    private MenuApi api;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        QQBotConfig config = QQBotConfig.builder("app-id", "app-secret")
                .baseUrl(server.url("/").toString().replaceAll("/$", ""))
                .requestsPerSecond(1000)
                .build();
        OkHttpClient client = new OkHttpClient();
        http = new HttpApiClient(config, new AccessTokenManager(config, client), client);
        api = new MenuApi(http);
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"T1\",\"expires_in\":\"7200\"}"));
    }

    @AfterEach
    void tearDown() throws IOException {
        http.shutdown();
        server.shutdown();
    }

    private void enqueueOk(String body) {
        server.enqueue(new MockResponse().setBody(body));
    }

    @Test
    void getMenuParsesOfficialSample() throws Exception {
        enqueueOk("{\"menu\":{\"items\":[{\"type\":\"send_message\",\"name\":\"帮助\",\"send_message\":\"/help\"}]},"
                + "\"version\":1}");
        MenuResponse response = api.getMenu();
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v2/menu", request.getPath());
        assertEquals(1, response.getVersion().intValue());
        assertEquals(1, response.getMenu().getItems().size());
        assertEquals("帮助", response.getMenu().getItems().get(0).getName());
        assertEquals("/help", response.getMenu().getItems().get(0).getSendMessage());

        enqueueOk("{\"version\":0}");
        assertNull(api.getMenu().getMenu());
        server.takeRequest();
    }

    @Test
    void setMenuSendsFullOverrideBody() throws Exception {
        enqueueOk("{\"version\":2}");
        VersionResponse result = api.setMenu(Menu.of(
                MenuItem.sendMessage("帮助", "/help"),
                MenuItem.link("官网", "https://example.com"),
                MenuItem.submenu("更多", SubMenuItem.sendMessage("设置", "/settings")),
                MenuItem.toggle("深度搜索", "search", true)));
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/v2/menu", request.getPath());
        assertEquals(2, result.getVersion().intValue());

        JsonNode items = JsonUtil.tree(request.getBody().readUtf8()).path("menu").path("items");
        assertEquals(4, items.size());
        assertEquals("send_message", items.get(0).get("type").asText());
        assertEquals("link", items.get(1).get("type").asText());
        assertEquals("https://example.com", items.get(1).get("link").asText());
        assertEquals("menu", items.get(2).get("type").asText());
        assertEquals("设置", items.get(2).path("sub_menu_items").get(0).get("name").asText());
        assertEquals("switch", items.get(3).get("type").asText());
        assertEquals("search", items.get(3).path("switch").get("switch_id").asText());
        assertTrue(items.get(3).path("switch").get("default").asBoolean());
        assertEquals(false, items.get(0).has("sub_menu_items"));
    }

    @Test
    void officialLimitsAndUrlRuleRejectedLocally() {
        assertThrows(IllegalArgumentException.class, () -> MenuItem.link("官网", "http://example.com"));
        assertThrows(IllegalArgumentException.class, () -> SubMenuItem.link("官网", "example.com"));
        assertThrows(IllegalArgumentException.class, () -> MenuItem.submenu("更多", List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                MenuItem.submenu("更多", List.of(
                        SubMenuItem.sendMessage("a", "/a"), SubMenuItem.sendMessage("b", "/b"),
                        SubMenuItem.sendMessage("c", "/c"), SubMenuItem.sendMessage("d", "/d"),
                        SubMenuItem.sendMessage("e", "/e"), SubMenuItem.sendMessage("f", "/f"))));
        MenuItem[] eleven = new MenuItem[11];
        for (int i = 0; i < 11; i++) {
            eleven[i] = MenuItem.sendMessage("按钮" + i, "/" + i);
        }
        assertThrows(IllegalArgumentException.class, () -> api.setMenu(Menu.of(eleven)));
        assertThrows(NullPointerException.class, () -> api.setMenu(null));
    }
}
