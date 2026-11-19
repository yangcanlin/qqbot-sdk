package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 机器人在指定群中的状态（GET /v2/groups/{group_openid}/bot_state，30 QPM，仅白名单机器人可用，否则报 11253）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_bot_state.get.html">获取机器人群内状态</a>
 */
public class GroupBotState {

    /** 机器人的 openid */
    @JsonProperty("member_openid")
    private String memberOpenid;

    /** 入群时间戳，RFC3339 格式 */
    @JsonProperty("joined_at")
    private String joinedAt;

    /** 是否接受主动推送 */
    @JsonProperty("allow_proactive_msg")
    private Boolean allowProactiveMsg;

    /** 群内接收消息设置：all、only_mention、mention_and_context */
    @JsonProperty("recv_msg_setting")
    private String recvMsgSetting;

    /** 群成员角色：member-普通成员，owner-群主，admin-管理员 */
    @JsonProperty("member_role")
    private String memberRole;

    public String getMemberOpenid() {
        return memberOpenid;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public Boolean isAllowProactiveMsg() {
        return allowProactiveMsg;
    }

    public String getRecvMsgSetting() {
        return recvMsgSetting;
    }

    public String getMemberRole() {
        return memberRole;
    }
}
