package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.channel.Message;
import cn.meilinkj.qqbot.core.model.channel.User;

/**
 * 频道消息删除事件：MESSAGE_DELETE（私域）、PUBLIC_MESSAGE_DELETE（公域）、DIRECT_MESSAGE_DELETE（私信）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messagedelete">MessageDelete</a>
 */
public class MessageDeleteEvent extends BotEvent {

    @JsonProperty("message")
    private Message message;

    @JsonProperty("op_user")
    private User opUser;

    public Message getMessage() {
        return message;
    }

    public MessageDeleteEvent setMessage(Message message) {
        this.message = message;
        return this;
    }

    public User getOpUser() {
        return opUser;
    }

    public MessageDeleteEvent setOpUser(User opUser) {
        this.opUser = opUser;
        return this;
    }
}