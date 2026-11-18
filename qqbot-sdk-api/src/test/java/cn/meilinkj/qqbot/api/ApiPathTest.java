package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.ChannelMessageRequest;
import cn.meilinkj.qqbot.api.request.MessageRequest;
import cn.meilinkj.qqbot.api.response.Dms;
import cn.meilinkj.qqbot.api.response.SendMessageResponse;
import cn.meilinkj.qqbot.api.support.MsgSeqAllocator;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.model.channel.Message;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 各业务 API 的路径与请求体测试，对齐官方接口定义：
 * 群聊 /v2/groups、单聊 /v2/users、频道 /channels 与 /dms、互动 /interactions。
 */
class ApiPathTest {

    private MockWebServer server;
    private HttpApiClient http;
    private GroupApi group;
    private C2CApi c2c;
    private ChannelApi channel;
    private InteractionApi interaction;

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
        MsgSeqAllocator allocator = new MsgSeqAllocator();
        group = new GroupApi(http, allocator);
        c2c = new C2CApi(http, allocator);
        channel = new ChannelApi(http);
        interaction = new InteractionApi(http);
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
    void groupSendAndReplySeq() throws Exception {
        enqueueOk("{\"id\":\"R1\",\"timestamp\":\"2024-02-20T14:33:47+08:00\",\"ext_info\":{\"ref_idx\":\"IDX1\"}}");
        enqueueOk("{\"id\":\"R2\",\"timestamp\":\"2024-02-20T14:33:48+08:00\"}");

        SendMessageResponse first = group.replyText("G1", "MSG1", "a");
        SendMessageResponse second = group.replyText("G1", "MSG1", "b");
        assertEquals("R1", first.getId());
        assertEquals("IDX1", first.getExtInfo().getRefIdx());
        assertEquals("R2", second.getId());

        server.takeRequest(); // token
        RecordedRequest r1 = server.takeRequest();
        assertEquals("/v2/groups/G1/messages", r1.getPath());
        assertEquals(1, JsonUtil.tree(r1.getBody().readUtf8()).get("msg_seq").asInt());
        RecordedRequest r2 = server.takeRequest();
        assertEquals(2, JsonUtil.tree(r2.getBody().readUtf8()).get("msg_seq").asInt());
    }

    @Test
    void groupRecallPath() throws Exception {
        enqueueOk("");
        group.recall("G1", "M1");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("DELETE", request.getMethod());
        assertEquals("/v2/groups/G1/messages/M1", request.getPath());
    }

    @Test
    void c2cInputNotify() throws Exception {
        enqueueOk("{\"id\":\"R1\",\"timestamp\":\"t\"}");
        c2c.sendInputNotify("U1", 20);
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("/v2/users/U1/messages", request.getPath());
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals(6, body.get("msg_type").asInt());
        assertEquals(1, body.get("input_notify").get("input_type").asInt());
        assertEquals(20, body.get("input_notify").get("input_second").asInt());
    }

    @Test
    void c2cWakeupIsMutuallyExclusiveWithReply() throws Exception {
        enqueueOk("{\"id\":\"R1\",\"timestamp\":\"t\"}");
        c2c.sendWakeupText("U1", "回来看看");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals(true, body.get("is_wakeup").asBoolean());
        assertEquals(false, body.has("msg_id"));
    }

    @Test
    void channelSendAndRecall() throws Exception {
        enqueueOk("{\"id\":\"CM1\",\"channel_id\":\"C1\",\"content\":\"hi\"}");
        Message message = channel.sendMessage("C1", ChannelMessageRequest.text("hi").replyTo("M0"));
        assertEquals("CM1", message.getId());
        server.takeRequest();
        RecordedRequest send = server.takeRequest();
        assertEquals("/channels/C1/messages", send.getPath());
        var body = JsonUtil.tree(send.getBody().readUtf8());
        assertEquals("hi", body.get("content").asText());
        assertEquals("M0", body.get("msg_id").asText());

        enqueueOk("");
        channel.recall("C1", "CM1", true);
        RecordedRequest recall = server.takeRequest();
        assertEquals("DELETE", recall.getMethod());
        assertEquals("/channels/C1/messages/CM1?hidetip=true", recall.getPath());
    }

    @Test
    void dmsCreateSendRecall() throws Exception {
        enqueueOk("{\"guild_id\":\"DG1\",\"channel_id\":\"DC1\",\"create_time\":\"t\"}");
        Dms dms = channel.createDms("USER1", "GUILD1");
        assertEquals("DG1", dms.getGuildId());
        server.takeRequest();
        RecordedRequest create = server.takeRequest();
        assertEquals("/users/@me/dms", create.getPath());
        var createBody = JsonUtil.tree(create.getBody().readUtf8());
        assertEquals("USER1", createBody.get("recipient_id").asText());
        assertEquals("GUILD1", createBody.get("source_guild_id").asText());

        enqueueOk("{\"id\":\"DM1\",\"content\":\"hello\"}");
        Message message = channel.sendDms("DG1", ChannelMessageRequest.text("hello"));
        assertEquals("DM1", message.getId());
        RecordedRequest send = server.takeRequest();
        assertEquals("/dms/DG1/messages", send.getPath());

        enqueueOk("");
        channel.recallDms("DG1", "DM1", false);
        RecordedRequest recall = server.takeRequest();
        assertEquals("/dms/DG1/messages/DM1?hidetip=false", recall.getPath());
    }

    @Test
    void interactionRespond() throws Exception {
        enqueueOk("{}");
        interaction.respondSuccess("I1");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("PUT", request.getMethod());
        assertEquals("/interactions/I1", request.getPath());
        assertEquals(0, JsonUtil.tree(request.getBody().readUtf8()).get("code").asInt());
        assertThrows(IllegalArgumentException.class, () -> interaction.respond("I1", 6));
    }

    @Test
    void blankOpenidRejected() {
        assertThrows(IllegalArgumentException.class, () -> group.sendText("", "x"));
        assertThrows(IllegalArgumentException.class, () -> c2c.sendText(" ", "x"));
        assertThrows(NullPointerException.class, () -> group.sendMessage("G1", (MessageRequest) null));
    }
}