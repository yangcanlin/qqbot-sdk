package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息删除（撤回）事件数据。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messagedelete">MessageDelete</a>
 */
public class MessageDelete {

    /** 被删除的消息内容 */
    @JsonProperty("message")
    private Message message;

    /** 执行删除操作的用户 */
    @JsonProperty("op_user")
    private User opUser;

    public Message getMessage() {
        return message;
    }

    public MessageDelete setMessage(Message message) {
        this.message = message;
        return this;
    }

    public User getOpUser() {
        return opUser;
    }

    public MessageDelete setOpUser(User opUser) {
        this.opUser = opUser;
        return this;
    }
}