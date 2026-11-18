package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 群消息中@的用户（不含@机器人自身）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html">群@机器人消息-GroupMentionsUser</a>
 */
public class MentionsUser {

    @JsonProperty("id")
    private String id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("bot")
    private Boolean bot;

    @JsonProperty("member_openid")
    private String memberOpenid;

    /** member=普通成员, admin=管理员, owner=群主 */
    @JsonProperty("member_role")
    private String memberRole;

    /** 是否是@的当前 bot */
    @JsonProperty("is_you")
    private Boolean isYou;

    /** all=@全体成员，single=指定@对象 */
    @JsonProperty("scope")
    private String scope;

    @JsonProperty("member_nickname")
    private String memberNickname;

    public String getId() {
        return id;
    }

    public MentionsUser setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public MentionsUser setUsername(String username) {
        this.username = username;
        return this;
    }

    public Boolean getBot() {
        return bot;
    }

    public MentionsUser setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public MentionsUser setMemberOpenid(String memberOpenid) {
        this.memberOpenid = memberOpenid;
        return this;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public MentionsUser setMemberRole(String memberRole) {
        this.memberRole = memberRole;
        return this;
    }

    public Boolean getIsYou() {
        return isYou;
    }

    public MentionsUser setIsYou(Boolean isYou) {
        this.isYou = isYou;
        return this;
    }

    public String getScope() {
        return scope;
    }

    public MentionsUser setScope(String scope) {
        this.scope = scope;
        return this;
    }

    public String getMemberNickname() {
        return memberNickname;
    }

    public MentionsUser setMemberNickname(String memberNickname) {
        this.memberNickname = memberNickname;
        return this;
    }
}