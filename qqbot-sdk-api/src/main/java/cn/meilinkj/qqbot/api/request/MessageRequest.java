package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * V2 发送消息请求体（群聊 /v2/groups/{group_openid}/messages 与
 * 单聊 /v2/users/{user_openid}/messages 通用）。
 *
 * <p>msg_type 决定内容字段：0=文本(content)、2=Markdown(markdown)、
 * 6=输入中状态(input_notify，仅单聊)、7=富媒体(media)、8=图文卡片(card)。</p>
 *
 * <p>被动回复携带 msg_id（来自消息事件 d.id）或 event_id（来自事件外层 id，二选一）；
 * 相同 msg_id+msg_seq 重复发送会失败，多次回复需递增 msg_seq。</p>
 *
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html">发送单聊消息</a>
 */
public class MessageRequest {

    @JsonProperty("msg_type")
    private Integer msgType;

    /** 文本内容，msg_type=0 时为全文；传 markdown 后此字段必须为空 */
    @JsonProperty("content")
    private String content;

    @JsonProperty("markdown")
    private MessageMarkdown markdown;

    @JsonProperty("keyboard")
    private Keyboard keyboard;

    /** 被动回复的消息 ID（事件 d.id），群 5 分钟/单聊 60 分钟内有效 */
    @JsonProperty("msg_id")
    private String msgId;

    /** 被动回复的事件 ID（事件外层 id），与 msg_id 二选一 */
    @JsonProperty("event_id")
    private String eventId;

    /** 回复序号，与 msg_id 联合使用，不填默认 1 */
    @JsonProperty("msg_seq")
    private Integer msgSeq;

    @JsonProperty("media")
    private MediaInfo media;

    @JsonProperty("message_reference")
    private V2MessageReference messageReference;

    @JsonProperty("card")
    private MessageCard card;

    /** 互动召回消息（仅单聊），与 msg_id/event_id 互斥 */
    @JsonProperty("is_wakeup")
    private Boolean isWakeup;

    /** 输入中状态（仅单聊，msg_type=6） */
    @JsonProperty("input_notify")
    private InputNotify inputNotify;

    /** 文本消息。 */
    public static MessageRequest text(String content) {
        return new MessageRequest().setMsgType(0).setContent(content);
    }

    /** Markdown 消息。 */
    public static MessageRequest markdown(MessageMarkdown markdown) {
        return new MessageRequest().setMsgType(2).setMarkdown(markdown);
    }

    /** 富媒体消息（file_info 来自上传接口）。 */
    public static MessageRequest media(String fileInfo) {
        return new MessageRequest().setMsgType(7).setMedia(MediaInfo.of(fileInfo));
    }

    /** 图文卡片消息。 */
    public static MessageRequest card(MessageCard card) {
        return new MessageRequest().setMsgType(8).setCard(card);
    }

    /** 设置为被动回复。 */
    public MessageRequest replyTo(String msgId, int msgSeq) {
        return setMsgId(msgId).setMsgSeq(msgSeq);
    }

    public Integer getMsgType() {
        return msgType;
    }

    public MessageRequest setMsgType(Integer msgType) {
        this.msgType = msgType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageRequest setContent(String content) {
        this.content = content;
        return this;
    }

    public MessageMarkdown getMarkdown() {
        return markdown;
    }

    public MessageRequest setMarkdown(MessageMarkdown markdown) {
        this.markdown = markdown;
        return this;
    }

    public Keyboard getKeyboard() {
        return keyboard;
    }

    public MessageRequest setKeyboard(Keyboard keyboard) {
        this.keyboard = keyboard;
        return this;
    }

    public String getMsgId() {
        return msgId;
    }

    public MessageRequest setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public MessageRequest setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public Integer getMsgSeq() {
        return msgSeq;
    }

    public MessageRequest setMsgSeq(Integer msgSeq) {
        this.msgSeq = msgSeq;
        return this;
    }

    public MediaInfo getMedia() {
        return media;
    }

    public MessageRequest setMedia(MediaInfo media) {
        this.media = media;
        return this;
    }

    public V2MessageReference getMessageReference() {
        return messageReference;
    }

    public MessageRequest setMessageReference(V2MessageReference messageReference) {
        this.messageReference = messageReference;
        return this;
    }

    public MessageCard getCard() {
        return card;
    }

    public MessageRequest setCard(MessageCard card) {
        this.card = card;
        return this;
    }

    public Boolean getIsWakeup() {
        return isWakeup;
    }

    public MessageRequest setIsWakeup(Boolean isWakeup) {
        this.isWakeup = isWakeup;
        return this;
    }

    public InputNotify getInputNotify() {
        return inputNotify;
    }

    public MessageRequest setInputNotify(InputNotify inputNotify) {
        this.inputNotify = inputNotify;
        return this;
    }
}