package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 修改全局自定义菜单请求体（官方 PUT /v2/menu）。
 * 传入 menu 后会覆盖原有的完整菜单配置；菜单仅支持 C2C 单聊场景，设置后对所有用户生效。
 */
public class MenuUpdateRequest {

    @JsonProperty("menu")
    private Menu menu;

    public static MenuUpdateRequest of(Menu menu) {
        if (menu != null && menu.getItems() != null && menu.getItems().size() > 10) {
            throw new IllegalArgumentException("官方限制菜单最多 10 个菜单项，当前 " + menu.getItems().size() + " 个");
        }
        return new MenuUpdateRequest().setMenu(menu);
    }

    public Menu getMenu() {
        return menu;
    }

    public MenuUpdateRequest setMenu(Menu menu) {
        this.menu = menu;
        return this;
    }
}
