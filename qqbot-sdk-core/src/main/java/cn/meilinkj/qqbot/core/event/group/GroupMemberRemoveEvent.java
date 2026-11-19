package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * GROUP_MEMBER_REMOVE（Intents.GROUP_MEMBER_EVENT）：群成员退出或被移出群聊。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_member_remove.html">群成员退出</a>
 */
public class GroupMemberRemoveEvent extends BotEvent {

    /** 事件时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 退出成员的 OpenID */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 退出成员的用户 OpenID，可能为空 */
    @JsonProperty("user_openid")
    private String userOpenid;

    /** 跨应用关联群 openid，绑定三方应用时有效 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public Long getTimestamp() {
        return timestamp;
    }

    public GroupMemberRemoveEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupMemberRemoveEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public GroupMemberRemoveEvent setMemberOpenid(String memberOpenid) {
        this.memberOpenid = memberOpenid;
        return this;
    }

    public String getUserOpenid() {
        return userOpenid;
    }

    public GroupMemberRemoveEvent setUserOpenid(String userOpenid) {
        this.userOpenid = userOpenid;
        return this;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }

    public GroupMemberRemoveEvent setUnionGroupOpenid(String unionGroupOpenid) {
        this.unionGroupOpenid = unionGroupOpenid;
        return this;
    }
}
