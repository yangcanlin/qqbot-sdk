package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 频道引用消息对象。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messagereference">MessageReference</a>
 */
public class MessageReference {

    /** 需要引用回复的消息 id */
    @JsonProperty("message_id")
    private String messageId;

    /** 是否忽略获取引用消息详情错误，默认否 */
    @JsonProperty("ignore_get_message_error")
    private Boolean ignoreGetMessageError;

    public String getMessageId() {
        return messageId;
    }

    public MessageReference setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public Boolean getIgnoreGetMessageError() {
        return ignoreGetMessageError;
    }

    public MessageReference setIgnoreGetMessageError(Boolean ignoreGetMessageError) {
        this.ignoreGetMessageError = ignoreGetMessageError;
        return this;
    }
}