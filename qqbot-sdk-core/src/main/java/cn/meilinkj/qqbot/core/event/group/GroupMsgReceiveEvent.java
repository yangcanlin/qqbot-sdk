package cn.meilinkj.qqbot.core.event.group;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * GROUP_MSG_RECEIVE：群管理员在机器人资料页操作开启通知。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_msg_receive.html">群聊消息接收开启</a>
 */
public class GroupMsgReceiveEvent extends BotEvent {

    /** 操作时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 操作群成员 OpenID */
    @JsonProperty("op_member_openid")
    private String opMemberOpenid;

    public Long getTimestamp() {
        return timestamp;
    }

    public GroupMsgReceiveEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public GroupMsgReceiveEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getOpMemberOpenid() {
        return opMemberOpenid;
    }

    public GroupMsgReceiveEvent setOpMemberOpenid(String opMemberOpenid) {
        this.opMemberOpenid = opMemberOpenid;
        return this;
    }
}