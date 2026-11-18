package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息审核对象 MessageAudited（MESSAGE_AUDIT_PASS/REJECT 事件）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messageaudited">消息审核对象(MessageAudited)</a>
 */
public class MessageAudited {

    @JsonProperty("audit_id")
    private String auditId;

    /** 消息 id，只有审核通过事件才会有值 */
    @JsonProperty("message_id")
    private String messageId;

    @JsonProperty("guild_id")
    private String guildId;

    @JsonProperty("channel_id")
    private String channelId;

    /** ISO8601 timestamp */
    @JsonProperty("audit_time")
    private String auditTime;

    /** ISO8601 timestamp */
    @JsonProperty("create_time")
    private String createTime;

    @JsonProperty("seq_in_channel")
    private String seqInChannel;

    public String getAuditId() {
        return auditId;
    }

    public MessageAudited setAuditId(String auditId) {
        this.auditId = auditId;
        return this;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageAudited setMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    public String getGuildId() {
        return guildId;
    }

    public MessageAudited setGuildId(String guildId) {
        this.guildId = guildId;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public MessageAudited setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getAuditTime() {
        return auditTime;
    }

    public MessageAudited setAuditTime(String auditTime) {
        this.auditTime = auditTime;
        return this;
    }

    public String getCreateTime() {
        return createTime;
    }

    public MessageAudited setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getSeqInChannel() {
        return seqInChannel;
    }

    public MessageAudited setSeqInChannel(String seqInChannel) {
        this.seqInChannel = seqInChannel;
        return this;
    }
}