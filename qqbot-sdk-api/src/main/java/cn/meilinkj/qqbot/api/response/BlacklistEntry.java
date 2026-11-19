package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 群黑名单成员。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.get.html">获取群黑名单列表</a>
 */
public class BlacklistEntry {

    /** 成员 union openid */
    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 成员 openid */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 成员昵称 */
    @JsonProperty("username")
    private String username;

    /** 拉黑时间（RFC3339 格式） */
    @JsonProperty("banned_at")
    private String bannedAt;

    /** 是否机器人 */
    @JsonProperty("bot")
    private Boolean bot;

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public String getUsername() {
        return username;
    }

    public String getBannedAt() {
        return bannedAt;
    }

    public Boolean isBot() {
        return bot;
    }
}
