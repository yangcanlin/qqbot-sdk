package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 修改指令面板请求体（官方 PUT /v2/panels/{panel_id}）。
 * 传入后会覆盖原有面板元素列表与备注，不影响已关联的用户 / 群列表。
 */
public class PanelUpdateRequest {

    @JsonProperty("panel")
    private Panel panel;

    public static PanelUpdateRequest of(Panel panel) {
        return new PanelUpdateRequest().setPanel(PanelCreateRequest.requirePanel(panel));
    }

    public Panel getPanel() {
        return panel;
    }

    public PanelUpdateRequest setPanel(Panel panel) {
        this.panel = panel;
        return this;
    }
}
