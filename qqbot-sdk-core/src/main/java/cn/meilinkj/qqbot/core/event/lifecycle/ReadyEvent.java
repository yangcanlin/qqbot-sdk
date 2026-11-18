package cn.meilinkj.qqbot.core.event.lifecycle;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.channel.User;

/**
 * READY 事件：Identify 鉴权成功后下发，携带 session_id。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#登录鉴权获得-session">登录鉴权获得 Session</a>
 */
public class ReadyEvent extends BotEvent {

    @JsonProperty("version")
    private Integer version;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("user")
    private User user;

    @JsonProperty("shard")
    private int[] shard;

    public Integer getVersion() {
        return version;
    }

    public ReadyEvent setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getSessionId() {
        return sessionId;
    }

    public ReadyEvent setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public User getUser() {
        return user;
    }

    public ReadyEvent setUser(User user) {
        this.user = user;
        return this;
    }

    public int[] getShard() {
        return shard;
    }

    public ReadyEvent setShard(int[] shard) {
        this.shard = shard;
        return this;
    }
}