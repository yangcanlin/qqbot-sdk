package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.response.GatewayBotInfo;
import cn.meilinkj.qqbot.api.response.GatewayInfo;
import cn.meilinkj.qqbot.core.http.HttpApiClient;

import java.util.concurrent.CompletableFuture;

/**
 * 网关接入点查询接口。
 *
 * <p>SDK 建立长连接时内部已自动调用本接口，业务代码通常无需直接使用；
 * 需要自建 WebSocket 或做接入点探活时才调用。</p>
 */
public class GatewayInfoApi {

    private final HttpApiClient http;

    public GatewayInfoApi(HttpApiClient http) {
        this.http = http;
    }

    /**
     * 官方：GET /gateway（获取通用 WSS 接入点）；2 QPM / 10 QPM burst。
     * 通过返回的 url 可建立 WebSocket 长连接。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/gateway.get.html">文档</a>
     */
    public GatewayInfo getGateway() {
        return http.get("/gateway", GatewayInfo.class);
    }

    public CompletableFuture<GatewayInfo> getGatewayAsync() {
        return http.getAsync("/gateway", GatewayInfo.class);
    }

    /**
     * 官方：GET /gateway/bot（获取带分片 WSS 接入点）。
     * 除接入地址外还返回平台建议的分片数与 Session 创建配额，多分片部署前先查它确定 numShards。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/wss/shard_url_get.html">文档</a>
     */
    public GatewayBotInfo getGatewayBot() {
        return http.get("/gateway/bot", GatewayBotInfo.class);
    }

    public CompletableFuture<GatewayBotInfo> getGatewayBotAsync() {
        return http.getAsync("/gateway/bot", GatewayBotInfo.class);
    }
}
