package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 创建私信会话响应。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">私信消息</a>
 */
public class Dms {

    /** 私信会话所属频道（guild）ID */
    @JsonProperty("guild_id")
    private String guildId;

    /** 私信子频道 ID */
    @JsonProperty("channel_id")
    private String channelId;

    /** 创建时间 */
    @JsonProperty("create_time")
    private String createTime;

    public String getGuildId() {
        return guildId;
    }

    public String getChannelId() {
        return channelId;
    }

    public String getCreateTime() {
        return createTime;
    }
}