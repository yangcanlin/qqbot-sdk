package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.ArkData;
import cn.meilinkj.qqbot.core.model.Author;
import cn.meilinkj.qqbot.core.model.MessageAttachment;
import cn.meilinkj.qqbot.core.model.MessageScene;
import cn.meilinkj.qqbot.core.model.MsgElement;

import java.util.List;

/**
 * C2C_MESSAGE_CREATE：用户给机器人发送单聊消息时触发。
 * message_type：0=纯文本，3=ARK卡片（ark_data 有值），103=引用消息（msg_elements 有值）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_message_create.html">单聊消息事件</a>
 */
public class C2CMessageCreateEvent extends BotEvent {

    /** 消息 ID，可用于被动回复（msg_id）和撤回 */
    @JsonProperty("id")
    private String id;

    /** 发送者（user_openid 有值） */
    @JsonProperty("author")
    private Author author;

    @JsonProperty("content")
    private String content;

    /** RFC3339 格式 */
    @JsonProperty("timestamp")
    private String timestamp;

    /** 0=普通文本, 3=结构化卡片, 101=并行消息, 102=聊天记录, 103=引用消息 */
    @JsonProperty("message_type")
    private Integer messageType;

    @JsonProperty("message_scene")
    private MessageScene messageScene;

    @JsonProperty("attachments")
    private List<MessageAttachment> attachments;

    @JsonProperty("ark_data")
    private ArkData arkData;

    @JsonProperty("msg_elements")
    private List<MsgElement> msgElements;

    public String getId() {
        return id;
    }

    public C2CMessageCreateEvent setId(String id) {
        this.id = id;
        return this;
    }

    public Author getAuthor() {
        return author;
    }

    public C2CMessageCreateEvent setAuthor(Author author) {
        this.author = author;
        return this;
    }

    public String getContent() {
        return content;
    }

    public C2CMessageCreateEvent setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public C2CMessageCreateEvent setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public C2CMessageCreateEvent setMessageType(Integer messageType) {
        this.messageType = messageType;
        return this;
    }

    public MessageScene getMessageScene() {
        return messageScene;
    }

    public C2CMessageCreateEvent setMessageScene(MessageScene messageScene) {
        this.messageScene = messageScene;
        return this;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public C2CMessageCreateEvent setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public ArkData getArkData() {
        return arkData;
    }

    public C2CMessageCreateEvent setArkData(ArkData arkData) {
        this.arkData = arkData;
        return this;
    }

    public List<MsgElement> getMsgElements() {
        return msgElements;
    }

    public C2CMessageCreateEvent setMsgElements(List<MsgElement> msgElements) {
        this.msgElements = msgElements;
        return this;
    }
}