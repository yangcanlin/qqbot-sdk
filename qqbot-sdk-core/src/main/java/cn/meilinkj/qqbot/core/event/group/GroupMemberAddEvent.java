package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * GROUP_MEMBER_ADD（Intents.GROUP_MEMBER_EVENT）：有新成员加入群聊。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_member_add.html">群成员加入</a>
 */
public class GroupMemberAddEvent extends BotEvent {

    /** 事件时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 新加入成员的 OpenID */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 跨应用关联群 openid，绑定三方应用时有效 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public Long getTimestamp() {
        return timestamp;
    }

    public GroupMemberAddEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupMemberAddEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public GroupMemberAddEvent setMemberOpenid(String memberOpenid) {
        this.memberOpenid = memberOpenid;
        return this;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }

    public GroupMemberAddEvent setUnionGroupOpenid(String unionGroupOpenid) {
        this.unionGroupOpenid = unionGroupOpenid;
        return this;
    }
}
