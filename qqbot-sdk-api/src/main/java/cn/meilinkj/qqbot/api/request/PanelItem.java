package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 指令面板元素（Panel 的 items 元素）。
 *
 * <p>点击行为按官方定义：type=command 时 name 会填入聊天输入框（不产生互动事件），
 * type=link 时 name 仅用于展示、点击后在浏览器打开 link。</p>
 */
public class PanelItem {

    /** 指令：点击后把 name 填入聊天输入框 */
    public static final String TYPE_COMMAND = "command";
    /** 链接：点击后打开 link */
    public static final String TYPE_LINK = "link";

    @JsonProperty("name")
    private String name;

    /** 元素描述，最多 30 个字符（约 15 个中文汉字） */
    @JsonProperty("desc")
    private String desc;

    /** 元素类型：command / link */
    @JsonProperty("type")
    private String type;

    /** 是否仅管理员可操作 */
    @JsonProperty("only_admin")
    private Boolean onlyAdmin;

    @JsonProperty("link")
    private String link;

    public static PanelItem command(String name, String desc) {
        return new PanelItem().setName(name).setDesc(desc).setType(TYPE_COMMAND);
    }

    /** 链接元素，官方要求 URL 以 https:// 开头（否则 err_code=40030008）。 */
    public static PanelItem link(String name, String url) {
        return new PanelItem().setName(name).setType(TYPE_LINK).setLink(SubMenuItem.requireHttps(url));
    }

    public String getName() {
        return name;
    }

    public PanelItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public PanelItem setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getType() {
        return type;
    }

    public PanelItem setType(String type) {
        this.type = type;
        return this;
    }

    public Boolean getOnlyAdmin() {
        return onlyAdmin;
    }

    public PanelItem setOnlyAdmin(Boolean onlyAdmin) {
        this.onlyAdmin = onlyAdmin;
        return this;
    }

    public String getLink() {
        return link;
    }

    public PanelItem setLink(String link) {
        this.link = link;
        return this;
    }
}
