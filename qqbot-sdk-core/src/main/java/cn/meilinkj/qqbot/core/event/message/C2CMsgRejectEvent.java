package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * C2C_MSG_REJECT（Intents.GROUP_AND_C2C_EVENT）：用户在机器人资料卡手动关闭「主动消息」推送时触发，
 * 关闭后机器人无法向该用户发送主动消息（发送会返回权限不足类错误）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_msg_reject.html">单聊消息接收关闭</a>
 */
public class C2CMsgRejectEvent extends BotEvent {

    /** 操作时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    /** 用户 OpenID */
    @JsonProperty("openid")
    private String openid;

    public Long getTimestamp() {
        return timestamp;
    }

    public C2CMsgRejectEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public C2CMsgRejectEvent setOpenid(String openid) {
        this.openid = openid;
        return this;
    }
}
