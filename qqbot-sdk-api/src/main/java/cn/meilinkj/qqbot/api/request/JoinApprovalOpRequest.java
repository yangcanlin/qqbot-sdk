package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 入群申请审批请求。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_approval_join_request_member_openid.post.html">入群申请审批</a>
 *
 * <p>joinRequestId 来自入群申请列表接口；机器人需群管理员身份。</p>
 */
public class JoinApprovalOpRequest {

    /** approve 通过 / decline 拒绝 */
    @JsonProperty("op")
    private String op;

    @JsonProperty("join_request_id")
    private String joinRequestId;

    /** 拒绝理由，op=decline 时可填 */
    @JsonProperty("reject_reason")
    private String rejectReason;

    /** 是否同时加入群黑名单，默认 false，op=decline 时可填 */
    @JsonProperty("add_to_member_blacklist")
    private Boolean addToMemberBlacklist;

    public static JoinApprovalOpRequest approve(String joinRequestId) {
        JoinApprovalOpRequest req = new JoinApprovalOpRequest();
        req.op = "approve";
        req.joinRequestId = joinRequestId;
        return req;
    }

    public static JoinApprovalOpRequest decline(String joinRequestId, String rejectReason, boolean addToBlacklist) {
        JoinApprovalOpRequest req = new JoinApprovalOpRequest();
        req.op = "decline";
        req.joinRequestId = joinRequestId;
        req.rejectReason = rejectReason;
        req.addToMemberBlacklist = addToBlacklist;
        return req;
    }

    public String getOp() {
        return op;
    }

    public String getJoinRequestId() {
        return joinRequestId;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public Boolean getAddToMemberBlacklist() {
        return addToMemberBlacklist;
    }
}
