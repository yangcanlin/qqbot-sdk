package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.api.response.Channel;

import java.util.List;

/**
 * 创建子频道请求体（官方字段全部标非必填，未设置的字段由平台取默认值）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.post.html">创建子频道</a>
 */
public class ChannelCreateRequest {

    @JsonProperty("name")
    private String name;

    /** 子频道类型，见 Channel.TYPE_* */
    @JsonProperty("type")
    private Integer type;

    /** 子频道子类型，见 Channel.SUB_TYPE_* */
    @JsonProperty("sub_type")
    private Integer subType;

    /** 排序值，分组类型（type=4）必须 ≥ 2 */
    @JsonProperty("position")
    private Integer position;

    @JsonProperty("parent_id")
    private String parentId;

    /** 私密类型，见 Channel.PRIVATE_TYPE_* */
    @JsonProperty("private_type")
    private Integer privateType;

    @JsonProperty("private_user_ids")
    private List<String> privateUserIds;

    /** 发言权限，见 Channel.SPEAK_PERMISSION_* */
    @JsonProperty("speak_permission")
    private Integer speakPermission;

    @JsonProperty("application_id")
    private String applicationId;

    /** 创建文字子频道（type=0）。 */
    public static ChannelCreateRequest text(String name) {
        return new ChannelCreateRequest().setName(name).setType(Channel.TYPE_TEXT);
    }

    /** 创建语音子频道（type=2）。 */
    public static ChannelCreateRequest voice(String name) {
        return new ChannelCreateRequest().setName(name).setType(Channel.TYPE_VOICE);
    }

    /** 创建分组节点（type=4），官方要求 position ≥ 2。 */
    public static ChannelCreateRequest category(String name, int position) {
        if (position < 2) {
            throw new IllegalArgumentException("分组类型的 position 必须 ≥ 2，当前值: " + position);
        }
        return new ChannelCreateRequest().setName(name).setType(Channel.TYPE_CATEGORY).setPosition(position);
    }

    public String getName() {
        return name;
    }

    public ChannelCreateRequest setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public ChannelCreateRequest setType(Integer type) {
        this.type = type;
        return this;
    }

    public Integer getSubType() {
        return subType;
    }

    public ChannelCreateRequest setSubType(Integer subType) {
        this.subType = subType;
        return this;
    }

    public Integer getPosition() {
        return position;
    }

    public ChannelCreateRequest setPosition(Integer position) {
        this.position = position;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public ChannelCreateRequest setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public Integer getPrivateType() {
        return privateType;
    }

    public ChannelCreateRequest setPrivateType(Integer privateType) {
        this.privateType = privateType;
        return this;
    }

    public List<String> getPrivateUserIds() {
        return privateUserIds;
    }

    public ChannelCreateRequest setPrivateUserIds(List<String> privateUserIds) {
        this.privateUserIds = privateUserIds;
        return this;
    }

    public Integer getSpeakPermission() {
        return speakPermission;
    }

    public ChannelCreateRequest setSpeakPermission(Integer speakPermission) {
        this.speakPermission = speakPermission;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public ChannelCreateRequest setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }
}
