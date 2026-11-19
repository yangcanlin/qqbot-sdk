package cn.meilinkj.qqbot.core.event.guild;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * 频道（Guild）对象事件体，字段与官方 GUILD_CREATE / GUILD_UPDATE / GUILD_DELETE 事件页内联的频道对象一致。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/guild_create.html">频道创建</a>
 */
public abstract class GuildEvent extends BotEvent {

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

    /** 频道成员数 */
    @JsonProperty("member_count")
    private Integer memberCount;

    /** 频道成员上限 */
    @JsonProperty("max_members")
    private Integer maxMembers;

    /** 频道简介 */
    @JsonProperty("description")
    private String description;

    /** 加入时间，ISO8601 格式 */
    @JsonProperty("joined_at")
    private String joinedAt;

    /** 操作人 ID */
    @JsonProperty("op_user_id")
    private String opUserId;

    public String getId() {
        return id;
    }

    public GuildEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public GuildEvent setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public GuildEvent setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public GuildEvent setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public GuildEvent setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
        return this;
    }

    public Integer getMaxMembers() {
        return maxMembers;
    }

    public GuildEvent setMaxMembers(Integer maxMembers) {
        this.maxMembers = maxMembers;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public GuildEvent setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getJoinedAt() {
        return joinedAt;
    }

    public GuildEvent setJoinedAt(String joinedAt) {
        this.joinedAt = joinedAt;
        return this;
    }

    public String getOpUserId() {
        return opUserId;
    }

    public GuildEvent setOpUserId(String opUserId) {
        this.opUserId = opUserId;
        return this;
    }
}
