package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 分片上传预处理响应。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html">群聊分片上传预处理-响应</a>
 */
public class UploadPrepareResponse {

    @JsonProperty("upload_id")
    private String uploadId;

    /** 每个分片大小（字节），字符串 */
    @JsonProperty("block_size")
    private String blockSize;

    @JsonProperty("parts")
    private List<Part> parts;

    @JsonProperty("upload_config")
    private UploadConfig uploadConfig;

    public String getUploadId() {
        return uploadId;
    }

    public String getBlockSize() {
        return blockSize;
    }

    public List<Part> getParts() {
        return parts;
    }

    public UploadConfig getUploadConfig() {
        return uploadConfig;
    }

    /** 分片信息 */
    public static class Part {

        /** 分片序号，从 0 开始 */
        @JsonProperty("index")
        private Integer index;

        /** 分片预签名上传 URL，使用 PUT 上传原始字节（不带 Authorization） */
        @JsonProperty("presigned_url")
        private String presignedUrl;

        /** 该分片大小（字节），字符串；最后一个分片可能小于 block_size */
        @JsonProperty("block_size")
        private String blockSize;

        public Integer getIndex() {
            return index;
        }

        public String getPresignedUrl() {
            return presignedUrl;
        }

        public String getBlockSize() {
            return blockSize;
        }
    }

    /** 上传配置 */
    public static class UploadConfig {

        /** 并发上传分片数，当前固定 1 */
        @JsonProperty("concurrency")
        private Integer concurrency;

        /** 分片上传重试超时（秒） */
        @JsonProperty("retry_timeout")
        private Integer retryTimeout;

        /** 重试间隔（秒） */
        @JsonProperty("retry_delay")
        private Integer retryDelay;

        public Integer getConcurrency() {
            return concurrency;
        }

        public Integer getRetryTimeout() {
            return retryTimeout;
        }

        public Integer getRetryDelay() {
            return retryDelay;
        }
    }
}