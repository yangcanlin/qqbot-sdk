package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 设置群成员禁言请求。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.post.html">设置群成员禁言</a>
 *
 * <p>单次最多 20 个成员，禁言最长 30 天；机器人需群管理员/群主身份。</p>
 */
public class MuteSettingRequest {

    public static final int MAX_MEMBER_OPS = 20;

    @JsonProperty("members")
    private List<MemberOp> members;

    /** 禁言至指定到期时间（RFC3339 格式，op=add）。 */
    public static MuteSettingRequest muteTo(String memberOpenid, String muteExpireAt) {
        return new MuteSettingRequest().addMemberOp("add", memberOpenid, muteExpireAt);
    }

    /** 立即解除禁言（op=del，mute_expire_at 传空串）。 */
    public static MuteSettingRequest unmute(String memberOpenid) {
        return new MuteSettingRequest().addMemberOp("del", memberOpenid, "");
    }

    public MuteSettingRequest addMemberOp(String op, String memberOpenid, String muteExpireAt) {
        if (members == null) {
            members = new ArrayList<>();
        }
        if (members.size() >= MAX_MEMBER_OPS) {
            throw new IllegalArgumentException("单次最多操作 " + MAX_MEMBER_OPS + " 个成员");
        }
        members.add(new MemberOp(op, memberOpenid, muteExpireAt));
        return this;
    }

    public List<MemberOp> getMembers() {
        return members;
    }

    /** 单个成员禁言操作。 */
    public static class MemberOp {

        /** add 禁言 / update 修改时长 / del 解除禁言 */
        @JsonProperty("op")
        private String op;

        @JsonProperty("member_openid")
        private String memberOpenid;

        /** 禁言到期时间（RFC3339）；op=del 时传空串表示立即解除 */
        @JsonProperty("mute_expire_at")
        private String muteExpireAt;

        public MemberOp(String op, String memberOpenid, String muteExpireAt) {
            this.op = op;
            this.memberOpenid = memberOpenid;
            this.muteExpireAt = muteExpireAt;
        }

        public String getOp() {
            return op;
        }

        public String getMemberOpenid() {
            return memberOpenid;
        }

        public String getMuteExpireAt() {
            return muteExpireAt;
        }
    }
}
