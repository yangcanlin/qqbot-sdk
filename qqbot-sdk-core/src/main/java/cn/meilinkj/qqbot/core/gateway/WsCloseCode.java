package cn.meilinkj.qqbot.core.gateway;

/**
 * WebSocket 关闭码及重连策略，严格对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html#websocket-错误码">WebSocket 错误码</a>
 */
public final class WsCloseCode {

    public static final int INVALID_OPCODE = 4001;
    public static final int INVALID_PAYLOAD = 4002;
    public static final int INVALID_SEQ = 4007;
    public static final int INVALID_SESSION = 4006;
    public static final int RATE_LIMITED = 4008;
    public static final int SESSION_TIMEOUT = 4009;
    public static final int INVALID_SHARD = 4010;
    public static final int SHARDING_REQUIRED = 4011;
    public static final int INVALID_VERSION = 4012;
    public static final int INVALID_INTENT = 4013;
    public static final int DISALLOWED_INTENT = 4014;
    /** 机器人已下架，只允许连接沙箱环境，不可重连。 */
    public static final int BOT_OFFLINE = 4914;
    /** 机器人已封禁，不允许连接，需申请解封。 */
    public static final int BOT_BANNED = 4915;

    private WsCloseCode() {
    }

    /** 是否可重试 RESUME（官方：仅 4008/4009）。 */
    public static boolean canResume(int code) {
        return code == SESSION_TIMEOUT || code == RATE_LIMITED;
    }

    /** 是否可重试 IDENTIFY。 */
    public static boolean canIdentify(int code) {
        if (code == BOT_OFFLINE || code == BOT_BANNED) {
            return false;
        }
        return code != INVALID_OPCODE && code != INVALID_PAYLOAD
                && code != INVALID_SHARD && code != SHARDING_REQUIRED
                && code != INVALID_VERSION && code != INVALID_INTENT
                && code != DISALLOWED_INTENT;
    }

    /** 不可恢复错误：4914/4915 必须停止连接。 */
    public static boolean isFatal(int code) {
        return code == BOT_OFFLINE || code == BOT_BANNED;
    }
}