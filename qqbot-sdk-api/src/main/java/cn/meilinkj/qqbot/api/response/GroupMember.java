package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 群成员信息。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members.get.html">获取群成员列表</a>
 */
public class GroupMember {

    /** 成员 openid */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 成员昵称 */
    @JsonProperty("username")
    private String username;

    /** 群内角色：member=普通成员 admin=管理员 owner=群主（官方取值口径） */
    @JsonProperty("member_role")
    private String memberRole;

    /** 是否机器人 */
    @JsonProperty("bot")
    private Boolean bot;

    /** 入群时间（RFC3339 格式） */
    @JsonProperty("joined_at")
    private String joinedAt;

    /** 成员 union openid */
    @JsonProperty("union_openid")
    private String unionOpenid;

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public String getUsername() {
        return username;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public Boolean isBot() {
        return bot;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }
}
