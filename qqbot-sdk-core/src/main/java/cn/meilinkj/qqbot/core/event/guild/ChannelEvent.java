package cn.meilinkj.qqbot.core.event.guild;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * 子频道（Channel）对象事件体，字段取自官方 CHANNEL_CREATE / CHANNEL_UPDATE / CHANNEL_DELETE 事件页。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/channel_create.html">子频道创建</a>
 */
public abstract class ChannelEvent extends BotEvent {

    /** 子频道 ID */
    @JsonProperty("id")
    private String id;

    /** 所属频道 ID */
    @JsonProperty("guild_id")
    private String guildId;

    /** 子频道名称 */
    @JsonProperty("name")
    private String name;

    /** 子频道类型：0=文字 2=语音 4=分组 10005=直播 10006=应用 10007=论坛 */
    @JsonProperty("type")
    private Integer type;

    /** 子频道子类型 */
    @JsonProperty("sub_type")
    private Integer subType;

    /**
     * 排序位置。官方事件页示例 JSON 含该字段，字段表未列出，含义参照子频道管理接口的 position。
     */
    @JsonProperty("position")
    private Integer position;

    /** 创建者 ID */
    @JsonProperty("owner_id")
    private String ownerId;

    /**
     * 操作人 ID。官方事件页字段表含该字段，示例 JSON 未出现，实际推送中可能缺失。
     */
    @JsonProperty("op_user_id")
    private String opUserId;

    public String getId() {
        return id;
    }

    public ChannelEvent setId(String id) {
        this.id = id;
        return this;
    }

    public String getGuildId() {
        return guildId;
    }

    public ChannelEvent setGuildId(String guildId) {
        this.guildId = guildId;
        return this;
    }

    public String getName() {
        return name;
    }

    public ChannelEvent setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public ChannelEvent setType(Integer type) {
        this.type = type;
        return this;
    }

    public Integer getSubType() {
        return subType;
    }

    public ChannelEvent setSubType(Integer subType) {
        this.subType = subType;
        return this;
    }

    public Integer getPosition() {
        return position;
    }

    public ChannelEvent setPosition(Integer position) {
        this.position = position;
        return this;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public ChannelEvent setOwnerId(String ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    public String getOpUserId() {
        return opUserId;
    }

    public ChannelEvent setOpUserId(String opUserId) {
        this.opUserId = opUserId;
        return this;
    }
}
