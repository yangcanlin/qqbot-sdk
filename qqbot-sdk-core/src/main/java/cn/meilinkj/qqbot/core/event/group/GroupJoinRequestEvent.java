package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

import java.util.List;

/**
 * GROUP_JOIN_REQUEST（Intents.GROUP_MEMBER_EVENT）：用户申请加群。
 * 仅当机器人群管理员时才会收到；join_request_id 需回传给入群申请审批接口
 * （api 模块 GroupManageApi#approveJoinRequest）。
 *
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_join_request.html">用户申请加群事件</a>
 */
public class GroupJoinRequestEvent extends BotEvent {

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 申请 ID，审批接口回传用 */
    @JsonProperty("join_request_id")
    private String joinRequestId;

    /** 安全提示语；可疑消息为 warning_tips，普通消息命中风险规则为 top_tips */
    @JsonProperty("risk_tips")
    private String riskTips;

    /** 用户在应用/开放平台下的统一标识（如有） */
    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 申请人 openid */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 申请人昵称 */
    @JsonProperty("username")
    private String username;

    /** 申请时间（RFC3339） */
    @JsonProperty("apply_at")
    private String applyAt;

    /** 申请来源：self_apply 主动申请 / invited 被邀请 */
    @JsonProperty("apply_source")
    private String applySource;

    /** 邀请人 openid，apply_source=invited 时有效 */
    @JsonProperty("invited_by")
    private String invitedBy;

    /** 是否为机器人账号 */
    @JsonProperty("bot")
    private Boolean bot;

    @JsonProperty("verify_info")
    private VerifyInfo verifyInfo;

    /** 自动审批通过的扩展信息，仅下行事件携带 */
    @JsonProperty("auto_approved")
    private AutoApproved autoApproved;

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupJoinRequestEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getJoinRequestId() {
        return joinRequestId;
    }

    public GroupJoinRequestEvent setJoinRequestId(String joinRequestId) {
        this.joinRequestId = joinRequestId;
        return this;
    }

    public String getRiskTips() {
        return riskTips;
    }

    public GroupJoinRequestEvent setRiskTips(String riskTips) {
        this.riskTips = riskTips;
        return this;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public GroupJoinRequestEvent setUnionOpenid(String unionOpenid) {
        this.unionOpenid = unionOpenid;
        return this;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public GroupJoinRequestEvent setMemberOpenid(String memberOpenid) {
        this.memberOpenid = memberOpenid;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public GroupJoinRequestEvent setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getApplyAt() {
        return applyAt;
    }

    public GroupJoinRequestEvent setApplyAt(String applyAt) {
        this.applyAt = applyAt;
        return this;
    }

    public String getApplySource() {
        return applySource;
    }

    public GroupJoinRequestEvent setApplySource(String applySource) {
        this.applySource = applySource;
        return this;
    }

    public String getInvitedBy() {
        return invitedBy;
    }

    public GroupJoinRequestEvent setInvitedBy(String invitedBy) {
        this.invitedBy = invitedBy;
        return this;
    }

    public Boolean getBot() {
        return bot;
    }

    public GroupJoinRequestEvent setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    public VerifyInfo getVerifyInfo() {
        return verifyInfo;
    }

    public GroupJoinRequestEvent setVerifyInfo(VerifyInfo verifyInfo) {
        this.verifyInfo = verifyInfo;
        return this;
    }

    public AutoApproved getAutoApproved() {
        return autoApproved;
    }

    public GroupJoinRequestEvent setAutoApproved(AutoApproved autoApproved) {
        this.autoApproved = autoApproved;
        return this;
    }

    /** 用户入群验证方式 */
    public static class VerifyInfo {

        /** verify_message 验证消息 / admin_review_qa 管理员问答 */
        @JsonProperty("method")
        private String method;

        /** 验证消息内容，method=verify_message 时可能携带 */
        @JsonProperty("verify_message")
        private String verifyMessage;

        /** 问答列表，method=admin_review_qa 时可能携带 */
        @JsonProperty("review_qa_list")
        private List<ReviewQa> reviewQaList;

        public String getMethod() {
            return method;
        }

        public VerifyInfo setMethod(String method) {
            this.method = method;
            return this;
        }

        public String getVerifyMessage() {
            return verifyMessage;
        }

        public VerifyInfo setVerifyMessage(String verifyMessage) {
            this.verifyMessage = verifyMessage;
            return this;
        }

        public List<ReviewQa> getReviewQaList() {
            return reviewQaList;
        }

        public VerifyInfo setReviewQaList(List<ReviewQa> reviewQaList) {
            this.reviewQaList = reviewQaList;
            return this;
        }
    }

    /** 管理员设置的问答 */
    public static class ReviewQa {

        @JsonProperty("question")
        private String question;

        @JsonProperty("answer")
        private String answer;

        public String getQuestion() {
            return question;
        }

        public ReviewQa setQuestion(String question) {
            this.question = question;
            return this;
        }

        public String getAnswer() {
            return answer;
        }

        public ReviewQa setAnswer(String answer) {
            this.answer = answer;
            return this;
        }
    }

    /** 自动审批通过信息 */
    public static class AutoApproved {

        @JsonProperty("strategy_id")
        private String strategyId;

        public String getStrategyId() {
            return strategyId;
        }

        public AutoApproved setStrategyId(String strategyId) {
            this.strategyId = strategyId;
            return this;
        }
    }
}
