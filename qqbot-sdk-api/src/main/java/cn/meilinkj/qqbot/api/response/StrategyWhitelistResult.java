package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 入群自动审批策略白名单操作结果。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_whitelist_users.post.html">修改入群自动审批策略的白名单号码</a>
 */
public class StrategyWhitelistResult {

    @JsonProperty("strategy_id")
    private String strategyId;

    /** 操作后策略当前白名单号码数（估算） */
    @JsonProperty("whitelist_user_count")
    private Integer whitelistUserCount;

    /** 策略更新时间（RFC3339 格式） */
    @JsonProperty("updated_at")
    private String updatedAt;

    public String getStrategyId() {
        return strategyId;
    }

    public Integer getWhitelistUserCount() {
        return whitelistUserCount;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }
}
