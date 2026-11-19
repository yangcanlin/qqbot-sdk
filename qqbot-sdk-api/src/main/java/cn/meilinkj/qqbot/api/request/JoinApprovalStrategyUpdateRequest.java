package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 修改入群自动审批策略请求（局部更新）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.patch.html">修改入群自动审批策略</a>
 */
public class JoinApprovalStrategyUpdateRequest {

    public static final int MAX_REMARK_LENGTH = 255;

    /** on 启用 / off 关闭 */
    @JsonProperty("is_enable")
    private String isEnable;

    /** 过期时间（RFC3339 格式） */
    @JsonProperty("expire_at")
    private String expireAt;

    /** 关联群增删操作 */
    @JsonProperty("group_action")
    private StrategyGroupAction groupAction;

    /** 策略备注，最多 255 个汉字 */
    @JsonProperty("remark")
    private String remark;

    public JoinApprovalStrategyUpdateRequest setIsEnable(String isEnable) {
        this.isEnable = isEnable;
        return this;
    }

    public JoinApprovalStrategyUpdateRequest setExpireAt(String expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public JoinApprovalStrategyUpdateRequest setGroupAction(StrategyGroupAction groupAction) {
        this.groupAction = groupAction;
        return this;
    }

    public JoinApprovalStrategyUpdateRequest setRemark(String remark) {
        if (remark != null && remark.length() > MAX_REMARK_LENGTH) {
            throw new IllegalArgumentException("remark 最多 " + MAX_REMARK_LENGTH + " 个字符");
        }
        this.remark = remark;
        return this;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public StrategyGroupAction getGroupAction() {
        return groupAction;
    }

    public String getRemark() {
        return remark;
    }
}
