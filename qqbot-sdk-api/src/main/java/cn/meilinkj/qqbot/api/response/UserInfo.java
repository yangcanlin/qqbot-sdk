package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 用户基本信息。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_info.get.html">获取用户基本信息</a>
 */
public class UserInfo {

    /** 跨应用关联用户 openid，绑定三方应用时有效 */
    @JsonProperty("union_openid")
    private String unionOpenid;

    /** 用户名 */
    @JsonProperty("username")
    private String username;

    /** 用户头像原图 */
    @JsonProperty("avatar")
    private String avatar;

    /** 是否接受主动推送 */
    @JsonProperty("allow_proactive_msg")
    private Boolean allowProactiveMsg;

    public String getUnionOpenid() {
        return unionOpenid;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public Boolean getAllowProactiveMsg() {
        return allowProactiveMsg;
    }
}
