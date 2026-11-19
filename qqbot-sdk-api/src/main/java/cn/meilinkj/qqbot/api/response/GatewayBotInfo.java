package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 带分片信息的 WSS 接入点（官方 GET /gateway/bot 响应）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/wss/shard_url_get.html">获取带分片 WSS 接入点</a>
 */
public class GatewayBotInfo {

    /** WebSocket 连接地址 */
    @JsonProperty("url")
    private String url;

    /** 平台建议的分片数，与 QQBotConfig.shard(shardId, numShards) 的 numShards 对应 */
    @JsonProperty("shards")
    private Integer shards;

    /** 创建 Session 的频率限制信息 */
    @JsonProperty("session_start_limit")
    private SessionStartLimit sessionStartLimit;

    public String getUrl() {
        return url;
    }

    public Integer getShards() {
        return shards;
    }

    public SessionStartLimit getSessionStartLimit() {
        return sessionStartLimit;
    }

    /** Session 创建配额 */
    public static class SessionStartLimit {

        /** 每 24 小时可创建的 Session 总数 */
        @JsonProperty("total")
        private Integer total;

        /** 当前周期内剩余可创建的 Session 数 */
        @JsonProperty("remaining")
        private Integer remaining;

        /** 计数重置的剩余时间，单位毫秒 */
        @JsonProperty("reset_after")
        private Long resetAfter;

        /** 每 5 秒可创建的 Session 数 */
        @JsonProperty("max_concurrency")
        private Integer maxConcurrency;

        public Integer getTotal() {
            return total;
        }

        public Integer getRemaining() {
            return remaining;
        }

        public Long getResetAfter() {
            return resetAfter;
        }

        public Integer getMaxConcurrency() {
            return maxConcurrency;
        }
    }
}
