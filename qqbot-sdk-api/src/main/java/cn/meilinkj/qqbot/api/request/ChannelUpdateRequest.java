package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 修改子频道请求体（PATCH 局部更新，只传要改的字段）。
 * 官方仅允许改 name/position/parent_id/private_type/speak_permission，type 等字段不可改。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.patch.html">修改子频道</a>
 */
public class ChannelUpdateRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("position")
    private Integer position;

    @JsonProperty("parent_id")
    private String parentId;

    /** 私密类型，见 Channel.PRIVATE_TYPE_* */
    @JsonProperty("private_type")
    private Integer privateType;

    /** 发言权限，见 Channel.SPEAK_PERMISSION_* */
    @JsonProperty("speak_permission")
    private Integer speakPermission;

    /** 只改子频道名称。 */
    public static ChannelUpdateRequest rename(String name) {
        return new ChannelUpdateRequest().setName(name);
    }

    /** 调整排序或移动到其它分组（官方无独立排序接口，靠这两个字段完成）。 */
    public static ChannelUpdateRequest move(Integer position, String parentId) {
        return new ChannelUpdateRequest().setPosition(position).setParentId(parentId);
    }

    public String getName() {
        return name;
    }

    public ChannelUpdateRequest setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getPosition() {
        return position;
    }

    public ChannelUpdateRequest setPosition(Integer position) {
        this.position = position;
        return this;
    }

    public String getParentId() {
        return parentId;
    }

    public ChannelUpdateRequest setParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public Integer getPrivateType() {
        return privateType;
    }

    public ChannelUpdateRequest setPrivateType(Integer privateType) {
        this.privateType = privateType;
        return this;
    }

    public Integer getSpeakPermission() {
        return speakPermission;
    }

    public ChannelUpdateRequest setSpeakPermission(Integer speakPermission) {
        this.speakPermission = speakPermission;
        return this;
    }
}
