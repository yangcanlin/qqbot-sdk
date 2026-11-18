package cn.meilinkj.qqbot.core.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.event.EventDispatcher;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Gateway WSS 长连接客户端。完整实现官方连接维护流程：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#websocket方式">WebSocket 方式</a>
 *
 * <ol>
 *   <li>GET /gateway 获取 WSS 接入点（<a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/wss/url_get.html">获取通用 WSS 接入点</a>）</li>
 *   <li>连接成功收到 OpCode 10 Hello（heartbeat_interval 毫秒）</li>
 *   <li>发送 OpCode 2 Identify（token="QQBot {AccessToken}"、intents、shard），成功收到 READY</li>
 *   <li>按周期发送 OpCode 1 Heartbeat（d=最新 s，首次为 null），收到 OpCode 11 HeartbeatACK</li>
 *   <li>断线后指数退避重连，优先 OpCode 6 Resume（session_id + seq）补发遗漏事件，收到 RESUMED</li>
 *   <li>关闭码处理对齐 <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html#websocket-错误码">WebSocket 错误码</a>：
 *       4009 重新 resume；4914/4915 停止连接；其他按规则重新 identify</li>
 * </ol>
 */
public class GatewayClient {

    private static final Logger log = LoggerFactory.getLogger(GatewayClient.class);

    private final QQBotConfig config;
    private final AccessTokenManager tokenManager;
    private final HttpApiClient http;
    private final EventDispatcher dispatcher;
    private final ScheduledExecutorService scheduler;

    private final AtomicLong lastSeq = new AtomicLong(-1);
    private final AtomicInteger reconnectAttempts = new AtomicInteger();
    private final Object lock = new Object();

    private volatile WebSocketClient socket;
    private volatile String sessionId;
    private volatile long heartbeatIntervalMs;
    private volatile long lastAckTimeMs;
    private volatile boolean closed;
    private volatile boolean forceIdentify;
    private ScheduledFuture<?> heartbeatTask;

    public GatewayClient(QQBotConfig config, AccessTokenManager tokenManager,
                         HttpApiClient http, EventDispatcher dispatcher) {
        this.config = config;
        this.tokenManager = tokenManager;
        this.http = http;
        this.dispatcher = dispatcher;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "qqbot-gateway");
            t.setDaemon(true);
            return t;
        });
    }

    /** 启动网关连接（异步，自动重连）。 */
    public void start() {
        closed = false;
        scheduler.execute(this::connectNew);
    }

    /** 关闭连接并停止重连。 */
    public void shutdown() {
        closed = true;
        scheduler.shutdownNow();
        WebSocketClient current = socket;
        if (current != null) {
            current.close(1000, "client shutdown");
        }
    }

    public boolean isConnected() {
        WebSocketClient current = socket;
        return current != null && current.isOpen();
    }

    public String getSessionId() {
        return sessionId;
    }

    /** 客户端收到的最新下行序列号 s（-1 表示尚未收到）。 */
    public long getLastSeq() {
        return lastSeq.get();
    }

    private void connectNew() {
        if (closed) {
            return;
        }
        try {
            JsonNode gateway = http.executeRaw("GET", "/gateway", null);
            String url = gateway.get("url").asText();
            log.info("连接 QQ 机器人网关: {}", url);
            WebSocketClient client = new InnerClient(URI.create(url));
            // 关闭 Java-WebSocket 内置丢连检测，使用官方心跳机制维护连接
            client.setConnectionLostTimeout(0);
            synchronized (lock) {
                socket = client;
            }
            client.connect();
        } catch (Exception e) {
            log.error("获取网关接入点失败: {}", e.getMessage());
            scheduleReconnect();
        }
    }

    private void handlePayload(String message) {
        Payload payload;
        try {
            payload = Payload.parse(message);
        } catch (Exception e) {
            log.warn("无法解析网关 payload: {}", message, e);
            return;
        }
        OpCode op = OpCode.fromCode(payload.getOp());
        if (op == null) {
            log.warn("未知 opcode: {}", payload.getOp());
            return;
        }
        switch (op) {
            case HELLO -> onHello(payload);
            case DISPATCH -> onDispatch(payload);
            case HEARTBEAT_ACK -> lastAckTimeMs = System.currentTimeMillis();
            case HEARTBEAT -> send(Payload.heartbeat(currentSeq()));
            case RECONNECT -> {
                log.info("收到 OpCode 7 Reconnect，服务端要求重新连接");
                closeSocket(1000, "server reconnect");
            }
            case INVALID_SESSION -> {
                log.warn("收到 OpCode 9 Invalid Session，将重新 Identify");
                sessionId = null;
                forceIdentify = true;
                closeSocket(1000, "invalid session");
            }
            default -> log.debug("忽略 opcode {} payload={}", op, message);
        }
    }

    private void onHello(Payload payload) {
        JsonNode d = payload.getD();
        heartbeatIntervalMs = d != null && d.has("heartbeat_interval")
                ? d.get("heartbeat_interval").asLong() : 45_000;
        lastAckTimeMs = System.currentTimeMillis();
        log.info("收到 Hello，心跳周期 {}ms", heartbeatIntervalMs);
        startHeartbeat();
        if (sessionId != null && !forceIdentify) {
            log.info("尝试 Resume 会话 {} seq={}", sessionId, currentSeq());
            send(Payload.resume(tokenManager.getToken(), sessionId, lastSeq.get()));
        } else {
            forceIdentify = false;
            log.info("发送 Identify，intents={} shard={}", config.getIntents(),
                    java.util.Arrays.toString(config.getShard()));
            send(Payload.identify(tokenManager.getToken(), config.getIntents(), config.getShard()));
        }
    }

    private void onDispatch(Payload payload) {
        if (payload.getS() != null) {
            lastSeq.set(payload.getS());
        }
        if ("READY".equals(payload.getT())) {
            JsonNode d = payload.getD();
            sessionId = d != null && d.has("session_id") ? d.get("session_id").asText() : null;
            reconnectAttempts.set(0);
            log.info("鉴权成功 READY，session_id={}", sessionId);
        } else if ("RESUMED".equals(payload.getT())) {
            reconnectAttempts.set(0);
            log.info("会话恢复成功 RESUMED");
        }
        dispatcher.dispatch(payload);
    }

    private void startHeartbeat() {
        stopHeartbeat();
        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            WebSocketClient current = socket;
            if (current == null || !current.isOpen()) {
                return;
            }
            long now = System.currentTimeMillis();
            if (heartbeatIntervalMs > 0 && now - lastAckTimeMs > heartbeatIntervalMs * 2) {
                log.warn("超过 2 个心跳周期未收到 HeartbeatACK，主动重连");
                closeSocket(1000, "heartbeat timeout");
                return;
            }
            send(Payload.heartbeat(currentSeq()));
        }, heartbeatIntervalMs, heartbeatIntervalMs, TimeUnit.MILLISECONDS);
    }

    private void stopHeartbeat() {
        if (heartbeatTask != null) {
            heartbeatTask.cancel(false);
            heartbeatTask = null;
        }
    }

    private Long currentSeq() {
        long seq = lastSeq.get();
        return seq < 0 ? null : seq;
    }

    private void send(Payload payload) {
        WebSocketClient current = socket;
        if (current != null && current.isOpen()) {
            current.send(payload.toJson());
        }
    }

    private void closeSocket(int code, String reason) {
        WebSocketClient current = socket;
        if (current != null) {
            try {
                current.close(code, reason);
            } catch (Exception e) {
                log.debug("关闭连接异常: {}", e.getMessage());
            }
        }
    }

    private void onSocketClosed(int code, String reason, boolean remote) {
        stopHeartbeat();
        if (closed) {
            return;
        }
        log.warn("网关连接断开 code={} reason={} remote={}", code, reason, remote);
        if (WsCloseCode.isFatal(code)) {
            log.error("不可恢复错误 code={}（4914=机器人已下架/4915=机器人已封禁），停止连接，请联系官方处理", code);
            closed = true;
            return;
        }
        if (code == WsCloseCode.INVALID_SESSION || code == WsCloseCode.INVALID_SEQ) {
            // 4006 无效 session / 4007 seq 错误：不可 resume，需重新 identify
            sessionId = null;
            forceIdentify = true;
        } else if (sessionId != null && !WsCloseCode.canResume(code) && code >= 4000) {
            // 其他 4xxx 错误按官方建议重新 identify
            sessionId = null;
            forceIdentify = true;
        }
        scheduleReconnect();
    }

    private void scheduleReconnect() {
        if (closed) {
            return;
        }
        int attempt = reconnectAttempts.incrementAndGet();
        long delay = Math.min(config.getReconnectMaxDelayMs(),
                config.getReconnectBaseDelayMs() * (1L << Math.min(attempt - 1, 16)));
        delay += ThreadLocalRandom.current().nextLong(delay / 2 + 1);
        log.info("{}ms 后第 {} 次重连（{}）", delay, attempt,
                sessionId != null && !forceIdentify ? "resume" : "identify");
        try {
            scheduler.schedule(this::connectNew, delay, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.RejectedExecutionException e) {
            log.debug("调度器已关闭，取消重连");
        }
    }

    private class InnerClient extends WebSocketClient {

        InnerClient(URI serverUri) {
            super(serverUri);
        }

        @Override
        public void onOpen(ServerHandshake handshake) {
            log.info("网关 WebSocket 已建立，等待 Hello");
        }

        @Override
        public void onMessage(String message) {
            handlePayload(message);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            onSocketClosed(code, reason, remote);
        }

        @Override
        public void onError(Exception ex) {
            log.error("网关 WebSocket 异常: {}", ex.getMessage(), ex);
        }
    }
}