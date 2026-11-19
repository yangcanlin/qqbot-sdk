package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 机器人详情（GET /users/@me，50 QPS）。
 * union_openid / union_user_account 需特殊申请并配置后才会返回。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me.get.html">获取机器人详情</a>
 */
public class BotInfo {

    /** 机器人用户 ID */
    @JsonProperty("id")
    private String id;

    /** 机器人名称 */
    @JsonProperty("username")
    private String username;

    /** 头像 URL */
    @JsonProperty("avatar")
    private String avatar;

    /** 是否为机器人 */
    @JsonProperty("bot")
    private Boolean bot;

    /** 跨应用统一用户 OpenID（需特殊申请） */
    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 跨应用统一用户账号（需特殊申请） */
    @JsonProperty("union_user_account")
    private String unionUserAccount;

    /** 机器人分享链接（官方响应示例提供） */
    @JsonProperty("share_url")
    private String shareUrl;

    /** 欢迎语（官方响应示例提供） */
    @JsonProperty("welcome_msg")
    private String welcomeMsg;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean isBot() {
        return bot;
    }

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public String getUnionUserAccount() {
        return unionUserAccount;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public String getWelcomeMsg() {
        return welcomeMsg;
    }
}
