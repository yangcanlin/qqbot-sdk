package cn.meilinkj.qqbot.spring;

import cn.meilinkj.qqbot.core.gateway.Intents;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * QQ 机器人配置属性（前缀 qqbot）。
 *
 * <pre>
 * qqbot:
 *   app-id: ${QQBOT_APP_ID}
 *   app-secret: ${QQBOT_APP_SECRET}
 *   intents: 33554432   # GROUP_AND_C2C_EVENT，见 Intents
 * </pre>
 */
@ConfigurationProperties(prefix = "qqbot")
public class QQBotProperties {

    /** 机器人 AppID（必填） */
    private String appId;

    /** 机器人 AppSecret（必填，建议通过环境变量注入，勿硬编码） */
    private String appSecret;

    /** OpenAPI 域名，默认 https://api.bot.qq.com */
    private String baseUrl;

    /** 事件订阅 intents 位掩码，默认 GROUP_AND_C2C_EVENT(1<<25)，组合方式见 Intents.of */
    private long intents = Intents.GROUP_AND_C2C_EVENT.getValue();

    /** 分片 ID，默认 0 */
    private int shardId = 0;

    /** 分片总数，默认 1 */
    private int numShards = 1;

    /** 最大重试次数 */
    private int maxRetries = 3;

    /** 客户端每秒请求限流 */
    private int requestsPerSecond = 20;

    /** 事件分发线程数 */
    private int eventThreads = 4;

    /** 应用就绪后是否自动连接网关，默认 true */
    private boolean autoStart = true;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public long getIntents() {
        return intents;
    }

    public void setIntents(long intents) {
        this.intents = intents;
    }

    public int getShardId() {
        return shardId;
    }

    public void setShardId(int shardId) {
        this.shardId = shardId;
    }

    public int getNumShards() {
        return numShards;
    }

    public void setNumShards(int numShards) {
        this.numShards = numShards;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public int getEventThreads() {
        return eventThreads;
    }

    public void setEventThreads(int eventThreads) {
        this.eventThreads = eventThreads;
    }

    public boolean isAutoStart() {
        return autoStart;
    }

    public void setAutoStart(boolean autoStart) {
        this.autoStart = autoStart;
    }
}