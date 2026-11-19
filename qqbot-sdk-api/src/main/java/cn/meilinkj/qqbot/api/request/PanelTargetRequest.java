package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * 修改指令面板关联对象请求体（官方 PUT /v2/panels/{panel_id}/target）。
 *
 * <p>官方约束：仅 target_type=specific 的面板可操作（全局面板返回 err_code=40030021）；
 * c2c 场景操作 user_openids、group 场景操作 group_openids，一次最多 20 个。</p>
 */
public class PanelTargetRequest {

    /** 添加关联对象 */
    public static final String OP_ADD = "add";
    /** 移除关联对象 */
    public static final String OP_DEL = "del";

    private static final int MAX_TARGET_SIZE = 20;

    @JsonProperty("op")
    private String op;

    @JsonProperty("user_openids")
    private List<String> userOpenids;

    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    public static PanelTargetRequest addUsers(String... userOpenids) {
        return addUsers(Arrays.asList(userOpenids));
    }

    public static PanelTargetRequest addUsers(List<String> userOpenids) {
        return of(OP_ADD, userOpenids, null);
    }

    public static PanelTargetRequest delUsers(List<String> userOpenids) {
        return of(OP_DEL, userOpenids, null);
    }

    public static PanelTargetRequest addGroups(String... groupOpenids) {
        return addGroups(Arrays.asList(groupOpenids));
    }

    public static PanelTargetRequest addGroups(List<String> groupOpenids) {
        return of(OP_ADD, null, groupOpenids);
    }

    public static PanelTargetRequest delGroups(List<String> groupOpenids) {
        return of(OP_DEL, null, groupOpenids);
    }

    private static PanelTargetRequest of(String op, List<String> userOpenids, List<String> groupOpenids) {
        return new PanelTargetRequest()
                .setOp(op)
                .setUserOpenids(requireList(userOpenids))
                .setGroupOpenids(requireList(groupOpenids));
    }

    private static List<String> requireList(List<String> openids) {
        if (openids == null) {
            return null;
        }
        if (openids.isEmpty()) {
            throw new IllegalArgumentException("关联对象列表不能为空");
        }
        if (openids.size() > MAX_TARGET_SIZE) {
            throw new IllegalArgumentException("官方限制一次最多传 " + MAX_TARGET_SIZE + " 个 openid，当前 " + openids.size() + " 个");
        }
        return openids;
    }

    public String getOp() {
        return op;
    }

    public PanelTargetRequest setOp(String op) {
        if (!OP_ADD.equals(op) && !OP_DEL.equals(op)) {
            throw new IllegalArgumentException("官方 op 仅支持 add / del（err_code=40030017），当前: " + op);
        }
        this.op = op;
        return this;
    }

    public List<String> getUserOpenids() {
        return userOpenids;
    }

    public PanelTargetRequest setUserOpenids(List<String> userOpenids) {
        this.userOpenids = userOpenids;
        return this;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }

    public PanelTargetRequest setGroupOpenids(List<String> groupOpenids) {
        this.groupOpenids = groupOpenids;
        return this;
    }
}
