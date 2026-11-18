package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 频道消息对象 Message。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#message">消息对象(Message)</a>
 */
public class Message {

    @JsonProperty("id")
    private String id;

    @JsonProperty("channel_id")
    private String channelId;

    @JsonProperty("guild_id")
    private String guildId;

    @JsonProperty("content")
    private String content;

    /** ISO8601 timestamp */
    @JsonProperty("timestamp")
    private String timestamp;

    /** ISO8601 timestamp */
    @JsonProperty("edited_timestamp")
    private String editedTimestamp;

    @JsonProperty("mention_everyone")
    private Boolean mentionEveryone;

    @JsonProperty("author")
    private User author;

    @JsonProperty("attachments")
    private List<Attachment> attachments;

    @JsonProperty("embeds")
    private List<MessageEmbed> embeds;

    @JsonProperty("mentions")
    private List<User> mentions;

    @JsonProperty("member")
    private Member member;

    @JsonProperty("ark")
    private MessageArk ark;

    /** 频道消息排序 seq（2022-08-01 后废弃，仅消息事件中有值） */
    @JsonProperty("seq")
    private Integer seq;

    /** 子频道消息 seq，同一子频道内按先后递增 */
    @JsonProperty("seq_in_channel")
    private String seqInChannel;

    @JsonProperty("message_reference")
    private MessageReference messageReference;

    /** 私信消息标记（DIRECT_MESSAGE_CREATE 事件中 direct_message=true） */
    @JsonProperty("direct_message")
    private Boolean directMessage;

    public String getId() {
        return id;
    }

    public Message setId(String id) {
        this.id = id;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public Message setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getGuildId() {
        return guildId;
    }

    public Message setGuildId(String guildId) {
        this.guildId = guildId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Message setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public Message setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getEditedTimestamp() {
        return editedTimestamp;
    }

    public Message setEditedTimestamp(String editedTimestamp) {
        this.editedTimestamp = editedTimestamp;
        return this;
    }

    public Boolean getMentionEveryone() {
        return mentionEveryone;
    }

    public Message setMentionEveryone(Boolean mentionEveryone) {
        this.mentionEveryone = mentionEveryone;
        return this;
    }

    public User getAuthor() {
        return author;
    }

    public Message setAuthor(User author) {
        this.author = author;
        return this;
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public Message setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public List<MessageEmbed> getEmbeds() {
        return embeds;
    }

    public Message setEmbeds(List<MessageEmbed> embeds) {
        this.embeds = embeds;
        return this;
    }

    public List<User> getMentions() {
        return mentions;
    }

    public Message setMentions(List<User> mentions) {
        this.mentions = mentions;
        return this;
    }

    public Member getMember() {
        return member;
    }

    public Message setMember(Member member) {
        this.member = member;
        return this;
    }

    public MessageArk getArk() {
        return ark;
    }

    public Message setArk(MessageArk ark) {
        this.ark = ark;
        return this;
    }

    public Integer getSeq() {
        return seq;
    }

    public Message setSeq(Integer seq) {
        this.seq = seq;
        return this;
    }

    public String getSeqInChannel() {
        return seqInChannel;
    }

    public Message setSeqInChannel(String seqInChannel) {
        this.seqInChannel = seqInChannel;
        return this;
    }

    public MessageReference getMessageReference() {
        return messageReference;
    }

    public Message setMessageReference(MessageReference messageReference) {
        this.messageReference = messageReference;
        return this;
    }

    public Boolean getDirectMessage() {
        return directMessage;
    }

    public Message setDirectMessage(Boolean directMessage) {
        this.directMessage = directMessage;
        return this;
    }

    /**
     * 频道消息附件。
     * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messageattachment">MessageAttachment</a>
     */
    public static class Attachment {

        @JsonProperty("url")
        private String url;

        public String getUrl() {
            return url;
        }

        public Attachment setUrl(String url) {
            this.url = url;
            return this;
        }
    }
}