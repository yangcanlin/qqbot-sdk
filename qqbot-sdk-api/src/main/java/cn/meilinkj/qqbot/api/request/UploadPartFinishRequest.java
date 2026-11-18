package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 分片上传完成通知请求（每个分片 PUT 成功后调用）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_part_finish.post.html">群聊分片上传完成</a>
 */
public class UploadPartFinishRequest {

    @JsonProperty("upload_id")
    private String uploadId;

    /** 分片序号，从 0 开始 */
    @JsonProperty("part_index")
    private Integer partIndex;

    /** 分片大小（字节），字符串 */
    @JsonProperty("block_size")
    private String blockSize;

    /** 该分片的 MD5 */
    @JsonProperty("md5")
    private String md5;

    public UploadPartFinishRequest(String uploadId, int partIndex, long blockSize, String md5) {
        this.uploadId = uploadId;
        this.partIndex = partIndex;
        this.blockSize = String.valueOf(blockSize);
        this.md5 = md5;
    }

    public String getUploadId() {
        return uploadId;
    }

    public Integer getPartIndex() {
        return partIndex;
    }

    public String getBlockSize() {
        return blockSize;
    }

    public String getMd5() {
        return md5;
    }
}