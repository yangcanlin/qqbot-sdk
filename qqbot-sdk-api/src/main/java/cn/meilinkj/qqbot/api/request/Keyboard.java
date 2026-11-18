package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * 内嵌键盘。短形式只传 id（平台预设模板），长形式传 content.rows（自定义按钮），二者互斥。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-Keyboard</a>
 */
public class Keyboard {

    /** 内嵌键盘模板 ID */
    @JsonProperty("id")
    private String id;

    @JsonProperty("content")
    private Content content;

    public static Keyboard ofTemplate(String id) {
        return new Keyboard().setId(id);
    }

    public static Keyboard ofRows(List<Row> rows) {
        return new Keyboard().setContent(new Content().setRows(rows));
    }

    public String getId() {
        return id;
    }

    public Keyboard setId(String id) {
        this.id = id;
        return this;
    }

    public Content getContent() {
        return content;
    }

    public Keyboard setContent(Content content) {
        this.content = content;
        return this;
    }

    /** 见官方 KeyboardContent */
    public static class Content {

        @JsonProperty("rows")
        private List<Row> rows;

        public List<Row> getRows() {
            return rows;
        }

        public Content setRows(List<Row> rows) {
            this.rows = rows;
            return this;
        }
    }

    /** 见官方 Row */
    public static class Row {

        /** 行内按钮，从左到右排列 */
        @JsonProperty("buttons")
        private List<Button> buttons;

        public static Row of(Button... buttons) {
            return new Row().setButtons(new ArrayList<>(List.of(buttons)));
        }

        public List<Button> getButtons() {
            return buttons;
        }

        public Row setButtons(List<Button> buttons) {
            this.buttons = buttons;
            return this;
        }
    }

    /** 见官方 Button */
    public static class Button {

        /** 按钮 ID，同一键盘内唯一 */
        @JsonProperty("id")
        private String id;

        @JsonProperty("render_data")
        private RenderData renderData;

        @JsonProperty("action")
        private Action action;

        /** 分组 ID，同组内一个按钮操作后其它按钮变灰（仅 action.type=1 有效） */
        @JsonProperty("group_id")
        private String groupId;

        public String getId() {
            return id;
        }

        public Button setId(String id) {
            this.id = id;
            return this;
        }

        public RenderData getRenderData() {
            return renderData;
        }

        public Button setRenderData(RenderData renderData) {
            this.renderData = renderData;
            return this;
        }

        public Action getAction() {
            return action;
        }

        public Button setAction(Action action) {
            this.action = action;
            return this;
        }

        public String getGroupId() {
            return groupId;
        }

        public Button setGroupId(String groupId) {
            this.groupId = groupId;
            return this;
        }
    }

    /** 见官方 RenderData */
    public static class RenderData {

        /** 按钮文字，最多 10 字符 */
        @JsonProperty("label")
        private String label;

        /** 点击后文字，不传则保持不变 */
        @JsonProperty("visited_label")
        private String visitedLabel;

        /** 0=灰色线框, 1=蓝色线框, 3=白色背景+红色字体, 4=蓝色背景+白色字体 */
        @JsonProperty("style")
        private Integer style;

        public String getLabel() {
            return label;
        }

        public RenderData setLabel(String label) {
            this.label = label;
            return this;
        }

        public String getVisitedLabel() {
            return visitedLabel;
        }

        public RenderData setVisitedLabel(String visitedLabel) {
            this.visitedLabel = visitedLabel;
            return this;
        }

        public Integer getStyle() {
            return style;
        }

        public RenderData setStyle(Integer style) {
            this.style = style;
            return this;
        }
    }

    /** 见官方 Action */
    public static class Action {

        /** 0=跳转按钮(http/小程序), 1=回调按钮(data 传给后台), 2=指令按钮(输入框插入 @bot data) */
        @JsonProperty("type")
        private Integer type;

        @JsonProperty("permission")
        private Permission permission;

        /** 回调数据，type=1/2 时必填 */
        @JsonProperty("data")
        private String data;

        /** 【已废弃】可点击次数限制，0=无限 */
        @JsonProperty("click_limit")
        private Integer clickLimit;

        /** 版本过低时提示文案 */
        @JsonProperty("unsupport_tips")
        private String unsupportTips;

        /** 指令按钮：点击后直接自动发送 data，仅单聊可用，默认 false（8983+） */
        @JsonProperty("enter")
        private Boolean enter;

        /** 指令按钮：指令是否带引用回复本消息，默认 false（8983+） */
        @JsonProperty("reply")
        private Boolean reply;

        /** 仅指令按钮有效：1=点击自动唤起手Q选图器（8983+ 单聊，桌面端不支持） */
        @JsonProperty("anchor")
        private Integer anchor;

        @JsonProperty("modal")
        private Modal modal;

        public Integer getType() {
            return type;
        }

        public Action setType(Integer type) {
            this.type = type;
            return this;
        }

        public Permission getPermission() {
            return permission;
        }

        public Action setPermission(Permission permission) {
            this.permission = permission;
            return this;
        }

        public String getData() {
            return data;
        }

        public Action setData(String data) {
            this.data = data;
            return this;
        }

        public Integer getClickLimit() {
            return clickLimit;
        }

        public Action setClickLimit(Integer clickLimit) {
            this.clickLimit = clickLimit;
            return this;
        }

        public String getUnsupportTips() {
            return unsupportTips;
        }

        public Action setUnsupportTips(String unsupportTips) {
            this.unsupportTips = unsupportTips;
            return this;
        }

        public Boolean getEnter() {
            return enter;
        }

        public Action setEnter(Boolean enter) {
            this.enter = enter;
            return this;
        }

        public Boolean getReply() {
            return reply;
        }

        public Action setReply(Boolean reply) {
            this.reply = reply;
            return this;
        }

        public Integer getAnchor() {
            return anchor;
        }

        public Action setAnchor(Integer anchor) {
            this.anchor = anchor;
            return this;
        }

        public Modal getModal() {
            return modal;
        }

        public Action setModal(Modal modal) {
            this.modal = modal;
            return this;
        }
    }

    /** 见官方 Permission */
    public static class Permission {

        /** 0=指定用户, 1=管理员, 2=所有人 */
        @JsonProperty("type")
        private Integer type;

        @JsonProperty("specify_user_ids")
        private List<String> specifyUserIds;

        /** 身份组 id 列表（仅频道可用） */
        @JsonProperty("specify_role_ids")
        private List<String> specifyRoleIds;

        public static Permission all() {
            return new Permission().setType(2);
        }

        public Integer getType() {
            return type;
        }

        public Permission setType(Integer type) {
            this.type = type;
            return this;
        }

        public List<String> getSpecifyUserIds() {
            return specifyUserIds;
        }

        public Permission setSpecifyUserIds(List<String> specifyUserIds) {
            this.specifyUserIds = specifyUserIds;
            return this;
        }

        public List<String> getSpecifyRoleIds() {
            return specifyRoleIds;
        }

        public Permission setSpecifyRoleIds(List<String> specifyRoleIds) {
            this.specifyRoleIds = specifyRoleIds;
            return this;
        }
    }

    /** 见官方 Modal（二次确认弹窗） */
    public static class Modal {

        /** 二次确认提示文本，最多 40 字符，不能有 URL */
        @JsonProperty("content")
        private String content;

        /** 确认按钮文字，最多 4 字符，默认"确认" */
        @JsonProperty("confirm_text")
        private String confirmText;

        /** 取消按钮文字，最多 4 字符，默认"取消" */
        @JsonProperty("cancel_text")
        private String cancelText;

        public String getContent() {
            return content;
        }

        public Modal setContent(String content) {
            this.content = content;
            return this;
        }

        public String getConfirmText() {
            return confirmText;
        }

        public Modal setConfirmText(String confirmText) {
            this.confirmText = confirmText;
            return this;
        }

        public String getCancelText() {
            return cancelText;
        }

        public Modal setCancelText(String cancelText) {
            this.cancelText = cancelText;
            return this;
        }
    }
}