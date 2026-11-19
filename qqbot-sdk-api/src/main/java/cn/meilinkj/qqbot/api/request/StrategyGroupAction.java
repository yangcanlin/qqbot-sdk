package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 入群自动审批策略的关联群增删操作（官方 GroupAction）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.patch.html">修改入群自动审批策略</a>
 *
 * <p>群标识形式须与创建策略时一致；groupOpenids 与 groupIds 互斥。</p>
 */
public class StrategyGroupAction {

    @JsonProperty("op")
    private String op;

    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    /** QQ 群号列表（uint64），以字符串传入避免精度问题 */
    @JsonProperty("group_ids")
    private List<String> groupIds;

    private StrategyGroupAction(String op) {
        this.op = op;
    }

    public static StrategyGroupAction addOpenids(List<String> groupOpenids) {
        StrategyGroupAction action = new StrategyGroupAction("add");
        action.groupOpenids = check("groupOpenids", groupOpenids);
        return action;
    }

    public static StrategyGroupAction removeOpenids(List<String> groupOpenids) {
        StrategyGroupAction action = new StrategyGroupAction("del");
        action.groupOpenids = check("groupOpenids", groupOpenids);
        return action;
    }

    public static StrategyGroupAction addGroupIds(List<String> groupIds) {
        StrategyGroupAction action = new StrategyGroupAction("add");
        action.groupIds = check("groupIds", groupIds);
        return action;
    }

    public static StrategyGroupAction removeGroupIds(List<String> groupIds) {
        StrategyGroupAction action = new StrategyGroupAction("del");
        action.groupIds = check("groupIds", groupIds);
        return action;
    }

    private static List<String> check(String name, List<String> value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value;
    }

    public String getOp() {
        return op;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }
}
