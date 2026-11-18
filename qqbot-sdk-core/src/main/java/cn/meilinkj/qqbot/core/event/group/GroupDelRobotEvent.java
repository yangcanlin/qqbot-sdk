package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * GROUP_DEL_ROBOT：机器人被移出群聊。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_del_robot.html">机器人退出群聊</a>
 */
public class GroupDelRobotEvent extends BotEvent {

    /** 移除时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 操作移除机器人退群的群成员 OpenID */
    @JsonProperty("op_member_openid")
    private String opMemberOpenid;

    /** 跨应用关联群 openid，绑定三方应用时有效 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public Long getTimestamp() {
        return timestamp;
    }

    public GroupDelRobotEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupDelRobotEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getOpMemberOpenid() {
        return opMemberOpenid;
    }

    public GroupDelRobotEvent setOpMemberOpenid(String opMemberOpenid) {
        this.opMemberOpenid = opMemberOpenid;
        return this;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }

    public GroupDelRobotEvent setUnionGroupOpenid(String unionGroupOpenid) {
        this.unionGroupOpenid = unionGroupOpenid;
        return this;
    }
}