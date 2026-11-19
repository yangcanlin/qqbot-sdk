package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 机器人所在频道信息（GuildInfo，GET /users/@me/guilds 响应元素）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me_guilds.get.html">获取机器人频道列表</a>
 */
public class BotGuild {

    /** 频道 ID */
    @JsonProperty("id")
    private String id;

    /** 频道名称 */
    @JsonProperty("name")
    private String name;

    /** 频道头像 URL */
    @JsonProperty("icon")
    private String icon;

    /** 频道创建者 ID */
    @JsonProperty("owner_id")
    private String ownerId;

    /** 当前机器人是否为频道创建者 */
    @JsonProperty("owner")
    private Boolean owner;

    /** 加入时间，ISO8601 格式 */
    @JsonProperty("joined_at")
    private String joinedAt;

    /** 频道成员数 */
    @JsonProperty("member_count")
    private Integer memberCount;

    /** 频道成员上限 */
    @JsonProperty("max_members")
    private Integer maxMembers;

    /** 频道简介 */
    @JsonProperty("description")
    private String description;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Boolean isOwner() {
        return owner;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public String getDescription() {
        return description;
    }
}
