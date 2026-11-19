package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 子频道（Channel）对象，官方「获取子频道列表 / 创建子频道 / 获取子频道详情 / 修改子频道」共用同一结构。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.get.html">获取子频道列表</a>
 */
public class Channel {

    /** 文字子频道 */
    public static final int TYPE_TEXT = 0;
    /** 语音子频道 */
    public static final int TYPE_VOICE = 2;
    /** 分组（目录）节点 */
    public static final int TYPE_CATEGORY = 4;
    /** 直播子频道 */
    public static final int TYPE_LIVE = 10005;
    /** 应用子频道 */
    public static final int TYPE_APP = 10006;
    /** 论坛子频道 */
    public static final int TYPE_FORUM = 10007;

    /** 文字子频道子类型：闲聊 */
    public static final int SUB_TYPE_CHAT = 0;
    /** 文字子频道子类型：公告 */
    public static final int SUB_TYPE_NOTICE = 1;
    /** 文字子频道子类型：攻略 */
    public static final int SUB_TYPE_STRATEGY = 2;
    /** 文字子频道子类型：开黑 */
    public static final int SUB_TYPE_GAME = 3;

    /** 私密类型：公开 */
    public static final int PRIVATE_TYPE_PUBLIC = 0;
    /** 私密类型：群主管理员可见 */
    public static final int PRIVATE_TYPE_ADMIN = 1;
    /** 私密类型：群主管理员 + 指定成员 */
    public static final int PRIVATE_TYPE_ASSIGN = 2;

    /** 发言权限：无效 */
    public static final int SPEAK_PERMISSION_INVALID = 0;
    /** 发言权限：所有人 */
    public static final int SPEAK_PERMISSION_ALL = 1;
    /** 发言权限：群主管理员 + 指定成员 */
    public static final int SPEAK_PERMISSION_ADMIN_ASSIGN = 2;

    /** 子频道 ID */
    @JsonProperty("id")
    private String id;

    /** 所属频道 ID */
    @JsonProperty("guild_id")
    private String guildId;

    /** 子频道名 */
    @JsonProperty("name")
    private String name;

    /** 子频道类型，见 TYPE_* */
    @JsonProperty("type")
    private Integer type;

    /** 子频道子类型（文字子频道），见 SUB_TYPE_* */
    @JsonProperty("sub_type")
    private Integer subType;

    /** 排序值，从 1 开始 */
    @JsonProperty("position")
    private Integer position;

    /** 所属分组 ID（仅子频道有效） */
    @JsonProperty("parent_id")
    private String parentId;

    /** 创建人 ID */
    @JsonProperty("owner_id")
    private String ownerId;

    /** 私密类型，见 PRIVATE_TYPE_* */
    @JsonProperty("private_type")
    private Integer privateType;

    /** 发言权限，见 SPEAK_PERMISSION_* */
    @JsonProperty("speak_permission")
    private Integer speakPermission;

    /** 应用子频道标识 */
    @JsonProperty("application_id")
    private String applicationId;

    /** 当前用户拥有的子频道权限（官方未给出位段语义） */
    @JsonProperty("permissions")
    private String permissions;

    public String getId() {
        return id;
    }

    public String getGuildId() {
        return guildId;
    }

    public String getName() {
        return name;
    }

    public Integer getType() {
        return type;
    }

    public Integer getSubType() {
        return subType;
    }

    public Integer getPosition() {
        return position;
    }

    public String getParentId() {
        return parentId;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public Integer getPrivateType() {
        return privateType;
    }

    public Integer getSpeakPermission() {
        return speakPermission;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getPermissions() {
        return permissions;
    }
}
