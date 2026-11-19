package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通用 WSS 接入点（官方 GET /gateway 响应）。
 */
public class GatewayInfo {

    /** WebSocket 连接地址 */
    @JsonProperty("url")
    private String url;

    public String getUrl() {
        return url;
    }
}
