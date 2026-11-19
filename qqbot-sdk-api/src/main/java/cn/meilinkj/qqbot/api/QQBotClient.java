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
    private final BotApi botApi;
    private final GroupApi groupApi;
    private final GroupManageApi groupManageApi;
    private final C2CApi c2cApi;
    private final ChannelApi channelApi;
    private final ChannelManageApi channelManageApi;
    private final InteractionApi interactionApi;
    private final MenuApi menuApi;
    private final PanelApi panelApi;
    private final GatewayInfoApi gatewayInfoApi;

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
        this.botApi = new BotApi(http);
        this.groupApi = new GroupApi(http, seqAllocator);
        this.groupManageApi = new GroupManageApi(http);
        this.c2cApi = new C2CApi(http, seqAllocator);
        this.channelApi = new ChannelApi(http);
        this.channelManageApi = new ChannelManageApi(http);
        this.interactionApi = new InteractionApi(http);
        this.menuApi = new MenuApi(http);
        this.panelApi = new PanelApi(http);
        this.gatewayInfoApi = new GatewayInfoApi(http);
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

    /** 官方：机器人章节接口 GET /users/@me、GET /users/@me/guilds、GET /v2/groups/{group_openid}/bot_state、POST /v2/generate_url_link（详情/频道列表/群内状态/分享链接）。 */
    public BotApi bot() {
        return botApi;
    }

    /** 官方：消息收发/群聊接口 POST /v2/groups/{group_openid}/messages、DELETE .../messages/{message_id}、POST .../files（发消息/撤回/富媒体上传）。 */
    public GroupApi group() {
        return groupApi;
    }

    /** 官方：群聊管理章节 16 个接口（群信息/群成员/批量移除/黑名单/禁言/入群申请与审批/自动审批策略）。 */
    public GroupManageApi groupManage() {
        return groupManageApi;
    }

    /** 官方：消息收发/单聊接口 POST /v2/users/{user_openid}/messages、.../stream_messages、DELETE .../messages/{message_id}、POST .../files、GET .../info（发消息/流式/撤回/富媒体/用户信息，含输入中与唤醒消息）。 */
    public C2CApi c2c() {
        return c2cApi;
    }

    /** 官方：消息收发/频道接口 POST /channels/{channel_id}/messages、DELETE .../messages/{message_id}、POST /users/@me/dms、POST /dms/{guild_id}/messages（子频道与私信）。 */
    public ChannelApi channel() {
        return channelApi;
    }

    /** 官方：PUT /interactions/{interaction_id}（互动事件响应，按钮点击回执）。 */
    public InteractionApi interaction() {
        return interactionApi;
    }

    /** 官方：频道管理章节 6 个接口 GET /guilds/{guild_id}、GET /guilds/{guild_id}/channels、POST .../channels、GET/PUT/PATCH/DELETE /channels/{channel_id}（频道与子频道增删改查）。 */
    public ChannelManageApi channelManage() {
        return channelManageApi;
    }

    /** 官方：GET /v2/menu、PUT /v2/menu（查询与修改全局自定义菜单，仅单聊场景生效）。 */
    public MenuApi menu() {
        return menuApi;
    }

    /** 官方：/v2/panels 系列 6 个接口（指令面板列表/创建/详情/修改/删除/关联对象）。 */
    public PanelApi panel() {
        return panelApi;
    }

    /** 官方：GET /gateway（获取通用 WSS 接入点）。SDK 内部建连已自动调用，业务侧一般无需使用。 */
    public GatewayInfoApi gatewayInfo() {
        return gatewayInfoApi;
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