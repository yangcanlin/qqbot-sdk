package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 频道成员对象 Member。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/member/model.html#member">成员对象(Member)</a>
 */
public class Member {

    /** 用户的频道基础信息，只有成员相关接口中会填充 */
    @JsonProperty("user")
    private User user;

    @JsonProperty("nick")
    private String nick;

    /** 用户在频道内的身份组 ID */
    @JsonProperty("roles")
    private List<String> roles;

    /** ISO8601 timestamp，用户加入频道的时间 */
    @JsonProperty("joined_at")
    private String joinedAt;

    public User getUser() {
        return user;
    }

    public Member setUser(User user) {
        this.user = user;
        return this;
    }

    public String getNick() {
        return nick;
    }

    public Member setNick(String nick) {
        this.nick = nick;
        return this;
    }

    public List<String> getRoles() {
        return roles;
    }

    public Member setRoles(List<String> roles) {
        this.roles = roles;
        return this;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public Member setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
        return this;
    }
}