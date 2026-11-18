package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 富媒体上传请求（URL 方式）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">群聊上传富媒体</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_files.post.html">单聊上传富媒体</a>
 */
public class MediaUploadRequest {

    /** 1=图片(png/jpg), 2=视频(mp4), 3=语音(silk), 4=文件（暂不开放） */
    @JsonProperty("file_type")
    private Integer fileType;

    /** 资源 URL（需公网可访问），与 upload_id、file_data 三选一 */
    @JsonProperty("url")
    private String url;

    /** true=直接发送消息（消耗主动消息额度），false=仅上传返回 file_info */
    @JsonProperty("srv_send_msg")
    private Boolean srvSendMsg;

    /** 分片上传返回的 upload_id（合并上传时必填） */
    @JsonProperty("upload_id")
    private String uploadId;

    /** 分片/二进制上传时必填的文件名 */
    @JsonProperty("file_name")
    private String fileName;

    /** 二进制直传数据（Base64 编码字符串），原始大小 ≤5MB */
    @JsonProperty("file_data")
    private String fileData;

    public static MediaUploadRequest ofUrl(int fileType, String url) {
        return new MediaUploadRequest().setFileType(fileType).setUrl(url).setSrvSendMsg(false);
    }

    public Integer getFileType() {
        return fileType;
    }

    public MediaUploadRequest setFileType(Integer fileType) {
        this.fileType = fileType;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public MediaUploadRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    public Boolean getSrvSendMsg() {
        return srvSendMsg;
    }

    public MediaUploadRequest setSrvSendMsg(Boolean srvSendMsg) {
        this.srvSendMsg = srvSendMsg;
        return this;
    }

    public String getUploadId() {
        return uploadId;
    }

    public MediaUploadRequest setUploadId(String uploadId) {
        this.uploadId = uploadId;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public MediaUploadRequest setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public String getFileData() {
        return fileData;
    }

    public MediaUploadRequest setFileData(String fileData) {
        this.fileData = fileData;
        return this;
    }
}