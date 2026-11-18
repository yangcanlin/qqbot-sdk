package cn.meilinkj.qqbot.core;

import cn.meilinkj.qqbot.core.gateway.Intents;

import java.util.Objects;

/**
 * SDK 全局配置。
 *
 * <p>默认接口域名与鉴权方式对齐官方文档
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/api-use.html">接口调用与鉴权</a>：
 * 统一地址 {@code https://api.bot.qq.com}，请求头 {@code Authorization: QQBot {ACCESS_TOKEN}}。</p>
 */
public final class QQBotConfig {

    private final String appId;
    private final String appSecret;
    private final String baseUrl;
    private final long intents;
    private final int[] shard;
    private final int maxRetries;
    private final long retryBaseDelayMs;
    private final long retryMaxDelayMs;
    private final int requestsPerSecond;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final int writeTimeoutMs;
    private final long reconnectBaseDelayMs;
    private final long reconnectMaxDelayMs;
    private final int eventThreads;

    private QQBotConfig(Builder b) {
        this.appId = b.appId;
        this.appSecret = b.appSecret;
        this.baseUrl = b.baseUrl;
        this.intents = b.intents;
        this.shard = b.shard;
        this.maxRetries = b.maxRetries;
        this.retryBaseDelayMs = b.retryBaseDelayMs;
        this.retryMaxDelayMs = b.retryMaxDelayMs;
        this.requestsPerSecond = b.requestsPerSecond;
        this.connectTimeoutMs = b.connectTimeoutMs;
        this.readTimeoutMs = b.readTimeoutMs;
        this.writeTimeoutMs = b.writeTimeoutMs;
        this.reconnectBaseDelayMs = b.reconnectBaseDelayMs;
        this.reconnectMaxDelayMs = b.reconnectMaxDelayMs;
        this.eventThreads = b.eventThreads;
    }

    public static Builder builder(String appId, String appSecret) {
        return new Builder(appId, appSecret);
    }

    public String getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public long getIntents() {
        return intents;
    }

    public int[] getShard() {
        return shard.clone();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryBaseDelayMs() {
        return retryBaseDelayMs;
    }

    public long getRetryMaxDelayMs() {
        return retryMaxDelayMs;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public int getWriteTimeoutMs() {
        return writeTimeoutMs;
    }

    public long getReconnectBaseDelayMs() {
        return reconnectBaseDelayMs;
    }

    public long getReconnectMaxDelayMs() {
        return reconnectMaxDelayMs;
    }

    public int getEventThreads() {
        return eventThreads;
    }

    public static final class Builder {
        private final String appId;
        private final String appSecret;
        private String baseUrl = "https://api.bot.qq.com";
        private long intents = Intents.GROUP_AND_C2C_EVENT.getValue();
        private int[] shard = {0, 1};
        private int maxRetries = 3;
        private long retryBaseDelayMs = 500;
        private long retryMaxDelayMs = 30_000;
        private int requestsPerSecond = 20;
        private int connectTimeoutMs = 10_000;
        private int readTimeoutMs = 15_000;
        private int writeTimeoutMs = 15_000;
        private long reconnectBaseDelayMs = 1_000;
        private long reconnectMaxDelayMs = 60_000;
        private int eventThreads = 4;

        private Builder(String appId, String appSecret) {
            this.appId = Objects.requireNonNull(appId, "appId 不能为空");
            this.appSecret = Objects.requireNonNull(appSecret, "appSecret 不能为空");
            if (appId.isBlank() || appSecret.isBlank()) {
                throw new IllegalArgumentException("appId/appSecret 不能为空字符串");
            }
        }

        /** 接口域名，默认 https://api.bot.qq.com（官方统一地址）。 */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = Objects.requireNonNull(baseUrl);
            return this;
        }

        /** 订阅事件掩码，可用 {@link Intents#of(Intents...)} 组合。 */
        public Builder intents(long intents) {
            this.intents = intents;
            return this;
        }

        /** 分片 [shard_id, num_shards]，规则见官方文档 Shard 机制。 */
        public Builder shard(int shardId, int numShards) {
            if (shardId < 0 || numShards <= 0 || shardId >= numShards) {
                throw new IllegalArgumentException("非法 shard: [" + shardId + "," + numShards + "]");
            }
            this.shard = new int[]{shardId, numShards};
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        public Builder retryBaseDelayMs(long retryBaseDelayMs) {
            this.retryBaseDelayMs = retryBaseDelayMs;
            return this;
        }

        public Builder retryMaxDelayMs(long retryMaxDelayMs) {
            this.retryMaxDelayMs = retryMaxDelayMs;
            return this;
        }

        /** 客户端全局限流（QPS），官方各接口另有独立频控。 */
        public Builder requestsPerSecond(int requestsPerSecond) {
            this.requestsPerSecond = Math.max(1, requestsPerSecond);
            return this;
        }

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder writeTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
            return this;
        }

        public Builder reconnectBaseDelayMs(long reconnectBaseDelayMs) {
            this.reconnectBaseDelayMs = reconnectBaseDelayMs;
            return this;
        }

        public Builder reconnectMaxDelayMs(long reconnectMaxDelayMs) {
            this.reconnectMaxDelayMs = reconnectMaxDelayMs;
            return this;
        }

        public Builder eventThreads(int eventThreads) {
            this.eventThreads = Math.max(1, eventThreads);
            return this;
        }

        public QQBotConfig build() {
            return new QQBotConfig(this);
        }
    }
}