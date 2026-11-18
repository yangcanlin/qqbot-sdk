package cn.meilinkj.qqbot.core.event.message;

import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.channel.Message;

/**
 * 频道消息事件：AT_MESSAGE_CREATE（公域@消息）、MESSAGE_CREATE（私域全量）、
 * DIRECT_MESSAGE_CREATE（频道私信）。事件体为 Message 对象。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/event.html">频道消息事件</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#message">Message</a>
 */
public class ChannelMessageEvent extends BotEvent {

    private Message message;

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}