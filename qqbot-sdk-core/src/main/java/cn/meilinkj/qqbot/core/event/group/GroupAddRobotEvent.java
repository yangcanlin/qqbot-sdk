package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * GROUP_ADD_ROBOT：机器人被添加到群聊。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_add_robot.html">机器人加入群聊</a>
 */
public class GroupAddRobotEvent extends BotEvent {

    /** 加入时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 操作添加机器人进群的群成员 OpenID */
    @JsonProperty("op_member_openid")
    private String opMemberOpenid;

    /** 跨应用关联群 openid，绑定三方应用时有效 */
    @JsonProperty("union_group_openid")
    private String unionGroupOpenid;

    public Long getTimestamp() {
        return timestamp;
    }

    public GroupAddRobotEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupAddRobotEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getOpMemberOpenid() {
        return opMemberOpenid;
    }

    public GroupAddRobotEvent setOpMemberOpenid(String opMemberOpenid) {
        this.opMemberOpenid = opMemberOpenid;
        return this;
    }

    public String getUnionGroupOpenid() {
        return unionGroupOpenid;
    }

    public GroupAddRobotEvent setUnionGroupOpenid(String unionGroupOpenid) {
        this.unionGroupOpenid = unionGroupOpenid;
        return this;
    }
}