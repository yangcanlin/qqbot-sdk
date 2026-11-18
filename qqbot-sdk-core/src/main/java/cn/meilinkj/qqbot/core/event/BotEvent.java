package cn.meilinkj.qqbot.core.event;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * 所有事件的基类，携带 Payload 外层元数据（id/s/t）与原始 d 节点。
 * 事件结构对齐
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html">事件订阅与通知</a>。
 */
public abstract class BotEvent {

    private String eventId;
    private Long seq;
    private String eventType;
    private JsonNode raw;

    /** Payload 外层 id（事件 id，可用于 event_id 被动回复）。 */
    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /** Payload 外层 s（下行序列号）。 */
    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    /** Payload 外层 t（事件名）。 */
    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /** 原始事件体 d。 */
    public JsonNode getRaw() {
        return raw;
    }

    public void setRaw(JsonNode raw) {
        this.raw = raw;
    }
}