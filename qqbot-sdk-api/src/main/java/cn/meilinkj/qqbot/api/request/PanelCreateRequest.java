package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建指令面板请求体（官方 POST /v2/panels）。
 *
 * <p>官方约束：channel / dm 场景只支持 target_type=all；specific 时 user_openids（c2c）
 * 或 group_openids（group）一次最多 20 个，后续用「修改指令面板关联对象」接口增删。</p>
 */
public class PanelCreateRequest {

    /** 单聊场景 */
    public static final String SCOPE_C2C = "c2c";
    /** 群聊场景 */
    public static final String SCOPE_GROUP = "group";
    /** 文字子频道场景 */
    public static final String SCOPE_CHANNEL = "channel";
    /** 频道私信场景 */
    public static final String SCOPE_DM = "dm";

    /** 对该场景下所有用户 / 群生效 */
    public static final String TARGET_ALL = "all";
    /** 仅对指定用户 / 群生效 */
    public static final String TARGET_SPECIFIC = "specific";

    private static final int MAX_TARGET_SIZE = 20;

    @JsonProperty("scope")
    private String scope;

    /** 生效范围：all / specific */
    @JsonProperty("target_type")
    private String targetType;

    @JsonProperty("user_openids")
    private List<String> userOpenids;

    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    @JsonProperty("panel")
    private Panel panel;

    /** 全局面板：官方要求 channel / dm 场景只能使用此方式。 */
    public static PanelCreateRequest global(String scope, Panel panel) {
        return new PanelCreateRequest().setScope(scope).setTargetType(TARGET_ALL).setPanel(requirePanel(panel));
    }

    /** 单聊指定用户面板（target_type=specific）。 */
    public static PanelCreateRequest forUsers(Panel panel, List<String> userOpenids) {
        return new PanelCreateRequest()
                .setScope(SCOPE_C2C)
                .setTargetType(TARGET_SPECIFIC)
                .setUserOpenids(requireTargets(userOpenids))
                .setPanel(requirePanel(panel));
    }

    /** 群聊指定群面板（target_type=specific）。 */
    public static PanelCreateRequest forGroups(Panel panel, List<String> groupOpenids) {
        return new PanelCreateRequest()
                .setScope(SCOPE_GROUP)
                .setTargetType(TARGET_SPECIFIC)
                .setGroupOpenids(requireTargets(groupOpenids))
                .setPanel(requirePanel(panel));
    }

    static Panel requirePanel(Panel panel) {
        if (panel == null) {
            throw new IllegalArgumentException("panel 不能为空（官方必填字段）");
        }
        if (panel.getItems() != null && panel.getItems().size() > 20) {
            throw new IllegalArgumentException("官方限制一个面板最多 20 个面板元素，当前 " + panel.getItems().size() + " 个");
        }
        return panel;
    }

    private static List<String> requireTargets(List<String> openids) {
        if (openids == null || openids.isEmpty()) {
            throw new IllegalArgumentException("target_type=specific 时必须提供至少 1 个 openid");
        }
        if (openids.size() > MAX_TARGET_SIZE) {
            throw new IllegalArgumentException("官方限制一次最多传 " + MAX_TARGET_SIZE + " 个 openid，当前 " + openids.size() + " 个");
        }
        return openids;
    }

    public String getScope() {
        return scope;
    }

    public PanelCreateRequest setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getTargetType() {
        return targetType;
    }

    public PanelCreateRequest setTargetType(String targetType) {
        if (TARGET_SPECIFIC.equals(targetType)
                && (SCOPE_CHANNEL.equals(scope) || SCOPE_DM.equals(scope))) {
            throw new IllegalArgumentException("官方限制 channel / dm 场景仅支持 target_type=all（err_code=40030012）");
        }
        this.targetType = targetType;
        return this;
    }

    public List<String> getUserOpenids() {
        return userOpenids;
    }

    public PanelCreateRequest setUserOpenids(List<String> userOpenids) {
        this.userOpenids = userOpenids;
        return this;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }

    public PanelCreateRequest setGroupOpenids(List<String> groupOpenids) {
        this.groupOpenids = groupOpenids;
        return this;
    }

    public Panel getPanel() {
        return panel;
    }

    public PanelCreateRequest setPanel(Panel panel) {
        this.panel = panel;
        return this;
    }
}
