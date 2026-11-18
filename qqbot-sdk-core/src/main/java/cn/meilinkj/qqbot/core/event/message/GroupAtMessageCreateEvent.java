package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.model.ArkData;
import cn.meilinkj.qqbot.core.model.Author;
import cn.meilinkj.qqbot.core.model.MessageAttachment;
import cn.meilinkj.qqbot.core.model.MessageScene;
import cn.meilinkj.qqbot.core.model.MentionsUser;
import cn.meilinkj.qqbot.core.model.MsgElement;

import java.util.List;

/**
 * GROUP_AT_MESSAGE_CREATE：用户在群里@机器人发送消息时触发。
 * content 已自动去除@机器人前缀；相同 msg_id 可能重复推送，需结合 msg_seq 去重。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html">群@机器人消息</a>
 */
public class GroupAtMessageCreateEvent extends BotEvent {

    /** 消息 ID，可用于被动回复（msg_id）和撤回 */
    @JsonProperty("id")
    private String id;

    /** 发送者（member_openid 有值） */
    @JsonProperty("author")
    private Author author;

    @JsonProperty("content")
    private String content;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** RFC3339 格式 */
    @JsonProperty("timestamp")
    private String timestamp;

    /** 消息内容类型（同 C2C_MESSAGE_CREATE） */
    @JsonProperty("message_type")
    private Integer messageType;

    @JsonProperty("message_scene")
    private MessageScene messageScene;

    @JsonProperty("attachments")
    private List<MessageAttachment> attachments;

    /** 消息中@的用户列表（不含@机器人自身） */
    @JsonProperty("mentions")
    private List<MentionsUser> mentions;

    @JsonProperty("ark_data")
    private ArkData arkData;

    @JsonProperty("msg_elements")
    private List<MsgElement> msgElements;

    /** 跨应用关联群 openid，绑定三方应用时有效 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public String getId() {
        return id;
    }

    public GroupAtMessageCreateEvent setId(String id) {
        this.id = id;
        return this;
    }

    public Author getAuthor() {
        return author;
    }

    public GroupAtMessageCreateEvent setAuthor(Author author) {
        this.author = author;
        return this;
    }

    public String getContent() {
        return content;
    }

    public GroupAtMessageCreateEvent setContent(String content) {
        this.content = content;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupAtMessageCreateEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public GroupAtMessageCreateEvent setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public GroupAtMessageCreateEvent setMessageType(Integer messageType) {
        this.messageType = messageType;
        return this;
    }

    public MessageScene getMessageScene() {
        return messageScene;
    }

    public GroupAtMessageCreateEvent setMessageScene(MessageScene messageScene) {
        this.messageScene = messageScene;
        return this;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public GroupAtMessageCreateEvent setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public List<MentionsUser> getMentions() {
        return mentions;
    }

    public GroupAtMessageCreateEvent setMentions(List<MentionsUser> mentions) {
        this.mentions = mentions;
        return this;
    }

    public ArkData getArkData() {
        return arkData;
    }

    public GroupAtMessageCreateEvent setArkData(ArkData arkData) {
        this.arkData = arkData;
        return this;
    }

    public List<MsgElement> getMsgElements() {
        return msgElements;
    }

    public GroupAtMessageCreateEvent setMsgElements(List<MsgElement> msgElements) {
        this.msgElements = msgElements;
        return this;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }

    public GroupAtMessageCreateEvent setUnionGroupOpenid(String unionGroupOpenid) {
        this.unionGroupOpenid = unionGroupOpenid;
        return this;
    }
}