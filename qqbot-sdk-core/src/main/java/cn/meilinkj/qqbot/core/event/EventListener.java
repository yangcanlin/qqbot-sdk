package cn.meilinkj.qqbot.core.event;

/**
 * 事件监听器。实现类声明具体事件泛型即可接收对应事件：
 *
 * <pre>{@code
 * public class EchoListener implements EventListener<GroupAtMessageCreateEvent> {
 *     public void onEvent(GroupAtMessageCreateEvent event) { ... }
 * }
 * }</pre>
 */
@FunctionalInterface
public interface EventListener<E extends BotEvent> {

    void onEvent(E event);
}