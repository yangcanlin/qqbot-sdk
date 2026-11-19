package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.Panel;
import cn.meilinkj.qqbot.api.request.PanelCreateRequest;
import cn.meilinkj.qqbot.api.request.PanelItem;
import cn.meilinkj.qqbot.api.request.PanelTargetRequest;
import cn.meilinkj.qqbot.api.response.Page;
import cn.meilinkj.qqbot.api.response.PanelRecord;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 指令面板接口路径、请求体与分页解析测试，对齐官方 /v2/panels 系列 6 个端点。
 */
class PanelApiTest {

    private MockWebServer server;
    private HttpApiClient http;
    private PanelApi api;

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
        api = new PanelApi(http);
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
    void listPanelsQueryAndCursorPage() throws Exception {
        enqueueOk("{\"records\":[{\"panel_id\":\"p_1\",\"scope\":\"c2c\",\"target_type\":\"all\","
                + "\"panel\":{\"items\":[{\"type\":\"command\",\"name\":\"查询天气\",\"desc\":\"查询当前天气\"}]},"
                + "\"version\":1}],\"next_cursor\":\"CURSOR2\",\"is_end\":false}");
        Page<PanelRecord> page = api.getPanels("c2c", null, 10);
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v2/panels?scope=c2c&limit=10", request.getPath());
        assertEquals(1, page.getItems().size());
        assertTrue(page.hasNext());
        assertEquals("CURSOR2", page.getNextCursor());
        PanelRecord record = page.getItems().get(0);
        assertEquals("p_1", record.getPanelId());
        assertEquals("c2c", record.getScope());
        assertEquals(1, record.getVersion().intValue());
        assertEquals("查询天气", record.getPanel().getItems().get(0).getName());

        enqueueOk("{\"records\":[],\"next_cursor\":\"\",\"is_end\":true}");
        api.getPanels("c2c", "CURSOR2", null);
        assertEquals("/v2/panels?scope=c2c&cursor=CURSOR2", server.takeRequest().getPath());

        enqueueOk("{\"records\":[],\"next_cursor\":\"\",\"is_end\":true}");
        Page<PanelRecord> lastPage = api.getPanels("c2c");
        assertEquals("/v2/panels?scope=c2c", server.takeRequest().getPath());
        assertFalse(lastPage.hasNext());

        assertThrows(NullPointerException.class, () -> api.getPanels(null));
        assertThrows(IllegalArgumentException.class, () -> api.getPanels("c2c", null, 51));
    }

    @Test
    void createPanelBodyMatchesOfficialSamples() throws Exception {
        enqueueOk("{\"panel_id\":\"p_x8k2\"}");
        String panelId = api.createPanel(PanelCreateRequest.forGroups(
                Panel.of(PanelItem.command("群签到", "每日签到")).setRemark("群面板"),
                List.of("openid_group_001", "openid_group_002")));
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v2/panels", request.getPath());
        assertEquals("p_x8k2", panelId);
        JsonNode body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals("group", body.get("scope").asText());
        assertEquals("specific", body.get("target_type").asText());
        assertEquals(2, body.path("group_openids").size());
        assertEquals("群面板", body.path("panel").get("remark").asText());
        assertEquals("command", body.path("panel").path("items").get(0).get("type").asText());
        assertEquals(false, body.has("user_openids"));

        enqueueOk("{\"panel_id\":\"p_all\"}");
        api.createPanel(PanelCreateRequest.global(PanelCreateRequest.SCOPE_CHANNEL,
                Panel.of(PanelItem.link("更多服务", "https://example.com"))));
        JsonNode globalBody = JsonUtil.tree(server.takeRequest().getBody().readUtf8());
        assertEquals("channel", globalBody.get("scope").asText());
        assertEquals("all", globalBody.get("target_type").asText());
        assertEquals("https://example.com", globalBody.path("panel").path("items").get(0).get("link").asText());

        assertThrows(IllegalArgumentException.class, () ->
                PanelCreateRequest.forUsers(Panel.of(PanelItem.command("指令", "说明")), List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                PanelCreateRequest.forUsers(Panel.of(PanelItem.command("指令", "说明")), java.util.Collections.nCopies(21, "u")));
        assertThrows(IllegalArgumentException.class, () ->
                new PanelCreateRequest().setScope(PanelCreateRequest.SCOPE_DM).setTargetType(PanelCreateRequest.TARGET_SPECIFIC));
    }

    @Test
    void detailUpdateDeleteAndTargetPaths() throws Exception {
        enqueueOk("{\"panel_id\":\"p_1\",\"scope\":\"group\",\"target_type\":\"specific\","
                + "\"panel\":{\"items\":[{\"type\":\"command\",\"name\":\"群签到\",\"desc\":\"每日签到\",\"only_admin\":true}]},"
                + "\"created_at\":\"2024-01-15T10:30:00Z\",\"updated_at\":\"2024-01-16T10:30:00Z\",\"version\":3,"
                + "\"user_openids\":[],\"group_openids\":[\"openid_group_001\"]}");
        PanelRecord detail = api.getPanel("p_1");
        server.takeRequest();
        RecordedRequest getRequest = server.takeRequest();
        assertEquals("GET", getRequest.getMethod());
        assertEquals("/v2/panels/p_1", getRequest.getPath());
        assertEquals(3, detail.getVersion().intValue());
        assertEquals("2024-01-15T10:30:00Z", detail.getCreatedAt());
        assertEquals(List.of("openid_group_001"), detail.getGroupOpenids());
        assertTrue(detail.getPanel().getItems().get(0).getOnlyAdmin());

        enqueueOk("{\"version\":4}");
        VersionResponse updated = api.updatePanel("p_1", Panel.of(PanelItem.command("新指令", "更新后的指令")));
        RecordedRequest putRequest = server.takeRequest();
        assertEquals("PUT", putRequest.getMethod());
        assertEquals("/v2/panels/p_1", putRequest.getPath());
        assertEquals("新指令", JsonUtil.tree(putRequest.getBody().readUtf8()).path("panel").path("items").get(0).get("name").asText());
        assertEquals(4, updated.getVersion().intValue());

        enqueueOk("{}");
        api.deletePanel("p_1");
        RecordedRequest deleteRequest = server.takeRequest();
        assertEquals("DELETE", deleteRequest.getMethod());
        assertEquals("/v2/panels/p_1", deleteRequest.getPath());

        enqueueOk("{}");
        api.updatePanelTarget("p_1", PanelTargetRequest.addGroups("openid_group_003"));
        RecordedRequest targetRequest = server.takeRequest();
        assertEquals("PUT", targetRequest.getMethod());
        assertEquals("/v2/panels/p_1/target", targetRequest.getPath());
        JsonNode targetBody = JsonUtil.tree(targetRequest.getBody().readUtf8());
        assertEquals("add", targetBody.get("op").asText());
        assertEquals("openid_group_003", targetBody.path("group_openids").get(0).asText());
        assertEquals(false, targetBody.has("user_openids"));

        enqueueOk("{}");
        api.updatePanelTarget("p_1", PanelTargetRequest.delUsers(List.of("openid_user_001")));
        assertEquals("del", JsonUtil.tree(server.takeRequest().getBody().readUtf8()).get("op").asText());

        assertThrows(IllegalArgumentException.class, () -> api.getPanel(" "));
        assertThrows(IllegalArgumentException.class, () -> PanelTargetRequest.addGroups(List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                PanelTargetRequest.addUsers(java.util.Collections.nCopies(21, "u")));
        assertThrows(NullPointerException.class, () -> api.updatePanel("p_1", null));
    }
}
