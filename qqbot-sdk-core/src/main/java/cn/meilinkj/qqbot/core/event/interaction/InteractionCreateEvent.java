package cn.meilinkj.qqbot.core.event.interaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

import java.util.List;

/**
 * INTERACTION_CREATE：用户与机器人的互动操作（消息按钮点击、快捷菜单回调、消息反馈等）。
 * type=11/12 需调用 PUT /interactions/{interaction_id} 回应，否则客户端一直 loading。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/interaction_create.html">互动事件</a>
 */
public class InteractionCreateEvent extends BotEvent {

    /** 事件 ID，用于被动消息发送和互动回调 */
    @JsonProperty("id")
    private String id;

    /**
     * 互动类型：11=消息按钮回调, 12=单聊快捷菜单回调, 13=消息反馈, 14=清空会话,
     * 15=进出故事集, 16=切换模型, 18=用户授权, 19=群授权, 20=群授权状态变更
     */
    @JsonProperty("type")
    private Integer type;

    /** 事件发生场景：c2c=单聊, group=群聊, guild=频道 */
    @JsonProperty("scene")
    private String scene;

    /** 聊天场景：0=频道, 1=群聊, 2=单聊 */
    @JsonProperty("chat_type")
    private Integer chatType;

    /** RFC3339 格式 */
    @JsonProperty("timestamp")
    private String timestamp;

    /** 频道 OpenID（仅频道场景有值） */
    @JsonProperty("guild_id")
    private String guildId;

    /** 子频道 OpenID（仅频道场景有值） */
    @JsonProperty("channel_id")
    private String channelId;

    /** 用户 OpenID（仅单聊场景有值） */
    @JsonProperty("user_openid")
    private String userOpenid;

    /** 群 OpenID（仅群聊场景有值） */
    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 群成员 OpenID（仅群聊场景有值） */
    @JsonProperty("group_member_openid")
    private String groupMemberOpenid;

    @JsonProperty("data")
    private Data data;

    /** 版本号，默认 1 */
    @JsonProperty("version")
    private Integer version;

    /** 机器人 AppID */
    @JsonProperty("application_id")
    private String applicationId;

    public String getId() {
        return id;
    }

    public InteractionCreateEvent setId(String id) {
        this.id = id;
        return this;
    }

    public Integer getType() {
        return type;
    }

    public InteractionCreateEvent setType(Integer type) {
        this.type = type;
        return this;
    }

    public String getScene() {
        return scene;
    }

    public InteractionCreateEvent setScene(String scene) {
        this.scene = scene;
        return this;
    }

    public Integer getChatType() {
        return chatType;
    }

    public InteractionCreateEvent setChatType(Integer chatType) {
        this.chatType = chatType;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public InteractionCreateEvent setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getGuildId() {
        return guildId;
    }

    public InteractionCreateEvent setGuildId(String guildId) {
        this.guildId = guildId;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public InteractionCreateEvent setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getUserOpenid() {
        return userOpenid;
    }

    public InteractionCreateEvent setUserOpenid(String userOpenid) {
        this.userOpenid = userOpenid;
        return this;
    }

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public InteractionCreateEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getGroupMemberOpenid() {
        return groupMemberOpenid;
    }

    public InteractionCreateEvent setGroupMemberOpenid(String groupMemberOpenid) {
        this.groupMemberOpenid = groupMemberOpenid;
        return this;
    }

    public Data getData() {
        return data;
    }

    public InteractionCreateEvent setData(Data data) {
        this.data = data;
        return this;
    }

    public Integer getVersion() {
        return version;
    }

    public InteractionCreateEvent setVersion(Integer version) {
        this.version = version;
        return this;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public InteractionCreateEvent setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        return this;
    }

    /** 见官方 InteractionData */
    public static class Data {

        @JsonProperty("type")
        private Integer type;

        @JsonProperty("resolved")
        private Resolved resolved;

        public Integer getType() {
            return type;
        }

        public Data setType(Integer type) {
            this.type = type;
            return this;
        }

        public Resolved getResolved() {
            return resolved;
        }

        public Data setResolved(Resolved resolved) {
            this.resolved = resolved;
            return this;
        }
    }

    /** 见官方 InteractionResolved */
    public static class Resolved {

        /** 按钮的 data 字段值；消息反馈场景下为回调数据 */
        @JsonProperty("button_data")
        private String buttonData;

        /** 按钮的 id 字段值 */
        @JsonProperty("button_id")
        private String buttonId;

        /** 操作用户 ID（仅频道场景有值） */
        @JsonProperty("user_id")
        private String userId;

        /** 功能 ID（仅快捷菜单有值） */
        @JsonProperty("feature_id")
        private String featureId;

        /** 操作的消息 ID */
        @JsonProperty("message_id")
        private String messageId;

        /** 反馈选项（仅 type=13）：LIKE=点赞, UNLIKE=点踩 */
        @JsonProperty("feedback_opt")
        private String feedbackOpt;

        /** 反馈选项是否选中（仅 type=13） */
        @JsonProperty("checked")
        private Integer checked;

        /** 操作类型（type=15：ENTER_STORY/QUIT_STORY；type=16：对应操作动作） */
        @JsonProperty("action")
        private String action;

        @JsonProperty("message_scene")
        private MessageScene messageScene;

        @JsonProperty("authorize_data")
        private AuthorizeData authorizeData;

        public String getButtonData() {
            return buttonData;
        }

        public Resolved setButtonData(String buttonData) {
            this.buttonData = buttonData;
            return this;
        }

        public String getButtonId() {
            return buttonId;
        }

        public Resolved setButtonId(String buttonId) {
            this.buttonId = buttonId;
            return this;
        }

        public String getUserId() {
            return userId;
        }

        public Resolved setUserId(String userId) {
            this.userId = userId;
            return this;
        }

        public String getFeatureId() {
            return featureId;
        }

        public Resolved setFeatureId(String featureId) {
            this.featureId = featureId;
            return this;
        }

        public String getMessageId() {
            return messageId;
        }

        public Resolved setMessageId(String messageId) {
            this.messageId = messageId;
            return this;
        }

        public String getFeedbackOpt() {
            return feedbackOpt;
        }

        public Resolved setFeedbackOpt(String feedbackOpt) {
            this.feedbackOpt = feedbackOpt;
            return this;
        }

        public Integer getChecked() {
            return checked;
        }

        public Resolved setChecked(Integer checked) {
            this.checked = checked;
            return this;
        }

        public String getAction() {
            return action;
        }

        public Resolved setAction(String action) {
            this.action = action;
            return this;
        }

        public MessageScene getMessageScene() {
            return messageScene;
        }

        public Resolved setMessageScene(MessageScene messageScene) {
            this.messageScene = messageScene;
            return this;
        }

        public AuthorizeData getAuthorizeData() {
            return authorizeData;
        }

        public Resolved setAuthorizeData(AuthorizeData authorizeData) {
            this.authorizeData = authorizeData;
            return this;
        }
    }

    /** 见官方 InteractionMessageScene */
    public static class MessageScene {

        /** 扩展信息键值对列表，如 "disable_net_search=1" */
        @JsonProperty("ext")
        private List<String> ext;

        public List<String> getExt() {
            return ext;
        }

        public MessageScene setExt(List<String> ext) {
            this.ext = ext;
            return this;
        }
    }

    /** 见官方 AuthorizeData（type=18/19 授权事件） */
    public static class AuthorizeData {

        /** 授权操作场景：setting=资料页设置, dialog=弹窗授权 */
        @JsonProperty("opt_scene")
        private String optScene;

        /** 授权范围：c2c_push=C2C 主动消息推送, group_push=群主动消息推送 */
        @JsonProperty("scope")
        private String scope;

        public String getOptScene() {
            return optScene;
        }

        public AuthorizeData setOptScene(String optScene) {
            this.optScene = optScene;
            return this;
        }

        public String getScope() {
            return scope;
        }

        public AuthorizeData setScope(String scope) {
            this.scope = scope;
            return this;
        }
    }
}