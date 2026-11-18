package cn.meilinkj.qqbot.api;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.api.support.MsgSeqAllocator;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.event.BotEvent;
import cn.meilinkj.qqbot.core.event.EventDispatcher;
import cn.meilinkj.qqbot.core.event.EventListener;
import cn.meilinkj.qqbot.core.gateway.GatewayClient;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import okhttp3.OkHttpClient;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * SDK 统一入口：组装鉴权、HTTP、网关、事件分发与各业务 API。
 *
 * <pre>{@code
 * QQBotConfig config = QQBotConfig.builder(appId, appSecret)
 *         .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT))
 *         .build();
 * try (QQBotClient client = new QQBotClient(config)) {
 *     client.on(GroupAtMessageCreateEvent.class, e ->
 *             client.group().replyText(e.getGroupOpenid(), e.getId(), e.getContent()));
 *     client.start();
 * }
 * }</pre>
 *
 * <p>协议与接口定义见官方文档 <a href="https://bot.q.qq.com/wiki/develop/api-v2/">QQ 机器人开发文档</a>。</p>
 */
public class QQBotClient implements AutoCloseable {

    private final QQBotConfig config;
    private final OkHttpClient httpClient;
    private final AccessTokenManager tokenManager;
    private final HttpApiClient http;
    private final EventDispatcher dispatcher;
    private final GatewayClient gateway;
    private final GroupApi groupApi;
    private final C2CApi c2cApi;
    private final ChannelApi channelApi;
    private final InteractionApi interactionApi;

    public QQBotClient(QQBotConfig config) {
        this.config = Objects.requireNonNull(config, "config 不能为空");
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(Duration.ofMillis(config.getConnectTimeoutMs()))
                .readTimeout(Duration.ofMillis(config.getReadTimeoutMs()))
                .writeTimeout(Duration.ofMillis(config.getWriteTimeoutMs()))
                .build();
        this.tokenManager = new AccessTokenManager(config, httpClient);
        this.http = new HttpApiClient(config, tokenManager, httpClient);
        this.dispatcher = new EventDispatcher(config.getEventThreads());
        this.gateway = new GatewayClient(config, tokenManager, http, dispatcher);
        MsgSeqAllocator seqAllocator = new MsgSeqAllocator();
        this.groupApi = new GroupApi(http, seqAllocator);
        this.c2cApi = new C2CApi(http, seqAllocator);
        this.channelApi = new ChannelApi(http);
        this.interactionApi = new InteractionApi(http);
    }

    /** 启动 WebSocket 网关（自动鉴权、心跳、断线重连、会话恢复）。 */
    public void start() {
        gateway.start();
    }

    /** 注册类型化事件监听器（事件类型由泛型参数解析）。 */
    public <E extends BotEvent> QQBotClient on(EventListener<E> listener) {
        dispatcher.addListener(listener);
        return this;
    }

    /** 注册指定事件类型的监听器。 */
    public <E extends BotEvent> QQBotClient on(Class<E> eventType, Consumer<E> consumer) {
        dispatcher.addListener(eventType, consumer);
        return this;
    }

    /** 注册原始事件监听器（按官方事件名，d 为原始 JsonNode，适用于未建模事件）。 */
    public QQBotClient onRaw(String eventType, Consumer<JsonNode> consumer) {
        dispatcher.addRawListener(eventType, consumer);
        return this;
    }

    /** 群聊 V2 接口。 */
    public GroupApi group() {
        return groupApi;
    }

    /** 单聊（C2C）V2 接口。 */
    public C2CApi c2c() {
        return c2cApi;
    }

    /** 频道消息接口。 */
    public ChannelApi channel() {
        return channelApi;
    }

    /** 互动事件响应接口。 */
    public InteractionApi interaction() {
        return interactionApi;
    }

    public GatewayClient gateway() {
        return gateway;
    }

    public EventDispatcher dispatcher() {
        return dispatcher;
    }

    public QQBotConfig getConfig() {
        return config;
    }

    /** 关闭网关、事件线程池与 HTTP 资源。 */
    public void shutdown() {
        gateway.shutdown();
        dispatcher.shutdown();
        http.shutdown();
        httpClient.dispatcher().executorService().shutdown();
        httpClient.connectionPool().evictAll();
    }

    @Override
    public void close() {
        shutdown();
    }
}