package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 频道用户对象 User。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/user/model.html#user">用户对象(User)</a>
 *
 * <p>union_openid 与 union_user_account 仅在单独拉取 member 信息时提供。</p>
 */
public class User {

    @JsonProperty("id")
    private String id;

    @JsonProperty("username")
    private String username;

    @JsonProperty("avatar")
    private String avatar;

    @JsonProperty("bot")
    private Boolean bot;

    @JsonProperty("union_openid")
    private String unionOpenid;

    @JsonProperty("union_user_account")
    private String unionUserAccount;

    public String getId() {
        return id;
    }

    public User setId(String id) {
        this.id = id;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public User setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getAvatar() {
        return avatar;
    }

    public User setAvatar(String avatar) {
        this.avatar = avatar;
        return this;
    }

    public Boolean getBot() {
        return bot;
    }

    public User setBot(Boolean bot) {
        this.bot = bot;
        return this;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public User setUnionOpenid(String unionOpenid) {
        this.unionOpenid = unionOpenid;
        return this;
    }

    public String getUnionUserAccount() {
        return unionUserAccount;
    }

    public User setUnionUserAccount(String unionUserAccount) {
        this.unionUserAccount = unionUserAccount;
        return this;
    }
}