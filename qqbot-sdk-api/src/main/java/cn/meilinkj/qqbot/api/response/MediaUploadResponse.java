package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 富媒体上传响应。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">群聊上传富媒体-响应</a>
 */
public class MediaUploadResponse {

    /** 文件 UUID */
    @JsonProperty("file_uuid")
    private String fileUuid;

    /** 文件信息，发送富媒体消息时填入 media.file_info */
    @JsonProperty("file_info")
    private String fileInfo;

    /** 有效期（秒），0=永久 */
    @JsonProperty("ttl")
    private Long ttl;

    /** 消息 ID，仅 srv_send_msg=true 时返回 */
    @JsonProperty("id")
    private String id;

    /** 资源原始 URL，仅分片合并上传且 file_type 为 1/2/3 时返回 */
    @JsonProperty("raw_url")
    private String rawUrl;

    public String getFileUuid() {
        return fileUuid;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public Long getTtl() {
        return ttl;
    }

    public String getId() {
        return id;
    }

    public String getRawUrl() {
        return rawUrl;
    }
}