package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * V2 引用回复 message_reference。
 * 非机器人消息的索引从事件 message_scene.ext 的 msg_idx 获取；
 * 机器人自己消息的索引从发消息响应 ext_info.ref_idx 获取。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-MessageReference</a>
 */
public class V2MessageReference {

    /** 被引用消息 ID，例如 REFIDX_xxxxxx */
    @JsonProperty("message_id")
    private String messageId;

    public static V2MessageReference of(String messageId) {
        return new V2MessageReference().setMessageId(messageId);
    }

    public String getMessageId() {
        return messageId;
    }

    public V2MessageReference setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }
}