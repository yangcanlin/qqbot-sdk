package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.Menu;
import cn.meilinkj.qqbot.api.request.MenuUpdateRequest;
import cn.meilinkj.qqbot.api.response.MenuResponse;
import cn.meilinkj.qqbot.api.response.VersionResponse;
import cn.meilinkj.qqbot.core.http.HttpApiClient;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 机器人自定义菜单接口（官方「服务端接口 / 机器人 / 自定义菜单与指令面板」章节的菜单部分）。
 *
 * <ul>
 *   <li>查询菜单：GET /v2/menu
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.get.html">文档</a></li>
 *   <li>修改菜单：PUT /v2/menu（全量覆盖）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.put.html">文档</a></li>
 * </ul>
 *
 * <p>官方约束：自定义菜单仅支持 C2C（单聊）场景，设置后对所有用户生效，不支持按用户维度区分；
 * 面板（按钮区）配置请用 {@link PanelApi}。</p>
 */
public class MenuApi {

    private static final String MENU_PATH = "/v2/menu";

    private final HttpApiClient http;

    public MenuApi(HttpApiClient http) {
        this.http = http;
    }

    /**
     * 官方：GET /v2/menu（查询全局自定义菜单）；30 QPM。
     * 未设置过菜单时 {@link MenuResponse#getMenu()} 为空。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.get.html">文档</a>
     */
    public MenuResponse getMenu() {
        return http.get(MENU_PATH, MenuResponse.class);
    }

    public CompletableFuture<MenuResponse> getMenuAsync() {
        return http.getAsync(MENU_PATH, MenuResponse.class);
    }

    /**
     * 官方：PUT /v2/menu（修改全局自定义菜单）；5 QPM，传入后覆盖原有完整菜单配置（最多 10 个菜单项）。
     * 常见错误码：40030008（URL 非 https）、40030013（数量超限）、40030014（type 非法）、40030016（必填缺失）、40030020（内容安全风险）。
     *
     * @return 本次修改后的菜单版本号
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_menu.put.html">文档</a>
     */
    public VersionResponse setMenu(Menu menu) {
        Objects.requireNonNull(menu, "menu 不能为空");
        return http.put(MENU_PATH, MenuUpdateRequest.of(menu), VersionResponse.class);
    }

    public CompletableFuture<VersionResponse> setMenuAsync(Menu menu) {
        Objects.requireNonNull(menu, "menu 不能为空");
        return http.putAsync(MENU_PATH, MenuUpdateRequest.of(menu), VersionResponse.class);
    }
}
