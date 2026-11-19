package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * C2C_MSG_RECEIVE（Intents.GROUP_AND_C2C_EVENT）：用户在机器人资料卡手动开启「主动消息」推送开关时触发，
 * 开启后机器人可向该用户发送主动消息。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_msg_receive.html">单聊消息接收开启</a>
 */
public class C2CMsgReceiveEvent extends BotEvent {

    /** 操作时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    /** 用户 OpenID */
    @JsonProperty("openid")
    private String openid;

    public Long getTimestamp() {
        return timestamp;
    }

    public C2CMsgReceiveEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public C2CMsgReceiveEvent setOpenid(String openid) {
        this.openid = openid;
        return this;
    }
}
