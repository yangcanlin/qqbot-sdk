package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分片上传预处理请求。file_size/block_size 按官方定义为字符串。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html">群聊分片上传预处理</a>
 */
public class UploadPrepareRequest {

    @JsonProperty("file_type")
    private Integer fileType;

    /** 文件大小（字节），字符串 */
    @JsonProperty("file_size")
    private String fileSize;

    @JsonProperty("file_name")
    private String fileName;

    /** 文件完整 MD5 */
    @JsonProperty("md5")
    private String md5;

    /** 文件完整 SHA1 */
    @JsonProperty("sha1")
    private String sha1;

    /** 文件前 10002432 字节的 MD5 */
    @JsonProperty("md5_10m")
    private String md510m;

    public UploadPrepareRequest(int fileType, long fileSize, String fileName,
                                String md5, String sha1, String md510m) {
        this.fileType = fileType;
        this.fileSize = String.valueOf(fileSize);
        this.fileName = fileName;
        this.md5 = md5;
        this.sha1 = sha1;
        this.md510m = md510m;
    }

    public Integer getFileType() {
        return fileType;
    }

    public String getFileSize() {
        return fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMd5() {
        return md5;
    }

    public String getSha1() {
        return sha1;
    }

    public String getMd510m() {
        return md510m;
    }
}