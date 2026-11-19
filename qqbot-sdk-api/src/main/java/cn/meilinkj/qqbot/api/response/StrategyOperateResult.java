package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 入群自动审批策略操作结果（创建/修改策略响应）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.post.html">创建入群自动审批策略</a>
 */
public class StrategyOperateResult {

    /** 策略 ID（创建接口返回） */
    @JsonProperty("strategy_id")
    private String strategyId;

    /** 是否启用：on / off */
    @JsonProperty("is_enable")
    private String isEnable;

    /** 失效时间（RFC3339 格式） */
    @JsonProperty("expire_at")
    private String expireAt;

    public String getStrategyId() {
        return strategyId;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public String getExpireAt() {
        return expireAt;
    }
}
