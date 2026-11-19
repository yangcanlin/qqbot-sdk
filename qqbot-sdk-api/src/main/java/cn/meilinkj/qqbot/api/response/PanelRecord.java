package cn.meilinkj.qqbot.api.response;

import cn.meilinkj.qqbot.api.request.Panel;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 指令面板记录（官方 GET /v2/panels 列表项与 GET /v2/panels/{panel_id} 详情的共同结构）。
 * user_openids / group_openids 仅详情接口在 specific 面板时返回，最多 1000 条。
 */
public class PanelRecord {

    @JsonProperty("panel_id")
    private String panelId;

    /** 生效场景：c2c / group / channel / dm */
    @JsonProperty("scope")
    private String scope;

    /** 生效范围：all / specific */
    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("panel")
    private Panel panel;

    /** 创建时间，RFC3339 */
    @JsonProperty("created_at")
    private String createdAt;

    /** 更新时间，RFC3339 */
    @JsonProperty("updated_at")
    private String updatedAt;

    @JsonProperty("version")
    private Integer version;

    /** 仅 c2c 场景 + target_type=specific 的详情接口返回 */
    @JsonProperty("user_openids")
    private List<String> userOpenids;

    /** 仅 group 场景 + target_type=specific 的详情接口返回 */
    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    public String getPanelId() {
        return panelId;
    }

    public String getScope() {
        return scope;
    }

    public String getTargetType() {
        return targetType;
    }

    public Panel getPanel() {
        return panel;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public Integer getVersion() {
        return version;
    }

    public List<String> getUserOpenids() {
        return userOpenids;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }
}
