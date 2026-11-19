package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.BlacklistUserRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalOpRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalStrategyRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalStrategyUpdateRequest;
import cn.meilinkj.qqbot.api.request.MuteSettingRequest;
import cn.meilinkj.qqbot.api.request.StrategyGroupAction;
import cn.meilinkj.qqbot.api.request.StrategyWhitelistUsersRequest;
import cn.meilinkj.qqbot.api.response.BlacklistEntry;
import cn.meilinkj.qqbot.api.response.BlacklistOperateResult;
import cn.meilinkj.qqbot.api.response.GroupInfo;
import cn.meilinkj.qqbot.api.response.GroupMember;
import cn.meilinkj.qqbot.api.response.JoinApprovalStrategy;
import cn.meilinkj.qqbot.api.response.JoinRequest;
import cn.meilinkj.qqbot.api.response.MuteSetting;
import cn.meilinkj.qqbot.api.response.Page;
import cn.meilinkj.qqbot.api.response.StrategyOperateResult;
import cn.meilinkj.qqbot.api.response.StrategyWhitelistResult;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;
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
 * 群聊管理接口路径、请求体与分页解析测试，对齐官方「服务端接口/群聊管理」16 个端点。
 */
class GroupManageApiTest {

    private MockWebServer server;
    private HttpApiClient http;
    private GroupManageApi manage;

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
        manage = new GroupManageApi(http);
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
    void groupInfoAndMemberPaths() throws Exception {
        enqueueOk("{\"group_openid\":\"G1\",\"group_name\":\"测试群\",\"group_finger_memo\":\"123456\","
                + "\"group_class_text\":\"兴趣\",\"group_tags\":[\"技术\"],\"group_member_num\":128,\"union_group_openid\":\"UG1\"}");
        GroupInfo info = manage.getGroupInfo("G1");
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v2/groups/G1/info", request.getPath());
        assertEquals("测试群", info.getGroupName());
        assertEquals(List.of("技术"), info.getGroupTags());
        assertEquals(128, info.getGroupMemberNum());

        enqueueOk("{\"members\":[{\"member_openid\":\"M1\",\"username\":\"张三\",\"member_role\":\"admin\",\"bot\":false,\"joined_at\":\"2026-01-01T00:00:00+08:00\"}],\"next_cursor\":\"C2\"}");
        Page<GroupMember> page = manage.getGroupMembers("G1", "C1");
        RecordedRequest members = server.takeRequest();
        assertEquals("/v2/groups/G1/members?cursor=C1", members.getPath());
        assertEquals(1, page.getItems().size());
        assertEquals("M1", page.getItems().get(0).getMemberOpenid());
        assertEquals(Boolean.FALSE, page.getItems().get(0).isBot());
        assertEquals("C2", page.getNextCursor());
        assertTrue(page.hasNext());

        enqueueOk("{\"member_openid\":\"M1\",\"username\":\"张三\"}");
        GroupMember member = manage.getGroupMember("G1", "M1");
        RecordedRequest one = server.takeRequest();
        assertEquals("/v2/groups/G1/members/M1", one.getPath());
        assertEquals("张三", member.getUsername());

        assertThrows(IllegalArgumentException.class, () -> manage.getGroupMember("G1", " "));
    }

    @Test
    void batchRemoveMembers() throws Exception {
        enqueueOk("{\"remove_members_result\":\"success\"}");
        String result = manage.batchRemoveMembers("G1", List.of("M1", "M2"));
        server.takeRequest();
        RecordedRequest request = server.takeRequest();
        assertEquals("/v2/groups/G1/batch_remove_members", request.getPath());
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals(2, body.get("member_openids").size());
        assertEquals("success", result);
        assertThrows(IllegalArgumentException.class, () -> manage.batchRemoveMembers("G1", List.of()));
        assertThrows(IllegalArgumentException.class, () -> manage.batchRemoveMembers("G1", java.util.Collections.nCopies(21, "M")));
    }

    @Test
    void blacklistQueryAndOperate() throws Exception {
        enqueueOk("{\"users\":[{\"union_openid\":\"U1\",\"member_openid\":\"M1\",\"username\":\"小黑\",\"banned_at\":\"2026-01-01T00:00:00+08:00\",\"bot\":false}],\"next_cursor\":\"\"}");
        Page<BlacklistEntry> page = manage.getBlacklistMembers("G1", "c", 20);
        server.takeRequest();
        RecordedRequest query = server.takeRequest();
        assertEquals("/v2/groups/G1/member_blacklist?cursor=c&limit=20", query.getPath());
        assertEquals("U1", page.getItems().get(0).getUnionOpenid());
        assertFalse(page.hasNext());

        enqueueOk("{\"fail_openids\":[]}");
        BlacklistOperateResult operated = manage.operateBlacklist("G1", BlacklistUserRequest.add(List.of("M1")));
        RecordedRequest add = server.takeRequest();
        assertEquals("/v2/groups/G1/member_blacklist", add.getPath());
        var body = JsonUtil.tree(add.getBody().readUtf8());
        assertEquals("add", body.get("op").asText());
        assertTrue(operated.getFailOpenids().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> BlacklistUserRequest.remove(java.util.Collections.nCopies(21, "M")));
        assertThrows(IllegalArgumentException.class, () -> manage.getBlacklistMembers("G1", null, 101));
    }

    @Test
    void muteSettingQueryAndApply() throws Exception {
        enqueueOk("{\"global_rule\":{\"mode\":\"schedule\",\"schedule_rules\":[{\"task_id\":\"T1\",\"start_at\":\"2026-01-01T00:00:00+08:00\",\"end_at\":\"2026-01-02T00:00:00+08:00\",\"enabled\":true}],"
                + "\"recurring_rules\":[{\"task_id\":\"T2\",\"weekdays\":[1,7],\"start_time\":\"09:00\",\"end_time\":\"18:00\",\"enabled\":false}]},"
                + "\"members\":[{\"member_openid\":\"M1\",\"mute_expire_at\":\"2026-01-01T10:00:00+08:00\",\"username\":\"张三\"}]}");
        MuteSetting setting = manage.getMuteSetting("G1");
        server.takeRequest();
        RecordedRequest get = server.takeRequest();
        assertEquals("/v2/groups/G1/restrict_chat_setting", get.getPath());
        assertEquals("schedule", setting.getGlobalRule().getMode());
        assertEquals("T1", setting.getGlobalRule().getScheduleRules().get(0).getTaskId());
        assertEquals(List.of(1, 7), setting.getGlobalRule().getRecurringRules().get(0).getWeekdays());
        assertEquals("M1", setting.getMembers().get(0).getMemberOpenid());

        enqueueOk("{}");
        manage.setGroupMute("G1", MuteSettingRequest.muteTo("M1", "2026-01-01T10:00:00+08:00").addMemberOp("del", "M2", ""));
        RecordedRequest post = server.takeRequest();
        assertEquals("/v2/groups/G1/restrict_chat_setting", post.getPath());
        var body = JsonUtil.tree(post.getBody().readUtf8());
        assertEquals("add", body.get("members").get(0).get("op").asText());
        assertEquals("2026-01-01T10:00:00+08:00", body.get("members").get(0).get("mute_expire_at").asText());
        assertEquals("", body.get("members").get(1).get("mute_expire_at").asText());
        assertThrows(IllegalArgumentException.class, () -> {
            MuteSettingRequest req = new MuteSettingRequest();
            for (int i = 0; i < 21; i++) {
                req.addMemberOp("add", "M" + i, "t");
            }
        });
    }

    @Test
    void joinRequestListAndApproval() throws Exception {
        enqueueOk("{\"list\":[{\"join_request_id\":\"J1\",\"risk_tips\":[\"异地\"],\"member_openid\":\"M1\",\"username\":\"李四\","
                + "\"apply_at\":\"2026-01-01T00:00:00+08:00\",\"apply_source\":\"invited\",\"invited_by\":\"M9\",\"bot\":false,"
                + "\"verify_info\":{\"method\":\"admin_review_qa\",\"review_qa_list\":[{\"question\":\"Q1\",\"answer\":\"A1\"}]}}],\"next_cursor\":\"N1\"}");
        Page<JoinRequest> page = manage.getJoinRequests("G1", null, 50);
        server.takeRequest();
        RecordedRequest list = server.takeRequest();
        assertEquals("/v2/groups/G1/join_request_list?limit=50", list.getPath());
        JoinRequest jr = page.getItems().get(0);
        assertEquals("J1", jr.getJoinRequestId());
        assertEquals("invited", jr.getApplySource());
        assertEquals("A1", jr.getVerifyInfo().getReviewQaList().get(0).getAnswer());
        assertEquals("N1", page.getNextCursor());

        enqueueOk("{}");
        manage.approveJoinRequest("G1", "M1", JoinApprovalOpRequest.approve("J1"));
        RecordedRequest approve = server.takeRequest();
        assertEquals("/v2/groups/G1/approval_join_request/M1", approve.getPath());
        var approveBody = JsonUtil.tree(approve.getBody().readUtf8());
        assertEquals("approve", approveBody.get("op").asText());
        assertFalse(approveBody.has("reject_reason"));

        enqueueOk("{}");
        manage.approveJoinRequest("G1", "M2", JoinApprovalOpRequest.decline("J2", "不符合要求", true));
        RecordedRequest decline = server.takeRequest();
        var declineBody = JsonUtil.tree(decline.getBody().readUtf8());
        assertEquals("decline", declineBody.get("op").asText());
        assertEquals("不符合要求", declineBody.get("reject_reason").asText());
        assertTrue(declineBody.get("add_to_member_blacklist").asBoolean());
        assertThrows(IllegalArgumentException.class, () -> manage.getJoinRequests("G1", null, 51));
    }

    @Test
    void joinApprovalStrategyCrud() throws Exception {
        enqueueOk("{\"strategies\":[{\"strategy_id\":\"ST1\",\"group_openids\":[\"G1\"],\"whitelist_user_count\":2,\"is_enable\":\"on\","
                + "\"expire_at\":\"2027-01-01T00:00:00+08:00\",\"created_at\":\"t\",\"updated_at\":\"t\",\"remark\":\"备注\"}],\"next_cursor\":\"\"}");
        Page<JoinApprovalStrategy> page = manage.getJoinApprovalStrategies(null, null);
        server.takeRequest();
        RecordedRequest list = server.takeRequest();
        assertEquals("/v2/groups/join_approval_strategy", list.getPath());
        assertEquals("ST1", page.getItems().get(0).getStrategyId());
        assertEquals(List.of("G1"), page.getItems().get(0).getGroupOpenids());

        enqueueOk("{\"strategy_id\":\"ST2\",\"is_enable\":\"on\",\"expire_at\":\"2027-01-01T00:00:00+08:00\"}");
        StrategyOperateResult created = manage.createJoinApprovalStrategy(
                JoinApprovalStrategyRequest.ofGroupOpenids(List.of("G1", "G2")).setRemark("测试"));
        RecordedRequest create = server.takeRequest();
        assertEquals("/v2/groups/join_approval_strategy", create.getPath());
        var createBody = JsonUtil.tree(create.getBody().readUtf8());
        assertEquals(2, createBody.get("group_openids").size());
        assertFalse(createBody.has("group_ids"));
        assertEquals("ST2", created.getStrategyId());

        assertThrows(IllegalArgumentException.class, () -> JoinApprovalStrategyRequest.ofGroupOpenids(List.of()));
        assertThrows(IllegalArgumentException.class, () -> JoinApprovalStrategyRequest.ofGroupOpenids(java.util.Collections.nCopies(101, "G")));

        enqueueOk("{\"is_enable\":\"off\",\"expire_at\":\"2027-01-01T00:00:00+08:00\"}");
        StrategyOperateResult updated = manage.updateJoinApprovalStrategy("ST2",
                new JoinApprovalStrategyUpdateRequest().setIsEnable("off")
                        .setGroupAction(StrategyGroupAction.addOpenids(List.of("G3"))));
        RecordedRequest patch = server.takeRequest();
        assertEquals("PATCH", patch.getMethod());
        assertEquals("/v2/groups/join_approval_strategy/ST2", patch.getPath());
        var patchBody = JsonUtil.tree(patch.getBody().readUtf8());
        assertEquals("off", patchBody.get("is_enable").asText());
        assertEquals("add", patchBody.get("group_action").get("op").asText());
        assertEquals("off", updated.getIsEnable());

        enqueueOk("{}");
        manage.deleteJoinApprovalStrategy("ST2");
        RecordedRequest del = server.takeRequest();
        assertEquals("DELETE", del.getMethod());
        assertEquals("/v2/groups/join_approval_strategy/ST2", del.getPath());

        enqueueOk("{}");
        manage.executeJoinApprovalStrategy("ST2");
        RecordedRequest exec = server.takeRequest();
        assertEquals("POST", exec.getMethod());
        assertEquals("/v2/groups/join_approval_strategy/ST2/execute", exec.getPath());

        enqueueOk("{\"strategy_id\":\"ST2\",\"whitelist_user_count\":2,\"updated_at\":\"t\"}");
        StrategyWhitelistResult whitelist = manage.operateJoinApprovalWhitelist("ST2",
                StrategyWhitelistUsersRequest.add(List.of("1234567", "1234568")));
        RecordedRequest wl = server.takeRequest();
        assertEquals("/v2/groups/join_approval_strategy/ST2/whitelist_users", wl.getPath());
        assertEquals(2, JsonUtil.tree(wl.getBody().readUtf8()).get("whitelist_users").size());
        assertEquals(2, whitelist.getWhitelistUserCount());
        assertThrows(IllegalArgumentException.class, () -> manage.deleteJoinApprovalStrategy(" "));
    }
}
