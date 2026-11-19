package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.ChannelMessageRequest;
import cn.meilinkj.qqbot.api.request.MessageRequest;
import cn.meilinkj.qqbot.api.request.StreamMessageRequest;
import cn.meilinkj.qqbot.api.response.BotGuild;
import cn.meilinkj.qqbot.api.response.BotInfo;
import cn.meilinkj.qqbot.api.response.Dms;
import cn.meilinkj.qqbot.api.response.GroupBotState;
import cn.meilinkj.qqbot.api.response.SendMessageResponse;
import cn.meilinkj.qqbot.api.response.GatewayBotInfo;
import cn.meilinkj.qqbot.api.response.GatewayInfo;
import cn.meilinkj.qqbot.api.response.ShareLinkResponse;
import cn.meilinkj.qqbot.api.response.StreamMessageResponse;
import cn.meilinkj.qqbot.api.response.UserInfo;
import cn.meilinkj.qqbot.api.support.MsgSeqAllocator;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.model.channel.Message;
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
 * 各业务 API 的路径与请求体测试，对齐官方接口定义：
 * 群聊 /v2/groups、单聊 /v2/users、频道 /channels 与 /dms、互动 /interactions。
 */
class ApiPathTest {

    private MockWebServer server;
    private HttpApiClient http;
    private BotApi bot;
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
        bot = new BotApi(http);
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
    void botInfoAndGuildsPagination() throws Exception {
        enqueueOk("{\"id\":\"BOT1\",\"username\":\"阳光小助手\",\"avatar\":\"https://a\",\"bot\":true,\"share_url\":\"https://s\",\"welcome_msg\":\"欢迎\"}");
        BotInfo info = bot.getBot();
        server.takeRequest();
        RecordedRequest me = server.takeRequest();
        assertEquals("GET", me.getMethod());
        assertEquals("/users/@me", me.getPath());
        assertEquals("BOT1", info.getId());
        assertEquals("阳光小助手", info.getUsername());
        assertEquals(Boolean.TRUE, info.isBot());
        assertEquals("欢迎", info.getWelcomeMsg());

        enqueueOk("[{\"id\":\"G1\",\"name\":\"读书分享会\",\"owner\":false,\"member_count\":6,\"max_members\":5000000,\"joined_at\":\"2025-01-09T15:17:23+08:00\"}]");
        List<BotGuild> guilds = bot.getGuilds();
        RecordedRequest all = server.takeRequest();
        assertEquals("/users/@me/guilds", all.getPath());
        assertEquals("G1", guilds.get(0).getId());
        assertEquals(Boolean.FALSE, guilds.get(0).isOwner());
        assertEquals(6, guilds.get(0).getMemberCount());

        enqueueOk("[]");
        bot.getGuilds("G9", "G1", 20);
        RecordedRequest page = server.takeRequest();
        assertEquals("/users/@me/guilds?before=G9&after=G1&limit=20", page.getPath());
        assertThrows(IllegalArgumentException.class, () -> bot.getGuilds(null, null, 101));
    }

    @Test
    void botGroupStateAndShareLink() throws Exception {
        enqueueOk("{\"member_openid\":\"B1\",\"joined_at\":\"2025-06-15T14:30:00+08:00\",\"allow_proactive_msg\":false,\"recv_msg_setting\":\"only_mention\",\"member_role\":\"admin\"}");
        GroupBotState state = bot.getGroupBotState("G1");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v2/groups/G1/bot_state", request.getPath());
        assertEquals("only_mention", state.getRecvMsgSetting());
        assertEquals("admin", state.getMemberRole());
        assertEquals(Boolean.FALSE, state.isAllowProactiveMsg());
        assertThrows(IllegalArgumentException.class, () -> bot.getGroupBotState(" "));

        enqueueOk("{\"data\":{\"url\":\"https://qun.qq.com/qunpro/robot/qunshare?data=xxx\"}}");
        ShareLinkResponse link = bot.generateShareLink("custom_data_123");
        RecordedRequest post = server.takeRequest();
        assertEquals("POST", post.getMethod());
        assertEquals("/v2/generate_url_link", post.getPath());
        assertEquals("custom_data_123", JsonUtil.tree(post.getBody().readUtf8()).get("callback_data").asText());
        assertEquals("https://qun.qq.com/qunpro/robot/qunshare?data=xxx", link.getUrl());

        enqueueOk("{\"data\":{\"url\":\"https://x\"}}");
        bot.generateShareLink(null);
        RecordedRequest plain = server.takeRequest();
        assertEquals(false, JsonUtil.tree(plain.getBody().readUtf8()).has("callback_data"));
        assertThrows(IllegalArgumentException.class, () -> bot.generateShareLink("x".repeat(33)));
    }

    @Test
    void c2cStreamMessageChunks() throws Exception {
        enqueueOk("{\"id\":\"STREAM1\",\"timestamp\":\"2026-07-21T10:00:00+08:00\",\"ext_info\":{\"ref_idx\":\"REFIDX_abc\"}}");
        StreamMessageResponse first = c2c.sendStream("U1",
                StreamMessageRequest.replaceMarkdown("MSG1", "正在生成").setMsgSeq(1));
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("POST", request.getMethod());
        assertEquals("/v2/users/U1/stream_messages", request.getPath());
        JsonNode firstBody = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals("replace", firstBody.get("input_mode").asText());
        assertEquals(1, firstBody.get("input_state").asInt());
        assertEquals(0, firstBody.get("index").asInt());
        assertEquals("markdown", firstBody.get("content_type").asText());
        assertEquals("MSG1", firstBody.get("msg_id").asText());
        assertEquals(false, firstBody.has("stream_msg_id"));
        assertEquals("STREAM1", first.getId());
        assertEquals("REFIDX_abc", first.getExtInfo().getRefIdx());

        enqueueOk("{\"id\":\"STREAM1\",\"timestamp\":\"2026-07-21T10:00:01+08:00\",\"remain_msg_len\":0}");
        StreamMessageResponse last = c2c.sendStream("U1", StreamMessageRequest.replaceMarkdown("MSG1", "完整内容")
                .setStreamMsgId(first.getId()).setIndex(1).setMsgSeq(1).finish());
        JsonNode lastBody = JsonUtil.tree(server.takeRequest().getBody().readUtf8());
        assertEquals(10, lastBody.get("input_state").asInt());
        assertEquals(1, lastBody.get("index").asInt());
        assertEquals("STREAM1", lastBody.get("stream_msg_id").asText());
        assertEquals(0, last.getRemainMsgLen().intValue());

        assertThrows(IllegalArgumentException.class, () -> new StreamMessageRequest().setInputState(3));
        assertThrows(IllegalArgumentException.class, () -> new StreamMessageRequest().setInputMode("patch"));
        assertThrows(IllegalArgumentException.class, () -> new StreamMessageRequest().setIndex(-1));
    }

    @Test
    void c2cUserInfoAndGatewayUrl() throws Exception {
        enqueueOk("{\"username\":\"user\",\"avatar\":\"https://thirdqq.qlogo.cn/a\",\"allow_proactive_msg\":true,\"union_openid\":\"UNION1\"}");
        UserInfo user = c2c.getUserInfo("U1");
        server.takeRequest();
        RecordedRequest infoRequest = server.takeRequest();
        assertEquals("GET", infoRequest.getMethod());
        assertEquals("/v2/users/U1/info", infoRequest.getPath());
        assertEquals(Boolean.TRUE, user.getAllowProactiveMsg());
        assertEquals("UNION1", user.getUnionOpenid());

        enqueueOk("{\"url\":\"wss://qqbot-api.example.com/websocket\"}");
        GatewayInfo gateway = new GatewayInfoApi(http).getGateway();
        RecordedRequest gatewayRequest = server.takeRequest();
        assertEquals("GET", gatewayRequest.getMethod());
        assertEquals("/gateway", gatewayRequest.getPath());
        assertEquals("wss://qqbot-api.example.com/websocket", gateway.getUrl());
    }

    @Test
    void gatewayBotShardsAndSessionQuota() throws Exception {
        enqueueOk("{\"url\":\"wss://qqbot-api.example.com/websocket\",\"shards\":9,"
                + "\"session_start_limit\":{\"total\":1000,\"remaining\":999,\"reset_after\":14400000,\"max_concurrency\":1}}");
        GatewayBotInfo botGateway = new GatewayInfoApi(http).getGatewayBot();
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/gateway/bot", request.getPath());
        assertEquals("wss://qqbot-api.example.com/websocket", botGateway.getUrl());
        assertEquals(9, botGateway.getShards());
        assertEquals(1000, botGateway.getSessionStartLimit().getTotal());
        assertEquals(999, botGateway.getSessionStartLimit().getRemaining());
        assertEquals(14400000L, botGateway.getSessionStartLimit().getResetAfter());
        assertEquals(1, botGateway.getSessionStartLimit().getMaxConcurrency());
    }

    @Test
    void blankOpenidRejected() {
        assertThrows(IllegalArgumentException.class, () -> group.sendText("", "x"));
        assertThrows(IllegalArgumentException.class, () -> c2c.sendText(" ", "x"));
        assertThrows(NullPointerException.class, () -> group.sendMessage("G1", (MessageRequest) null));
    }
}