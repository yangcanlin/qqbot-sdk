package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 群黑名单操作请求（批量添加/删除）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.post.html">添加/删除群黑名单</a>
 *
 * <p>单次最多 20 个成员。</p>
 */
public class BlacklistUserRequest {

    public static final int MAX_MEMBER_OPENIDS = 20;

    @JsonProperty("op")
    private String op;

    @JsonProperty("member_openids")
    private List<String> memberOpenids;

    public BlacklistUserRequest(String op, List<String> memberOpenids) {
        if (memberOpenids == null || memberOpenids.isEmpty()) {
            throw new IllegalArgumentException("memberOpenids 不能为空");
        }
        if (memberOpenids.size() > MAX_MEMBER_OPENIDS) {
            throw new IllegalArgumentException("单次最多操作 " + MAX_MEMBER_OPENIDS + " 个成员");
        }
        this.op = op;
        this.memberOpenids = memberOpenids;
    }

    public static BlacklistUserRequest add(List<String> memberOpenids) {
        return new BlacklistUserRequest("add", memberOpenids);
    }

    public static BlacklistUserRequest remove(List<String> memberOpenids) {
        return new BlacklistUserRequest("del", memberOpenids);
    }

    public String getOp() {
        return op;
    }

    public List<String> getMemberOpenids() {
        return memberOpenids;
    }
}
