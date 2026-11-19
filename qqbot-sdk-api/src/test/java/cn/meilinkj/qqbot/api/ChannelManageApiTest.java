package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.ChannelCreateRequest;
import cn.meilinkj.qqbot.api.request.ChannelUpdateRequest;
import cn.meilinkj.qqbot.api.response.Channel;
import cn.meilinkj.qqbot.api.response.Guild;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 频道管理接口路径、请求体与响应解析测试，对齐官方「服务端接口/频道管理」6 个端点。
 */
class ChannelManageApiTest {

    private MockWebServer server;
    private HttpApiClient http;
    private ChannelManageApi api;

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
        api = new ChannelManageApi(http);
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
    void guildDetailPathAndFields() throws Exception {
        enqueueOk("{\"id\":\"G100\",\"name\":\"测试频道\",\"icon\":\"https://avatar.example.com/i.png\","
                + "\"owner_id\":\"U1\",\"owner\":true,\"member_count\":128,\"max_members\":200000,"
                + "\"description\":\"demo\",\"joined_at\":\"2025-06-15T14:30:00+08:00\"}");
        Guild guild = api.getGuild("G100");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/guilds/G100", request.getPath());
        assertEquals("测试频道", guild.getName());
        assertEquals(Boolean.TRUE, guild.getOwner());
        assertEquals(128, guild.getMemberCount().intValue());
        assertThrows(IllegalArgumentException.class, () -> api.getGuild(" "));
    }

    @Test
    void channelsListParsesArrayAndObjectShapes() throws Exception {
        enqueueOk("[{\"id\":\"C1\",\"guild_id\":\"G100\",\"name\":\"公告\",\"type\":0,\"sub_type\":0,"
                + "\"position\":1,\"parent_id\":\"P1\",\"private_type\":0,\"speak_permission\":0}]");
        List<Channel> arrayShape = api.getChannels("G100");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/guilds/G100/channels", request.getPath());
        assertEquals(1, arrayShape.size());
        assertEquals(Channel.TYPE_TEXT, arrayShape.get(0).getType().intValue());
        assertEquals("P1", arrayShape.get(0).getParentId());

        enqueueOk("{\"channels\":[{\"id\":\"C2\",\"name\":\"语音\",\"type\":2}]}");
        List<Channel> objectShape = api.getChannels("G100");
        assertEquals(1, objectShape.size());
        assertEquals(Channel.TYPE_VOICE, objectShape.get(0).getType().intValue());
        server.takeRequest();

        enqueueOk("[]");
        assertEquals(0, api.getChannels("G100").size());
    }

    @Test
    void createChannelBodyMatchesOfficialFields() throws Exception {
        enqueueOk("{\"id\":\"C9\",\"name\":\"新分组\",\"type\":4,\"position\":3,\"parent_id\":\"0\"}");
        Channel created = api.createChannel("G100", ChannelCreateRequest.category("新分组", 3));
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/guilds/G100/channels", request.getPath());
        JsonNode body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals("新分组", body.get("name").asText());
        assertEquals(4, body.get("type").asInt());
        assertEquals(3, body.get("position").asInt());
        assertEquals("新分组", created.getName());

        enqueueOk("{\"id\":\"C10\",\"name\":\"文本\",\"type\":0}");
        api.createChannel("G100", ChannelCreateRequest.text("文本"));
        assertEquals(0, JsonUtil.tree(server.takeRequest().getBody().readUtf8()).get("type").asInt());

        assertThrows(IllegalArgumentException.class, () -> ChannelCreateRequest.category("分组", 1));
    }

    @Test
    void updateChannelUsesPatchAndDeleteHasNoBody() throws Exception {
        enqueueOk("{\"id\":\"C1\",\"name\":\"改名后\"}");
        Channel updated = api.updateChannel("C1", ChannelUpdateRequest.rename("改名后"));
        server.takeRequest();
        RecordedRequest patch = server.takeRequest();
        assertEquals("PATCH", patch.getMethod());
        assertEquals("/channels/C1", patch.getPath());
        assertEquals("改名后", JsonUtil.tree(patch.getBody().readUtf8()).get("name").asText());
        assertEquals("改名后", updated.getName());

        enqueueOk("{\"id\":\"C1\",\"position\":5,\"parent_id\":\"P2\"}");
        api.updateChannel("C1", ChannelUpdateRequest.move(5, "P2"));
        JsonNode moveBody = JsonUtil.tree(server.takeRequest().getBody().readUtf8());
        assertEquals(5, moveBody.get("position").asInt());
        assertEquals("P2", moveBody.get("parent_id").asText());

        enqueueOk("{}");
        api.deleteChannel("C1");
        RecordedRequest delete = server.takeRequest();
        assertEquals("DELETE", delete.getMethod());
        assertEquals("/channels/C1", delete.getPath());

        enqueueOk("{\"id\":\"C1\",\"name\":\"详情\"}");
        assertEquals("详情", api.getChannel("C1").getName());
        assertEquals("GET", server.takeRequest().getMethod());
    }
}
