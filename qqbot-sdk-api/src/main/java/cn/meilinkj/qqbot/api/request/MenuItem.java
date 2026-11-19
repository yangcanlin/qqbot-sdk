package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * 一级菜单项。
 *
 * <p>官方行为差异（重要）：type=switch 与 type=menu（二级项）不会产生 INTERACTION_CREATE 互动事件，
 * 切换结果通过消息 ext 携带（key 为开关自身的 switch_id）；type=send_message 点击后文本填入输入框，
 * type=link 点击后打开 URL。</p>
 *
 * <p>name 最多 10 个字符（一个中文汉字算 2 个字符）。</p>
 */
public class MenuItem {

    /** 发送消息：点击后把 send_message 填入聊天输入框 */
    public static final String TYPE_SEND_MESSAGE = "send_message";
    /** 链接跳转：点击后打开 link */
    public static final String TYPE_LINK = "link";
    /** 二级菜单：展开 sub_menu_items（最多 5 个，不支持三级） */
    public static final String TYPE_SUBMENU = "menu";
    /** 开关：切换状态随消息 ext 的 switch_id 回传 */
    public static final String TYPE_SWITCH = "switch";

    @JsonProperty("name")
    private String name;

    /** 按钮类型：send_message / link / menu / switch */
    @JsonProperty("type")
    private String type;

    @JsonProperty("sub_menu_items")
    private List<SubMenuItem> subMenuItems;

    @JsonProperty("send_message")
    private String sendMessage;

    @JsonProperty("link")
    private String link;

    @JsonProperty("switch")
    private MenuSwitch menuSwitch;

    /** 一级项：点击后把 content 填入聊天输入框。 */
    public static MenuItem sendMessage(String name, String content) {
        return new MenuItem().setName(name).setType(TYPE_SEND_MESSAGE).setSendMessage(content);
    }

    /** 一级项：点击后跳转 URL，官方要求必须以 https:// 开头（否则 err_code=40030008）。 */
    public static MenuItem link(String name, String url) {
        return new MenuItem().setName(name).setType(TYPE_LINK).setLink(SubMenuItem.requireHttps(url));
    }

    /** 一级项：二级菜单，官方最多 5 个子项、不支持三级嵌套。 */
    public static MenuItem submenu(String name, List<SubMenuItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("二级菜单至少需要 1 个子项");
        }
        if (items.size() > 5) {
            throw new IllegalArgumentException("官方限制二级菜单最多 5 个子项，当前 " + items.size() + " 个");
        }
        return new MenuItem().setName(name).setType(TYPE_SUBMENU).setSubMenuItems(items);
    }

    /** 一级项：二级菜单（可变参数写法）。 */
    public static MenuItem submenu(String name, SubMenuItem... items) {
        return submenu(name, Arrays.asList(items));
    }

    /** 一级项：开关按钮。switchId 会出现在后续消息 ext 的 switch_id=1 中。 */
    public static MenuItem toggle(String name, String switchId, boolean defaultOn) {
        return new MenuItem().setName(name).setType(TYPE_SWITCH).setMenuSwitch(MenuSwitch.of(switchId, defaultOn));
    }

    public String getName() {
        return name;
    }

    public MenuItem setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public MenuItem setType(String type) {
        this.type = type;
        return this;
    }

    public List<SubMenuItem> getSubMenuItems() {
        return subMenuItems;
    }

    public MenuItem setSubMenuItems(List<SubMenuItem> subMenuItems) {
        this.subMenuItems = subMenuItems;
        return this;
    }

    public String getSendMessage() {
        return sendMessage;
    }

    public MenuItem setSendMessage(String sendMessage) {
        this.sendMessage = sendMessage;
        return this;
    }

    public String getLink() {
        return link;
    }

    public MenuItem setLink(String link) {
        this.link = link;
        return this;
    }

    public MenuSwitch getMenuSwitch() {
        return menuSwitch;
    }

    public MenuItem setMenuSwitch(MenuSwitch menuSwitch) {
        this.menuSwitch = menuSwitch;
        return this;
    }
}
