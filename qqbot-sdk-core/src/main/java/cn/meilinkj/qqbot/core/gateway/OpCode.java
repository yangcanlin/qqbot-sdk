package cn.meilinkj.qqbot.core.gateway;

/**
 * 网关 OpCode，严格对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#opcode-含义">OpCode 含义</a>
 */
public enum OpCode {

    /** 服务端进行消息推送（webhook/websocket，Receive） */
    DISPATCH(0),
    /** 客户端或服务端发送心跳（websocket，Send/Receive） */
    HEARTBEAT(1),
    /** 客户端发送鉴权（websocket，Send） */
    IDENTIFY(2),
    /** 客户端恢复连接（websocket，Send） */
    RESUME(6),
    /** 服务端通知客户端重新连接（websocket，Receive） */
    RECONNECT(7),
    /** identify/resume 参数错误（websocket，Receive） */
    INVALID_SESSION(9),
    /** 建立 ws 连接后网关下发的第一条消息，携带心跳周期（websocket，Receive） */
    HELLO(10),
    /** 心跳成功回执（websocket，Receive/Reply） */
    HEARTBEAT_ACK(11),
    /** http 回调模式回包（webhook，Reply） */
    HTTP_CALLBACK_ACK(12),
    /** 回调地址验证（webhook，Receive） */
    CALLBACK_VERIFY(13);

    private final int code;

    OpCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static OpCode fromCode(int code) {
        for (OpCode op : values()) {
            if (op.code == code) {
                return op;
            }
        }
        return null;
    }
}