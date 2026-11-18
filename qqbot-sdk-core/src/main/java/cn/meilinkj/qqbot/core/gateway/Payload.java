package cn.meilinkj.qqbot.core.gateway;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.util.JsonUtil;

/**
 * 网关上下行通用数据结构 Payload，对齐官方文档：
 * <a href="https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html#通用数据结构-payload">通用数据结构 Payload</a>
 *
 * <pre>{@code
 * { "id": "event_id", "op": 0, "d": {}, "s": 42, "t": "GATEWAY_EVENT_NAME" }
 * }</pre>
 */
public class Payload {

    /** 事件 id */
    @JsonProperty("id")
    private String id;

    /** opcode，见 {@link OpCode} */
    @JsonProperty("op")
    private int op;

    /** 事件内容/操作数据 */
    @JsonProperty("d")
    private JsonNode d;

    /** 下行消息序列号，心跳与 resume 时需携带最新值 */
    @JsonProperty("s")
    private Long s;

    /** 事件类型（op=0 Dispatch 时使用） */
    @JsonProperty("t")
    private String t;

    public static Payload parse(String json) {
        return JsonUtil.fromJson(json, Payload.class);
    }

    /** OpCode 2 Identify：token 格式 "QQBot {AccessToken}"。 */
    public static Payload identify(String accessToken, long intents, int[] shard) {
        var d = JsonUtil.mapper().createObjectNode();
        d.put("token", "QQBot " + accessToken);
        d.put("intents", intents);
        var shardNode = d.putArray("shard");
        for (int v : shard) {
            shardNode.add(v);
        }
        var properties = d.putObject("properties");
        properties.put("$os", System.getProperty("os.name", "unknown"));
        properties.put("$browser", "qqbot-sdk-java");
        properties.put("$device", "qqbot-sdk-java");
        Payload p = new Payload();
        p.op = OpCode.IDENTIFY.getCode();
        p.d = d;
        return p;
    }

    /** OpCode 6 Resume：seq 为客户端收到的最新 s。 */
    public static Payload resume(String accessToken, String sessionId, long seq) {
        var d = JsonUtil.mapper().createObjectNode();
        d.put("token", "QQBot " + accessToken);
        d.put("session_id", sessionId);
        d.put("seq", seq);
        Payload p = new Payload();
        p.op = OpCode.RESUME.getCode();
        p.d = d;
        return p;
    }

    /** OpCode 1 Heartbeat：d 为最新 seq，首次连接传 null。 */
    public static Payload heartbeat(Long seq) {
        Payload p = new Payload();
        p.op = OpCode.HEARTBEAT.getCode();
        p.d = seq == null ? JsonUtil.mapper().nullNode() : JsonUtil.mapper().getNodeFactory().numberNode(seq);
        return p;
    }

    public String toJson() {
        return JsonUtil.toJson(this);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getOp() {
        return op;
    }

    public void setOp(int op) {
        this.op = op;
    }

    public JsonNode getD() {
        return d;
    }

    public void setD(JsonNode d) {
        this.d = d;
    }

    public Long getS() {
        return s;
    }

    public void setS(Long s) {
        this.s = s;
    }

    public String getT() {
        return t;
    }

    public void setT(String t) {
        this.t = t;
    }
}