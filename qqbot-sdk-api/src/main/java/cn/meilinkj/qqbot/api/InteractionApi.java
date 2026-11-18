package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.core.http.HttpApiClient;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 互动事件响应接口：PUT /interactions/{interaction_id}（50 QPS）。
 *
 * <p>收到 INTERACTION_CREATE 事件后需调用此接口回应，否则客户端一直 loading 直到超时。
 * 仅 type=11（消息按钮）与 type=12（快捷菜单）需要回应；同一 interaction_id 只能回应一次，
 * 超时后失效。interaction_id 取事件 d.id（不带 "INTERACTION_CREATE:" 前缀）。</p>
 *
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/interactions_interaction_id.put.html">互动事件响应</a>
 */
public class InteractionApi {

    /** 回调结果：成功 */
    public static final int CODE_SUCCESS = 0;
    /** 回调结果：操作失败 */
    public static final int CODE_FAILED = 1;
    /** 回调结果：操作频繁 */
    public static final int CODE_FREQUENT = 2;
    /** 回调结果：重复操作 */
    public static final int CODE_DUPLICATE = 3;
    /** 回调结果：没有权限 */
    public static final int CODE_NO_PERMISSION = 4;
    /** 回调结果：仅管理员操作 */
    public static final int CODE_ADMIN_ONLY = 5;

    private final HttpApiClient http;

    public InteractionApi(HttpApiClient http) {
        this.http = http;
    }

    /** 回应互动事件，code 取值 0~5（见 CODE_* 常量）。 */
    public void respond(String interactionId, int code) {
        Objects.requireNonNull(interactionId, "interactionId 不能为空");
        if (code < 0 || code > 5) {
            throw new IllegalArgumentException("code 取值范围 0~5: " + code);
        }
        http.put("/interactions/" + interactionId, Map.of("code", code));
    }

    /** 回应成功（code=0）。 */
    public void respondSuccess(String interactionId) {
        respond(interactionId, CODE_SUCCESS);
    }

    public CompletableFuture<Void> respondAsync(String interactionId, int code) {
        Objects.requireNonNull(interactionId, "interactionId 不能为空");
        if (code < 0 || code > 5) {
            throw new IllegalArgumentException("code 取值范围 0~5: " + code);
        }
        return http.putAsync("/interactions/" + interactionId, Map.of("code", code));
    }
}