package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 入群申请。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_join_request_list.get.html">入群申请列表拉取</a>
 */
public class JoinRequest {

    /** 申请 ID，审批时回传 */
    @JsonProperty("join_request_id")
    private String joinRequestId;

    /** 风险提示文案列表 */
    @JsonProperty("risk_tips")
    private List<String> riskTips;

    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 申请成员 openid，审批接口路径参数 */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 申请人昵称 */
    @JsonProperty("username")
    private String username;

    /** 申请时间（RFC3339 格式） */
    @JsonProperty("apply_at")
    private String applyAt;

    /** 申请来源：self_apply 主动申请 / invited 群成员邀请 */
    @JsonProperty("apply_source")
    private String applySource;

    /** 邀请人 openid（apply_source=invited 时） */
    @JsonProperty("invited_by")
    private String invitedBy;

    /** 申请人是否机器人 */
    @JsonProperty("bot")
    private Boolean bot;

    /** 验证信息 */
    @JsonProperty("verify_info")
    private VerifyInfo verifyInfo;

    public String getJoinRequestId() {
        return joinRequestId;
    }

    public List<String> getRiskTips() {
        return riskTips;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public String getUsername() {
        return username;
    }

    public String getApplyAt() {
        return applyAt;
    }

    public String getApplySource() {
        return applySource;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public Boolean isBot() {
        return bot;
    }

    public VerifyInfo getVerifyInfo() {
        return verifyInfo;
    }

    /** 验证信息：method=verify_message 验证消息 / admin_review_qa 管理员问答题。 */
    public static class VerifyInfo {

        @JsonProperty("method")
        private String method;

        /** 验证消息内容（method=verify_message 时） */
        @JsonProperty("verify_message")
        private String verifyMessage;

        /** 问答题列表（method=admin_review_qa 时） */
        @JsonProperty("review_qa_list")
        private List<ReviewQa> reviewQaList;

        public String getMethod() {
            return method;
        }

        public String getVerifyMessage() {
            return verifyMessage;
        }

        public List<ReviewQa> getReviewQaList() {
            return reviewQaList;
        }
    }

    /** 问答题。 */
    public static class ReviewQa {

        @JsonProperty("question")
        private String question;

        @JsonProperty("answer")
        private String answer;

        public String getQuestion() {
            return question;
        }

        public String getAnswer() {
            return answer;
        }
    }
}
