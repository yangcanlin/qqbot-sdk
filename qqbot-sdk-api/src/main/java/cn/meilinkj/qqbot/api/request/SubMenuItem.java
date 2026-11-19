package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 菜单二级项（仅 MenuItem.TYPE_SUBMENU 使用，官方不支持三级嵌套）。
 * name 最多 14 个字符（约 7 个中文汉字）。
 */
public class SubMenuItem {

    /** 发送消息（点击后填入输入框） */
    public static final String TYPE_SEND_MESSAGE = "send_message";
    /** 链接跳转（点击后打开 URL） */
    public static final String TYPE_LINK = "link";

    @JsonProperty("name")
    private String name;

    /** 按钮类型，官方仅支持 send_message / link */
    @JsonProperty("type")
    private String type;

    @JsonProperty("send_message")
    private String sendMessage;

    @JsonProperty("link")
    private String link;

    /** 二级项：点击后把 content 填入聊天输入框。 */
    public static SubMenuItem sendMessage(String name, String content) {
        return new SubMenuItem().setName(name).setType(TYPE_SEND_MESSAGE).setSendMessage(content);
    }

    /** 二级项：点击后跳转 URL，官方要求必须以 https:// 开头（否则 err_code=40030008）。 */
    public static SubMenuItem link(String name, String url) {
        return new SubMenuItem().setName(name).setType(TYPE_LINK).setLink(requireHttps(url));
    }

    static String requireHttps(String url) {
        if (url == null || !url.startsWith("https://")) {
            throw new IllegalArgumentException("官方要求链接必须以 https:// 开头，当前值: " + url);
        }
        return url;
    }

    public String getName() {
        return name;
    }

    public SubMenuItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public SubMenuItem setType(String type) {
        this.type = type;
        return this;
    }

    public String getSendMessage() {
        return sendMessage;
    }

    public SubMenuItem setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
        return this;
    }

    public String getLink() {
        return link;
    }

    public SubMenuItem setLink(String link) {
        this.link = link;
        return this;
    }
}
