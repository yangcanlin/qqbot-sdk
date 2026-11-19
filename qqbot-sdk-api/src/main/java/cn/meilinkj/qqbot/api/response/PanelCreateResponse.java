package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 创建指令面板响应（官方 POST /v2/panels）。
 */
public class PanelCreateResponse {

    /** 新创建的面板 ID，后续修改 / 删除 / 查询详情均使用此 ID */
    @JsonProperty("panel_id")
    private String panelId;

    public String getPanelId() {
        return panelId;
    }
}
