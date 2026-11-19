package cn.meilinkj.qqbot.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.event.friend.FriendDelEvent;
import cn.meilinkj.qqbot.core.event.group.GroupJoinRequestEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMemberAddEvent;
import cn.meilinkj.qqbot.core.event.group.GroupMemberRemoveEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelCreateEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelDeleteEvent;
import cn.meilinkj.qqbot.core.event.guild.ChannelUpdateEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildCreateEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildDeleteEvent;
import cn.meilinkj.qqbot.core.event.guild.GuildUpdateEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMsgReceiveEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMsgRejectEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.SubscribeMessageStatusEvent;
import cn.meilinkj.qqbot.core.gateway.Intents;
import cn.meilinkj.qqbot.core.gateway.Payload;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 事件分发测试：类型化监听、泛型解析、原始监听、异常隔离、未知事件兜底。
 */
class EventDispatcherTest {

    private EventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new EventDispatcher(2);
    }

    @AfterEach
    void tearDown() {
        dispatcher.shutdown();
    }

    private static Payload groupMessagePayload() {
        Payload p = new Payload();
        p.setOp(0);
        p.setId("EV-1");
        p.setS(5L);
        p.setT("GROUP_AT_MESSAGE_CREATE");
        p.setD(JsonUtil.tree("{\"id\":\"MSG1\",\"content\":\"hello\",\"group_openid\":\"G1\"}"));
        return p;
    }

    @Test
    void typedListenerReceivesMappedEventWithMeta() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<GroupAtMessageCreateEvent> received = new AtomicReference<>();
        dispatcher.addListener(GroupAtMessageCreateEvent.class, e -> {
            received.set(e);
            latch.countDown();
        });
        dispatcher.dispatch(groupMessagePayload());
        assertTrue(latch.await(5, TimeUnit.SECONDS));

        GroupAtMessageCreateEvent e = received.get();
        assertEquals("MSG1", e.getId());
        assertEquals("hello", e.getContent());
        assertEquals("G1", e.getGroupOpenid());
        assertEquals("EV-1", e.getEventId());
        assertEquals(5L, e.getSeq());
        assertEquals("GROUP_AT_MESSAGE_CREATE", e.getEventType());
    }

    @Test
    void genericListenerResolvesEventType() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<String> content = new AtomicReference<>();
        dispatcher.addListener(new EventListener<GroupAtMessageCreateEvent>() {
            @Override
            public void onEvent(GroupAtMessageCreateEvent event) {
                content.set(event.getContent());
                latch.countDown();
            }
        });
        dispatcher.dispatch(groupMessagePayload());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("hello", content.get());
    }

    @Test
    void rawListenerReceivesOriginalNode() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<JsonNode> raw = new AtomicReference<>();
        dispatcher.addRawListener("GROUP_AT_MESSAGE_CREATE", d -> {
            raw.set(d);
            latch.countDown();
        });
        dispatcher.dispatch(groupMessagePayload());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals("MSG1", raw.get().get("id").asText());
    }

    @Test
    void listenerExceptionDoesNotAffectOthers() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        dispatcher.addListener(GroupAtMessageCreateEvent.class, e -> {
            throw new IllegalStateException("boom");
        });
        dispatcher.addListener(GroupAtMessageCreateEvent.class, e -> latch.countDown());
        dispatcher.dispatch(groupMessagePayload());
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    void unknownEventFallsBackToGenericEvent() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<BotEvent> received = new AtomicReference<>();
        dispatcher.addListener(GenericEvent.class, e -> {
            received.set(e);
            latch.countDown();
        });
        Payload p = new Payload();
        p.setOp(0);
        p.setT("SOME_FUTURE_EVENT");
        p.setD(JsonUtil.tree("{\"x\":1}"));
        dispatcher.dispatch(p);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertInstanceOf(GenericEvent.class, received.get());
        assertEquals("SOME_FUTURE_EVENT", received.get().getEventType());
    }

    @Test
    void eventTypeMappingMatchesOfficialNames() {
        assertEquals(EventType.GROUP_AT_MESSAGE_CREATE, EventType.fromName("GROUP_AT_MESSAGE_CREATE"));
        assertEquals(EventType.C2C_MESSAGE_CREATE, EventType.fromName("C2C_MESSAGE_CREATE"));
        assertEquals(EventType.INTERACTION_CREATE, EventType.fromName("INTERACTION_CREATE"));
        assertEquals(EventType.READY, EventType.fromName("READY"));

        BotEvent c2c = EventType.C2C_MESSAGE_CREATE.create(
                JsonUtil.tree("{\"id\":\"M2\",\"content\":\"hi\",\"author\":{\"user_openid\":\"U1\"}}"));
        assertInstanceOf(C2CMessageCreateEvent.class, c2c);
        assertEquals("U1", ((C2CMessageCreateEvent) c2c).getAuthor().getUserOpenid());
    }

    @Test
    void groupMemberEventsParseOfficialPayloads() {
        assertEquals(Intents.GROUP_MEMBER_EVENT, EventType.GROUP_MEMBER_ADD.getIntent());
        assertEquals(Intents.GROUP_MEMBER_EVENT, EventType.GROUP_MEMBER_REMOVE.getIntent());
        assertEquals(Intents.GROUP_MEMBER_EVENT, EventType.GROUP_JOIN_REQUEST.getIntent());
        assertEquals(Intents.GROUP_AND_C2C_EVENT, EventType.SUBSCRIBE_MESSAGE_STATUS.getIntent());

        GroupMemberAddEvent add = (GroupMemberAddEvent) EventType.GROUP_MEMBER_ADD.create(
                JsonUtil.tree("{\"timestamp\":1784276757,\"group_openid\":\"G1\",\"member_openid\":\"M1\"}"));
        assertEquals(1784276757L, (long) add.getTimestamp());
        assertEquals("G1", add.getGroupOpenid());
        assertEquals("M1", add.getMemberOpenid());

        GroupMemberRemoveEvent remove = (GroupMemberRemoveEvent) EventType.GROUP_MEMBER_REMOVE.create(
                JsonUtil.tree("{\"timestamp\":1784276759,\"group_openid\":\"G1\",\"member_openid\":\"M1\",\"user_openid\":\"U1\"}"));
        assertEquals("M1", remove.getMemberOpenid());
        assertEquals("U1", remove.getUserOpenid());

        GroupJoinRequestEvent join = (GroupJoinRequestEvent) EventType.GROUP_JOIN_REQUEST.create(JsonUtil.tree(
                "{\"group_openid\":\"G1\",\"join_request_id\":\"JR1\",\"member_openid\":\"M1\","
                        + "\"username\":\"tester\",\"apply_at\":\"2026-08-05T16:21:40+08:00\",\"apply_source\":\"self_apply\","
                        + "\"verify_info\":{\"method\":\"verify_message\",\"verify_message\":\"hello\"},"
                        + "\"auto_approved\":{\"strategy_id\":\"st_7c0b77d442\"}}"));
        assertEquals("JR1", join.getJoinRequestId());
        assertEquals("self_apply", join.getApplySource());
        assertEquals("verify_message", join.getVerifyInfo().getMethod());
        assertEquals("hello", join.getVerifyInfo().getVerifyMessage());
        assertEquals("st_7c0b77d442", join.getAutoApproved().getStrategyId());

        SubscribeMessageStatusEvent subscribe = (SubscribeMessageStatusEvent) EventType.SUBSCRIBE_MESSAGE_STATUS.create(
                JsonUtil.tree("{\"openid\":\"U1\",\"result\":[{\"template_id\":10001,\"custom_template_id\":\"tpl_abc123\","
                        + "\"op\":1,\"subscribe_id\":\"sub_def456\",\"subscribe_ts\":1784276820,\"update_ts\":1784276820}]}"));
        assertEquals("U1", subscribe.getOpenid());
        assertEquals(1, subscribe.getResult().size());
        SubscribeMessageStatusEvent.TemplateResult first = subscribe.getResult().get(0);
        assertEquals(10001L, (long) first.getTemplateId());
        assertEquals(1, (int) first.getOp());
        assertEquals("tpl_abc123", first.getCustomTemplateId());
        assertEquals("sub_def456", first.getSubscribeId());
    }

    @Test
    void guildAndChannelEventsParseOfficialPayloads() {
        GuildCreateEvent create = (GuildCreateEvent) EventType.GUILD_CREATE.create(JsonUtil.tree(
                "{\"id\":\"123456789012345678\",\"name\":\"技术交流频道\",\"icon\":\"https://thirdqq.qlogo.cn/0\","
                        + "\"owner_id\":\"123456789012345678\",\"member_count\":100,\"max_members\":1000,"
                        + "\"description\":\"专注于技术分享与交流的频道\",\"joined_at\":\"2026-01-01T00:00:00+08:00\","
                        + "\"op_user_id\":\"123456789012345678\"}"));
        assertEquals("123456789012345678", create.getId());
        assertEquals("技术交流频道", create.getName());
        assertEquals(100, (int) create.getMemberCount());
        assertEquals(1000, (int) create.getMaxMembers());
        assertEquals("2026-01-01T00:00:00+08:00", create.getJoinedAt());
        assertEquals("123456789012345678", create.getOpUserId());

        // 官方 update/delete 页字段表与 create 完全一致，示例 JSON 未出现 joined_at 与 op_user_id
        GuildUpdateEvent update = (GuildUpdateEvent) EventType.GUILD_UPDATE.create(JsonUtil.tree(
                "{\"id\":\"123456789012345678\",\"name\":\"更新后的频道\",\"owner_id\":\"123456789012345678\","
                        + "\"icon\":\"https://thirdqq.qlogo.cn/0\",\"member_count\":12,\"max_members\":1000,"
                        + "\"description\":\"更新后的描述\"}"));
        assertEquals(12, (int) update.getMemberCount());
        assertNull(update.getJoinedAt());
        assertNull(update.getOpUserId());

        GuildDeleteEvent delete = (GuildDeleteEvent) EventType.GUILD_DELETE.create(JsonUtil.tree(
                "{\"id\":\"123456789012345678\",\"name\":\"测试频道\",\"member_count\":10}"));
        assertEquals("测试频道", delete.getName());
        assertEquals(Intents.GUILDS, EventType.GUILD_DELETE.getIntent());

        ChannelCreateEvent channelCreate = (ChannelCreateEvent) EventType.CHANNEL_CREATE.create(JsonUtil.tree(
                "{\"id\":\"123456\",\"guild_id\":\"123456789012345678\",\"name\":\"新子频道\",\"type\":0,"
                        + "\"sub_type\":0,\"position\":1,\"owner_id\":\"123456789012345678\"}"));
        assertEquals("123456", channelCreate.getId());
        assertEquals("123456789012345678", channelCreate.getGuildId());
        assertEquals(0, (int) channelCreate.getType());
        assertEquals(1, (int) channelCreate.getPosition());
        assertEquals("123456789012345678", channelCreate.getOwnerId());

        ChannelUpdateEvent channelUpdate = (ChannelUpdateEvent) EventType.CHANNEL_UPDATE.create(JsonUtil.tree(
                "{\"id\":\"123456\",\"guild_id\":\"123456789012345678\",\"name\":\"更新后的子频道\",\"type\":0,"
                        + "\"sub_type\":0,\"position\":1,\"owner_id\":\"123456789012345678\",\"op_user_id\":\"123\"}"));
        assertEquals("更新后的子频道", channelUpdate.getName());
        assertEquals("123", channelUpdate.getOpUserId());

        ChannelDeleteEvent channelDelete = (ChannelDeleteEvent) EventType.CHANNEL_DELETE.create(JsonUtil.tree(
                "{\"id\":\"123456\",\"guild_id\":\"123456789012345678\",\"name\":\"被删除的子频道\",\"type\":10007}"));
        assertEquals(10007, (int) channelDelete.getType());
    }

    @Test
    void friendDelAndC2cMsgSwitchEventsParseOfficialPayloads() {
        FriendDelEvent del = (FriendDelEvent) EventType.FRIEND_DEL.create(
                JsonUtil.tree("{\"openid\":\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4\",\"timestamp\":1784570524}"));
        assertEquals("A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4", del.getOpenid());
        assertEquals(1784570524L, (long) del.getTimestamp());
        assertNull(del.getAuthor());

        FriendDelEvent withAuthor = (FriendDelEvent) EventType.FRIEND_DEL.create(JsonUtil.tree(
                "{\"openid\":\"U1\",\"timestamp\":1784570524,\"author\":{\"union_openid\":\"O1\"}}"));
        assertEquals("O1", withAuthor.getAuthor().getUnionOpenid());

        C2CMsgReceiveEvent receive = (C2CMsgReceiveEvent) EventType.C2C_MSG_RECEIVE.create(JsonUtil.tree(
                "{\"openid\":\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4\",\"timestamp\":1784570617}"));
        assertEquals(1784570617L, (long) receive.getTimestamp());

        C2CMsgRejectEvent reject = (C2CMsgRejectEvent) EventType.C2C_MSG_REJECT.create(JsonUtil.tree(
                "{\"openid\":\"A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4\",\"timestamp\":1784570599}"));
        assertEquals("A1B2C3D4E5F6A1B2C3D4E5F6A1B2C3D4", reject.getOpenid());

        assertEquals(Intents.GROUP_AND_C2C_EVENT, EventType.FRIEND_DEL.getIntent());
        assertEquals(Intents.GROUP_AND_C2C_EVENT, EventType.C2C_MSG_RECEIVE.getIntent());
        assertEquals(Intents.GUILD_MEMBERS, EventType.GUILD_MEMBER_ADD.getIntent());
        assertInstanceOf(GenericEvent.class, EventType.GUILD_MEMBER_ADD.create(JsonUtil.tree("{\"x\":1}")));
    }
}