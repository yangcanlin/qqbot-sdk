package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 创建入群自动审批策略请求。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.post.html">创建入群自动审批策略</a>
 *
 * <p>groupOpenids 与 groupIds 互斥且必传其一，各最多 100 个；最多 20 个策略。</p>
 */
public class JoinApprovalStrategyRequest {

    public static final int MAX_GROUPS = 100;
    public static final int MAX_REMARK_LENGTH = 255;

    @JsonProperty("group_openids")
    private List<String> groupOpenids;

    /** QQ 群号列表（uint64），以字符串传入避免精度问题 */
    @JsonProperty("group_ids")
    private List<String> groupIds;

    /** on 启用 / off 关闭，默认 on */
    @JsonProperty("is_enable")
    private String isEnable;

    /** 失效时间（RFC3339 格式），默认一年后 */
    @JsonProperty("expire_at")
    private String expireAt;

    /** 策略备注，最多 255 个汉字 */
    @JsonProperty("remark")
    private String remark;

    public static JoinApprovalStrategyRequest ofGroupOpenids(List<String> groupOpenids) {
        JoinApprovalStrategyRequest req = new JoinApprovalStrategyRequest();
        req.groupOpenids = requireExactlyOne(groupOpenids, "groupOpenids", "groupIds", req.groupIds);
        return req;
    }

    public static JoinApprovalStrategyRequest ofGroupIds(List<String> groupIds) {
        JoinApprovalStrategyRequest req = new JoinApprovalStrategyRequest();
        req.groupIds = requireExactlyOne(groupIds, "groupIds", "groupOpenids", req.groupOpenids);
        return req;
    }

    private static List<String> requireExactlyOne(List<String> value, String name, String otherName, List<String> other) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("groupOpenids 与 groupIds 必传其一");
        }
        if (other != null && !other.isEmpty()) {
            throw new IllegalArgumentException(name + " 与 " + otherName + " 互斥");
        }
        if (value.size() > MAX_GROUPS) {
            throw new IllegalArgumentException(name + " 单次最多 " + MAX_GROUPS + " 个");
        }
        return value;
    }

    public JoinApprovalStrategyRequest setIsEnable(String isEnable) {
        this.isEnable = isEnable;
        return this;
    }

    public JoinApprovalStrategyRequest setExpireAt(String expireAt) {
        this.expireAt = expireAt;
        return this;
    }

    public JoinApprovalStrategyRequest setRemark(String remark) {
        if (remark != null && remark.length() > MAX_REMARK_LENGTH) {
            throw new IllegalArgumentException("remark 最多 " + MAX_REMARK_LENGTH + " 个字符");
        }
        this.remark = remark;
        return this;
    }

    public List<String> getGroupOpenids() {
        return groupOpenids;
    }

    public List<String> getGroupIds() {
        return groupIds;
    }

    public String getIsEnable() {
        return isEnable;
    }

    public String getExpireAt() {
        return expireAt;
    }

    public String getRemark() {
        return remark;
    }
}
