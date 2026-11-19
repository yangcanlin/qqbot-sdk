package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

/**
 * 机器人自定义菜单（官方 PUT /v2/menu 请求体的 menu 字段，也用于 GET /v2/menu 响应）。
 *
 * <p>与指令面板（PanelApi）的区别：菜单展示在聊天窗口底部的固定按钮区，面板是可分页滚动的能力入口列表。</p>
 */
public class Menu {

    @JsonProperty("items")
    private List<MenuItem> items;

    public static Menu of(List<MenuItem> items) {
        return new Menu().setItems(items);
    }

    public static Menu of(MenuItem... items) {
        return of(Arrays.asList(items));
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public Menu setItems(List<MenuItem> items) {
        this.items = items;
        return this;
    }
}
