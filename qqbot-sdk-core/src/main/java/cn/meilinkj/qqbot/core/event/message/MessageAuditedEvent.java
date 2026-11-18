package cn.meilinkj.qqbot.core.event.message;

import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.channel.MessageAudited;

/**
 * MESSAGE_AUDIT_PASS / MESSAGE_AUDIT_REJECT：消息审核结果事件。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messageaudited">消息审核对象(MessageAudited)</a>
 */
public class MessageAuditedEvent extends BotEvent {

    private MessageAudited audited;

    public MessageAudited getAudited() {
        return audited;
    }

    public void setAudited(MessageAudited audited) {
        this.audited = audited;
    }

    public boolean isPass() {
        return "MESSAGE_AUDIT_PASS".equals(getEventType());
    }
}