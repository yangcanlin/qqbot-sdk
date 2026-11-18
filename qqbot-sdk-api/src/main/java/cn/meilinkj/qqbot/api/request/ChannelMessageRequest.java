package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.model.channel.ChannelMarkdown;
import cn.meilinkj.qqbot.core.model.channel.MessageArk;
import cn.meilinkj.qqbot.core.model.channel.MessageEmbed;
import cn.meilinkj.qqbot.core.model.channel.MessageReference;

/**
 * 频道 / 频道私信发送消息请求体。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html">发送子频道消息</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">发送私信消息</a>
 */
public class ChannelMessageRequest {

    @JsonProperty("content")
    private String content;

    @JsonProperty("embed")
    private MessageEmbed embed;

    @JsonProperty("ark")
    private MessageArk ark;

    /** 图片 URL，直接展示为图片消息 */
    @JsonProperty("image")
    private String image;

    /** 被动回复的消息 ID（事件 d.id），5 分钟内有效 */
    @JsonProperty("msg_id")
    private String msgId;

    /** 被动回复的事件 ID（事件外层 id） */
    @JsonProperty("event_id")
    private String eventId;

    @JsonProperty("markdown")
    private ChannelMarkdown markdown;

    @JsonProperty("message_reference")
    private MessageReference messageReference;

    public static ChannelMessageRequest text(String content) {
        return new ChannelMessageRequest().setContent(content);
    }

    public static ChannelMessageRequest image(String imageUrl) {
        return new ChannelMessageRequest().setImage(imageUrl);
    }

    public ChannelMessageRequest replyTo(String msgId) {
        return setMsgId(msgId);
    }

    public String getContent() {
        return content;
    }

    public ChannelMessageRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public MessageEmbed getEmbed() {
        return embed;
    }

    public ChannelMessageRequest setEmbed(MessageEmbed embed) {
        this.embed = embed;
        return this;
    }

    public MessageArk getArk() {
        return ark;
    }

    public ChannelMessageRequest setArk(MessageArk ark) {
        this.ark = ark;
        return this;
    }

    public String getImage() {
        return image;
    }

    public ChannelMessageRequest setImage(String image) {
        this.image = image;
        return this;
    }

    public String getMsgId() {
        return msgId;
    }

    public ChannelMessageRequest setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public ChannelMessageRequest setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public ChannelMarkdown getMarkdown() {
        return markdown;
    }

    public ChannelMessageRequest setMarkdown(ChannelMarkdown markdown) {
        this.markdown = markdown;
        return this;
    }

    public MessageReference getMessageReference() {
        return messageReference;
    }

    public ChannelMessageRequest setMessageReference(MessageReference messageReference) {
        this.messageReference = messageReference;
        return this;
    }
}