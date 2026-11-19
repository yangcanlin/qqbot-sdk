package cn.meilinkj.qqbot.api.response;

import cn.meilinkj.qqbot.api.request.Menu;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 查询全局自定义菜单响应（官方 GET /v2/menu）。
 */
public class MenuResponse {

    /** 当前菜单版本号，可用于判断配置是否变更 */
    @JsonProperty("version")
    private Integer version;

    /** 当前生效的菜单配置；官方在未设置过菜单时该字段为空 */
    @JsonProperty("menu")
    private Menu menu;

    public Integer getVersion() {
        return version;
    }

    public Menu getMenu() {
        return menu;
    }
}
