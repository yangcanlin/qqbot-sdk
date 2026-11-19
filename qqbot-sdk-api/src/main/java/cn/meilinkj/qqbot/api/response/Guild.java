package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 频道（Guild）基本信息，官方「获取频道详情」响应体。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id.get.html">获取频道详情</a>
 */
public class Guild {

    /** 频道 ID */
    @JsonProperty("id")
    private String id;

    /** 频道名称 */
    @JsonProperty("name")
    private String name;

    /** 频道头像 URL */
    @JsonProperty("icon")
    private String icon;

    /** 创建者用户 ID */
    @JsonProperty("owner_id")
    private String ownerId;

    /** 当前机器人是否为创建者 */
    @JsonProperty("owner")
    private Boolean owner;

    /** 成员数 */
    @JsonProperty("member_count")
    private Integer memberCount;

    /** 最大成员数 */
    @JsonProperty("max_members")
    private Integer maxMembers;

    /** 频道描述 */
    @JsonProperty("description")
    private String description;

    /** 加入时间，ISO8601 格式 */
    @JsonProperty("joined_at")
    private String joinedAt;

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

    public Boolean getOwner() {
        return owner;
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

    public String getJoinedAt() {
        return joinedAt;
    }
}
