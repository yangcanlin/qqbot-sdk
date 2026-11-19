package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 入群自动审批策略。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.get.html">查询入群自动审批策略列表</a>
 */
public class JoinApprovalStrategy {

    /** 策略 ID */
    @JsonProperty("strategy_id")
    private String strategyId;

    /** 关联群 openid 列表（与 groupIds 互斥，形式与创建时一致） */
    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    /** 关联 QQ 群号列表（uint64 字符串） */
    @JsonProperty("group_ids")
    private List<String> groupIds;

    /** 白名单号码数量 */
    @JsonProperty("whitelist_user_count")
    private Integer whitelistUserCount;

    /** 是否启用：on / off */
    @JsonProperty("is_enable")
    private String isEnable;

    /** 失效时间（RFC3339 格式） */
    @JsonProperty("expire_at")
    private String expireAt;

    @JsonProperty("created_at")
    private String createdAt;

    @JsonProperty("updated_at")
    private String updatedAt;

    /** 策略备注 */
    @JsonProperty("remark")
    private String remark;

    public String getStrategyId() {
        return strategyId;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public Integer getWhitelistUserCount() {
        return whitelistUserCount;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getRemark() {
        return remark;
    }
}
