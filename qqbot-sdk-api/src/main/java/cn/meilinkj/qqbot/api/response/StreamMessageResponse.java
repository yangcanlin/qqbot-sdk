package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 流式发送单聊消息响应（官方 POST /v2/users/{user_openid}/stream_messages）。
 * 首片返回的 id 即 stream_msg_id，后续分片必须携带。
 */
public class StreamMessageResponse {

    @JsonProperty("id")
    private String id;

    /** 消息发送时间，RFC3339 */
    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("ext_info")
    private ExtInfo extInfo;

    /** 流式消息剩余长度（字符数） */
    @JsonProperty("remain_msg_len")
    private Integer remainMsgLen;

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ExtInfo getExtInfo() {
        return extInfo;
    }

    public Integer getRemainMsgLen() {
        return remainMsgLen;
    }

    /** 官方 MessageExtInfo。 */
    public static class ExtInfo {

        /** 引用消息索引，对应消息 ext 里的 msg_idx 与 ref_msg_idx */
        @JsonProperty("ref_idx")
        private String refIdx;

        public String getRefIdx() {
            return refIdx;
        }
    }
}
