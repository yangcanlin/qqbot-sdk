package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 消息元素（递归结构），message_type=103 引用消息等场景携带。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html">群@机器人消息-MsgElement</a>
 */
public class MsgElement {

    /** 消息元素在列表中的引用消息索引 */
    @JsonProperty("msg_idx")
    private String msgIdx;

    /** 0=普通文本, 3=结构化卡片, 101=并行消息, 102=聊天记录, 103=引用消息 */
    @JsonProperty("message_type")
    private Integer messageType;

    @JsonProperty("content")
    private String content;

    @JsonProperty("attachments")
    private List<MessageAttachment> attachments;

    @JsonProperty("ark_data")
    private ArkData arkData;

    @JsonProperty("msg_elements")
    private List<MsgElement> msgElements;

    public String getMsgIdx() {
        return msgIdx;
    }

    public MsgElement setMsgIdx(String msgIdx) {
        this.msgIdx = msgIdx;
        return this;
    }

    public Integer getMessageType() {
        return messageType;
    }

    public MsgElement setMessageType(Integer messageType) {
        this.messageType = messageType;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MsgElement setContent(String content) {
        this.content = content;
        return this;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public MsgElement setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    public ArkData getArkData() {
        return arkData;
    }

    public MsgElement setArkData(ArkData arkData) {
        this.arkData = arkData;
        return this;
    }

    public List<MsgElement> getMsgElements() {
        return msgElements;
    }

    public MsgElement setMsgElements(List<MsgElement> msgElements) {
        this.msgElements = msgElements;
        return this;
    }
}