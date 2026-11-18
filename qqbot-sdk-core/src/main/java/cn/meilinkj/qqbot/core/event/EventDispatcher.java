package cn.meilinkj.qqbot.core.event;

import cn.meilinkj.qqbot.core.gateway.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * 事件分发器：将网关 Dispatch（op=0）事件按类型路由给监听器。
 *
 * <p>支持两种注册方式：</p>
 * <pre>{@code
 * // 1. 泛型监听器（按事件 class 匹配，含子类）
 * dispatcher.addListener(new EventListener<C2CMessageCreateEvent>() { ... });
 * // 2. 按事件名注册原始监听（收到原始 d 节点）
 * dispatcher.addRawListener("GUILD_MEMBER_ADD", d -> { ... });
 * }</pre>
 *
 * <p>监听器在独立线程池中执行，单个监听器异常不影响其他监听器。</p>
 */
public class EventDispatcher {

    private static final Logger log = LoggerFactory.getLogger(EventDispatcher.class);

    private final List<Registration<?>> listeners = new CopyOnWriteArrayList<>();
    private final Map<String, List<Consumer<com.fasterxml.jackson.databind.JsonNode>>> rawListeners =
            new ConcurrentHashMap<>();
    private final ExecutorService executor;

    public EventDispatcher(int threads) {
        this.executor = Executors.newFixedThreadPool(Math.max(1, threads), r -> {
            Thread t = new Thread(r, "qqbot-event-dispatcher");
            t.setDaemon(true);
            return t;
        });
    }

    /** 注册泛型监听器，事件类型从泛型参数解析。 */
    public <E extends BotEvent> void addListener(EventListener<E> listener) {
        Class<E> eventType = resolveEventType(listener.getClass());
        if (eventType == null) {
            throw new IllegalArgumentException(
                    "无法解析监听器泛型事件类型，请使用 addListener(Class, Consumer) 显式指定: "
                            + listener.getClass().getName());
        }
        listeners.add(new Registration<>(eventType, listener));
    }

    /** 显式指定事件类型注册监听器（lambda 场景）。 */
    public <E extends BotEvent> void addListener(Class<E> eventType, Consumer<E> consumer) {
        listeners.add(new Registration<>(eventType, consumer::accept));
    }

    /** 按官方事件名注册原始监听器，参数为事件体 d。 */
    public void addRawListener(String eventType, Consumer<com.fasterxml.jackson.databind.JsonNode> consumer) {
        rawListeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /** 分发网关 Payload（op=0 Dispatch）。 */
    public void dispatch(Payload payload) {
        String name = payload.getT();
        EventType type = EventType.fromName(name);
        BotEvent event;
        if (type != null) {
            event = type.create(payload.getD());
        } else {
            log.debug("未知事件类型 {}，以 GenericEvent 分发", name);
            event = new GenericEvent();
        }
        event.setEventId(payload.getId());
        event.setSeq(payload.getS());
        event.setEventType(name);
        event.setRaw(payload.getD());

        executor.execute(() -> notifyListeners(event, payload));
    }

    private void notifyListeners(BotEvent event, Payload payload) {
        for (Registration<?> registration : listeners) {
            if (registration.eventType.isInstance(event)) {
                try {
                    registration.invoke(event);
                } catch (Exception e) {
                    log.error("事件监听器执行异常 event={} listener={}", event.getEventType(),
                            registration.listener.getClass().getName(), e);
                }
            }
        }
        List<Consumer<com.fasterxml.jackson.databind.JsonNode>> raw = rawListeners.get(event.getEventType());
        if (raw != null) {
            for (Consumer<com.fasterxml.jackson.databind.JsonNode> consumer : raw) {
                try {
                    consumer.accept(payload.getD());
                } catch (Exception e) {
                    log.error("原始事件监听器执行异常 event={}", event.getEventType(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends BotEvent> Class<E> resolveEventType(Class<?> listenerClass) {
        for (Type type : listenerClass.getGenericInterfaces()) {
            if (type instanceof ParameterizedType pt && pt.getRawType() == EventListener.class) {
                Type arg = pt.getActualTypeArguments()[0];
                if (arg instanceof Class<?> clazz && BotEvent.class.isAssignableFrom(clazz)) {
                    return (Class<E>) clazz;
                }
            }
        }
        Type superclass = listenerClass.getGenericSuperclass();
        if (superclass instanceof Class<?> clazz && clazz != Object.class) {
            return resolveEventType(clazz);
        }
        return null;
    }

    public void shutdown() {
        executor.shutdown();
    }

    private static final class Registration<E extends BotEvent> {
        final Class<E> eventType;
        final EventListener<E> listener;

        Registration(Class<E> eventType, EventListener<E> listener) {
            this.eventType = eventType;
            this.listener = listener;
        }

        @SuppressWarnings("unchecked")
        void invoke(BotEvent event) {
            listener.onEvent((E) event);
        }
    }
}