package cn.meilinkj.qqbot.core.event;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;
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
}