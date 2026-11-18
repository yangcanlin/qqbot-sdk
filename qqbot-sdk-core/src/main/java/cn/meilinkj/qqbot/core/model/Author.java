package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 群聊/单聊消息发送者。群场景 member_openid 有值，单聊场景 user_openid 有值。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html">群@机器人消息-GroupUser</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_message_create.html">单聊消息事件-C2CUser</a>
 */
public class Author {

    /** 用户唯一标识（OpenID 格式） */
    @JsonProperty("id")
    private String id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("bot")
    private Boolean bot;

    /** 跨应用关联用户 openid，绑定三方应用时有效 */
    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 群成员 OpenID（群聊场景） */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 群内角色：member=普通成员, admin=管理员, owner=群主 */
    @JsonProperty("member_role")
    private String memberRole;

    /** 用户在群内的昵称 */
    @JsonProperty("member_nickname")
    private String memberNickname;

    /** 用户 OpenID（单聊场景） */
    @JsonProperty("user_openid")
    private String userOpenid;

    public String getId() {
        return id;
    }

    public Author setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public Author setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public Author setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Boolean getBot() {
        return bot;
    }

    public Author setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public Author setUnionOpenid(String unionOpenid) {
        this.unionOpenid = unionOpenid;
        return this;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public Author setMemberOpenid(String memberOpenid) {
        this.memberOpenid = memberOpenid;
        return this;
    }

    public String getMemberRole() {
        return memberRole;
    }

    public Author setMemberRole(String memberRole) {
        this.memberRole = memberRole;
        return this;
    }

    public String getMemberNickname() {
        return memberNickname;
    }

    public Author setMemberNickname(String memberNickname) {
        this.memberNickname = memberNickname;
        return this;
    }

    public String getUserOpenid() {
        return userOpenid;
    }

    public Author setUserOpenid(String userOpenid) {
        this.userOpenid = userOpenid;
        return this;
    }
}