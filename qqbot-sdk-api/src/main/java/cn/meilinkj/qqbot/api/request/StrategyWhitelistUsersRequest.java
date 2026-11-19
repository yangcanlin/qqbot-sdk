package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 修改入群自动审批策略白名单号码请求。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_whitelist_users.post.html">修改入群自动审批策略的白名单号码</a>
 *
 * <p>单次最多 10000 个号码，策略白名单总量上限 10W；号码用字符串避免精度问题。</p>
 */
public class StrategyWhitelistUsersRequest {

    public static final int MAX_USERS_PER_CALL = 10000;

    /** add 新增号码 / del 删除号码 */
    @JsonProperty("op")
    private String op;

    @JsonProperty("whitelist_users")
    private List<String> whitelistUsers;

    public StrategyWhitelistUsersRequest(String op, List<String> whitelistUsers) {
        if (whitelistUsers == null || whitelistUsers.isEmpty()) {
            throw new IllegalArgumentException("whitelistUsers 不能为空");
        }
        if (whitelistUsers.size() > MAX_USERS_PER_CALL) {
            throw new IllegalArgumentException("单次最多操作 " + MAX_USERS_PER_CALL + " 个号码");
        }
        this.op = op;
        this.whitelistUsers = whitelistUsers;
    }

    public static StrategyWhitelistUsersRequest add(List<String> qqNumbers) {
        return new StrategyWhitelistUsersRequest("add", qqNumbers);
    }

    public static StrategyWhitelistUsersRequest remove(List<String> qqNumbers) {
        return new StrategyWhitelistUsersRequest("del", qqNumbers);
    }

    public String getOp() {
        return op;
    }

    public List<String> getWhitelistUsers() {
        return whitelistUsers;
    }
}
