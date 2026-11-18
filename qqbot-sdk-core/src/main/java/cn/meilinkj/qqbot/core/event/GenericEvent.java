package cn.meilinkj.qqbot.core.event;

/**
 * 未映射为强类型的事件（如频道管理类事件），可通过 {@link #getRaw()} 读取完整事件体。
 * 新增官方事件时可在 {@link EventType} 中注册强类型映射。
 */
public class GenericEvent extends BotEvent {
}