package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * V2 发送消息响应。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-响应</a>
 */
public class SendMessageResponse {

    /** 消息 ID */
    @JsonProperty("id")
    private String id;

    /** 发送时间，RFC3339 格式（如 2024-02-20T14:33:47+08:00） */
    @JsonProperty("timestamp")
    private String timestamp;

    /** 引用消息扩展信息 */
    @JsonProperty("ext_info")
    private ExtInfo extInfo;

    public String getId() {
        return id;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public ExtInfo getExtInfo() {
        return extInfo;
    }

    /** 引用索引，用于撤回引用消息等场景 */
    public static class ExtInfo {

        @JsonProperty("ref_idx")
        private String refIdx;

        public String getRefIdx() {
            return refIdx;
        }
    }
}