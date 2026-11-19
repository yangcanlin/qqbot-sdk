package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流式发送单聊消息请求体（官方 POST /v2/users/{user_openid}/stream_messages）。
 *
 * <p>官方规则：每个分片使用相同 stream_msg_id（首片不传，由服务端返回），index 从 0 递增，
 * input_state=1 生成中、10 生成结束；input_mode=replace 时 content_raw 为全量正文，
 * 且必须以已下发前缀开头（否则 err_code=40007）。</p>
 */
public class StreamMessageRequest {

    /** 增量拼接：content_raw 追加到已有内容 */
    public static final String INPUT_MODE_APPEND = "append";
    /** 全量替换：content_raw 为当前完整正文 */
    public static final String INPUT_MODE_REPLACE = "replace";

    /** 生成中 */
    public static final int INPUT_STATE_GENERATING = 1;
    /** 生成结束 */
    public static final int INPUT_STATE_FINISHED = 10;

    public static final String CONTENT_TYPE_TEXT = "text";
    public static final String CONTENT_TYPE_MARKDOWN = "markdown";

    @JsonProperty("input_mode")
    private String inputMode;

    /** 输入状态：1=生成中，10=生成结束 */
    @JsonProperty("input_state")
    private Integer inputState;

    /** 分片序号，从 0 递增 */
    @JsonProperty("index")
    private Integer index;

    @JsonProperty("content_type")
    private String contentType;

    @JsonProperty("content_raw")
    private String contentRaw;

    /** 被动回复事件 ID（与 msg_id 二选一） */
    @JsonProperty("event_id")
    private String eventId;

    /** 被动回复消息 ID（与 event_id 二选一） */
    @JsonProperty("msg_id")
    private String msgId;

    /** 流式消息 ID：首片由服务端返回，后续分片必须携带 */
    @JsonProperty("stream_msg_id")
    private String streamMsgId;

    @JsonProperty("msg_seq")
    private Integer msgSeq;

    /** true 时不校验 msg_id / event_id 有效期 */
    @JsonProperty("is_wakeup")
    private Boolean isWakeup;

    /** 首片（增量文本）：input_mode=append、index=0、生成中。 */
    public static StreamMessageRequest appendText(String msgId, String contentRaw) {
        return new StreamMessageRequest()
                .setMsgId(msgId)
                .setInputMode(INPUT_MODE_APPEND)
                .setContentType(CONTENT_TYPE_TEXT)
                .setContentRaw(contentRaw)
                .setIndex(0)
                .setInputState(INPUT_STATE_GENERATING);
    }

    /** 首片（全量 Markdown）：input_mode=replace、index=0、生成中。 */
    public static StreamMessageRequest replaceMarkdown(String msgId, String fullContent) {
        return new StreamMessageRequest()
                .setMsgId(msgId)
                .setInputMode(INPUT_MODE_REPLACE)
                .setContentType(CONTENT_TYPE_MARKDOWN)
                .setContentRaw(fullContent)
                .setIndex(0)
                .setInputState(INPUT_STATE_GENERATING);
    }

    /** 被动回复：用事件 ID（INTERACTION_CREATE 等场景）代替 msg_id。 */
    public StreamMessageRequest replyByEventId(String eventId) {
        return setEventId(eventId).setMsgId(null);
    }

    /** 标记为最后一片：input_state=10。 */
    public StreamMessageRequest finish() {
        return setInputState(INPUT_STATE_FINISHED);
    }

    public String getInputMode() {
        return inputMode;
    }

    public StreamMessageRequest setInputMode(String inputMode) {
        if (inputMode != null && !INPUT_MODE_APPEND.equals(inputMode) && !INPUT_MODE_REPLACE.equals(inputMode)) {
            throw new IllegalArgumentException("官方 input_mode 仅支持 append / replace，当前: " + inputMode);
        }
        this.inputMode = inputMode;
        return this;
    }

    public Integer getInputState() {
        return inputState;
    }

    public StreamMessageRequest setInputState(Integer inputState) {
        if (inputState != null && inputState != INPUT_STATE_GENERATING && inputState != INPUT_STATE_FINISHED) {
            throw new IllegalArgumentException("官方 input_state 仅支持 1（生成中）/ 10（生成结束），当前: " + inputState);
        }
        this.inputState = inputState;
        return this;
    }

    public Integer getIndex() {
        return index;
    }

    public StreamMessageRequest setIndex(Integer index) {
        if (index != null && index < 0) {
            throw new IllegalArgumentException("官方 index 从 0 递增，不能为负数");
        }
        this.index = index;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public StreamMessageRequest setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getContentRaw() {
        return contentRaw;
    }

    public StreamMessageRequest setContentRaw(String contentRaw) {
        this.contentRaw = contentRaw;
        return this;
    }

    public String getEventId() {
        return eventId;
    }

    public StreamMessageRequest setEventId(String eventId) {
        this.eventId = eventId;
        return this;
    }

    public String getMsgId() {
        return msgId;
    }

    public StreamMessageRequest setMsgId(String msgId) {
        this.msgId = msgId;
        return this;
    }

    public String getStreamMsgId() {
        return streamMsgId;
    }

    public StreamMessageRequest setStreamMsgId(String streamMsgId) {
        this.streamMsgId = streamMsgId;
        return this;
    }

    public Integer getMsgSeq() {
        return msgSeq;
    }

    public StreamMessageRequest setMsgSeq(Integer msgSeq) {
        this.msgSeq = msgSeq;
        return this;
    }

    public Boolean getIsWakeup() {
        return isWakeup;
    }

    public StreamMessageRequest setIsWakeup(Boolean isWakeup) {
        this.isWakeup = isWakeup;
        return this;
    }
}
