package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 仅返回版本号的写操作响应（官方 PUT /v2/menu 与 PUT /v2/panels/{panel_id} 的响应体都是 version）。
 */
public class VersionResponse {

    /** 本次修改后的版本号，可用于判断配置是否有变更 */
    @JsonProperty("version")
    private Integer version;

    public Integer getVersion() {
        return version;
    }
}
