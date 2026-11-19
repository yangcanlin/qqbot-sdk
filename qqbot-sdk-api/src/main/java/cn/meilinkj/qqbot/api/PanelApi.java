package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.Panel;
import cn.meilinkj.qqbot.api.request.PanelCreateRequest;
import cn.meilinkj.qqbot.api.request.PanelTargetRequest;
import cn.meilinkj.qqbot.api.request.PanelUpdateRequest;
import cn.meilinkj.qqbot.api.response.Page;
import cn.meilinkj.qqbot.api.response.PanelCreateResponse;
import cn.meilinkj.qqbot.api.response.PanelRecord;
import cn.meilinkj.qqbot.api.response.VersionResponse;
import cn.meilinkj.qqbot.api.support.Paging;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 指令面板（聊天窗口能力面板）接口，官方「服务端接口 / 机器人 / 自定义菜单与指令面板」章节的面板部分。
 *
 * <ul>
 *   <li>面板列表：GET /v2/panels?scope=&amp;cursor=&amp;limit=
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.get.html">文档</a></li>
 *   <li>创建面板：POST /v2/panels
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.post.html">文档</a></li>
 *   <li>面板详情：GET /v2/panels/{panel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.get.html">文档</a></li>
 *   <li>修改面板：PUT /v2/panels/{panel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.put.html">文档</a></li>
 *   <li>删除面板：DELETE /v2/panels/{panel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.delete.html">文档</a></li>
 *   <li>关联对象：PUT /v2/panels/{panel_id}/target
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id_target.put.html">文档</a></li>
 * </ul>
 *
 * <p>官方频控：列表 / 详情 30 QPM，创建 / 修改 / 删除 10 QPM，关联对象 60 QPM；一个机器人最多 20 个面板。
 * 面板元素 type=command 点击后把名称填入输入框（不产生 INTERACTION_CREATE 互动事件）。</p>
 */
public class PanelApi {

    private static final String PANELS_PATH = "/v2/panels";

    private final HttpApiClient http;

    public PanelApi(HttpApiClient http) {
        this.http = http;
    }

    /**
     * 官方：GET /v2/panels（查询指令面板列表）；30 QPM，按设置时间倒序。
     * scope 必填，可选 c2c / group / channel / dm；limit 默认 20、最大 50；返回 Page 的 nextCursor 为空串表示已到最后一页。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.get.html">文档</a>
     */
    public Page<PanelRecord> getPanels(String scope, String cursor, Integer limit) {
        Objects.requireNonNull(scope, "scope 不能为空（官方必填：c2c / group / channel / dm）");
        return toPage(http.executeRaw("GET", PANELS_PATH + query(scope, cursor, limit), null));
    }

    /** 官方：GET /v2/panels（查询指令面板列表）首页。 */
    public Page<PanelRecord> getPanels(String scope) {
        return getPanels(scope, null, null);
    }

    /**
     * 官方：POST /v2/panels（创建指令面板）；10 QPM，一个机器人最多 20 个面板。
     * 常见错误码：40030011（scope 非法）、40030012（target_type 非法）、40030013（数量超限）、40030015（元素类型非法）。
     *
     * @return 新创建的面板 ID
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels.post.html">文档</a>
     */
    public String createPanel(PanelCreateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        PanelCreateResponse response = http.post(PANELS_PATH, request, PanelCreateResponse.class);
        return response == null ? null : response.getPanelId();
    }

    public CompletableFuture<String> createPanelAsync(PanelCreateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(PANELS_PATH, request, PanelCreateResponse.class)
                .thenApply(response -> response == null ? null : response.getPanelId());
    }

    /**
     * 官方：GET /v2/panels/{panel_id}（查询指令面板详情）；30 QPM。
     * specific 面板会附带关联的 user_openids / group_openids（最多 1000 条）。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.get.html">文档</a>
     */
    public PanelRecord getPanel(String panelId) {
        return http.get(panelPath(panelId), PanelRecord.class);
    }

    public CompletableFuture<PanelRecord> getPanelAsync(String panelId) {
        return http.getAsync(panelPath(panelId), PanelRecord.class);
    }

    /**
     * 官方：PUT /v2/panels/{panel_id}（修改指令面板）；10 QPM。
     * 覆盖面板元素列表与备注，不影响已关联的用户 / 群列表。
     *
     * @return 本次修改后的面板版本号
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.put.html">文档</a>
     */
    public VersionResponse updatePanel(String panelId, Panel panel) {
        Objects.requireNonNull(panel, "panel 不能为空");
        return http.put(panelPath(panelId), PanelUpdateRequest.of(panel), VersionResponse.class);
    }

    public CompletableFuture<VersionResponse> updatePanelAsync(String panelId, Panel panel) {
        Objects.requireNonNull(panel, "panel 不能为空");
        return http.putAsync(panelPath(panelId), PanelUpdateRequest.of(panel), VersionResponse.class);
    }

    /**
     * 官方：DELETE /v2/panels/{panel_id}（删除指令面板）；10 QPM，删除后该面板不再对任何用户或群生效，响应无 body。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id.delete.html">文档</a>
     */
    public void deletePanel(String panelId) {
        http.delete(panelPath(panelId));
    }

    public CompletableFuture<Void> deletePanelAsync(String panelId) {
        return http.deleteAsync(panelPath(panelId));
    }

    /**
     * 官方：PUT /v2/panels/{panel_id}/target（修改指令面板关联对象）；60 QPM，单次最多 20 个 openid，响应无 body。
     * 仅 target_type=specific 的面板可操作（全局面板返回 err_code=40030021），channel / dm 场景返回 40030018。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_panels_panel_id_target.put.html">文档</a>
     */
    public void updatePanelTarget(String panelId, PanelTargetRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        http.put(panelPath(panelId) + "/target", request);
    }

    public CompletableFuture<Void> updatePanelTargetAsync(String panelId, PanelTargetRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.putAsync(panelPath(panelId) + "/target", request);
    }

    private static String panelPath(String panelId) {
        if (panelId == null || panelId.isBlank()) {
            throw new IllegalArgumentException("panelId 不能为空");
        }
        return PANELS_PATH + "/" + panelId;
    }

    private static String query(String scope, String cursor, Integer limit) {
        if (limit != null && (limit < 1 || limit > 50)) {
            throw new IllegalArgumentException("官方限制 limit 为 1~50，当前 " + limit);
        }
        StringBuilder query = Paging.query();
        Paging.add(query, "scope", scope);
        Paging.add(query, "cursor", cursor);
        Paging.add(query, "limit", limit == null ? null : String.valueOf(limit));
        return query.toString();
    }

    private static Page<PanelRecord> toPage(JsonNode node) {
        return Paging.page(node, "records", items ->
                JsonUtil.mapper().convertValue(items, new TypeReference<List<PanelRecord>>() {
                }));
    }
}
