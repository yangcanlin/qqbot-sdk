package cn.meilinkj.qqbot.core.gateway;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 协议常量与 Payload 结构测试，断言值来自官方文档：
 * https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html
 */
class ProtocolTest {

    @Test
    void intentsMasksMatchOfficialDocs() {
        assertEquals(1L << 0, Intents.GUILDS.getValue());
        assertEquals(1L << 1, Intents.GUILD_MEMBERS.getValue());
        assertEquals(1L << 9, Intents.GUILD_MESSAGES.getValue());
        assertEquals(1L << 10, Intents.GUILD_MESSAGE_REACTIONS.getValue());
        assertEquals(1L << 12, Intents.DIRECT_MESSAGE.getValue());
        assertEquals(1L << 25, Intents.GROUP_AND_C2C_EVENT.getValue());
        assertEquals(1L << 26, Intents.INTERACTION.getValue());
        assertEquals(1L << 27, Intents.MESSAGE_AUDIT.getValue());
        assertEquals(1L << 28, Intents.FORUMS_EVENT.getValue());
        assertEquals(1L << 29, Intents.AUDIO_ACTION.getValue());
        assertEquals(1L << 30, Intents.PUBLIC_GUILD_MESSAGES.getValue());
    }

    @Test
    void intentsOfCombinesByBitOr() {
        long mask = Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.INTERACTION);
        assertEquals((1L << 25) | (1L << 26), mask);
        assertEquals(0, Intents.of());
    }

    @Test
    void opCodesMatchOfficialDocs() {
        assertEquals(0, OpCode.DISPATCH.getCode());
        assertEquals(1, OpCode.HEARTBEAT.getCode());
        assertEquals(2, OpCode.IDENTIFY.getCode());
        assertEquals(6, OpCode.RESUME.getCode());
        assertEquals(7, OpCode.RECONNECT.getCode());
        assertEquals(9, OpCode.INVALID_SESSION.getCode());
        assertEquals(10, OpCode.HELLO.getCode());
        assertEquals(11, OpCode.HEARTBEAT_ACK.getCode());
        assertEquals(12, OpCode.HTTP_CALLBACK_ACK.getCode());
        assertEquals(13, OpCode.CALLBACK_VERIFY.getCode());
        assertEquals(OpCode.HELLO, OpCode.fromCode(10));
        assertNull(OpCode.fromCode(99));
    }

    @Test
    void wsCloseCodeRulesMatchOfficialDocs() {
        // 4009 可 resume；4006/4007 不可 resume 但可 identify；4914/4915 致命
        assertTrue(WsCloseCode.canResume(WsCloseCode.SESSION_TIMEOUT));
        assertFalse(WsCloseCode.canResume(WsCloseCode.INVALID_SESSION));
        assertFalse(WsCloseCode.canResume(WsCloseCode.INVALID_SEQ));
        assertTrue(WsCloseCode.canIdentify(WsCloseCode.INVALID_SESSION));
        assertTrue(WsCloseCode.canIdentify(WsCloseCode.INVALID_SEQ));
        assertTrue(WsCloseCode.canIdentify(WsCloseCode.RATE_LIMITED));
        assertTrue(WsCloseCode.isFatal(WsCloseCode.BOT_OFFLINE));
        assertTrue(WsCloseCode.isFatal(WsCloseCode.BOT_BANNED));
        assertFalse(WsCloseCode.isFatal(WsCloseCode.SESSION_TIMEOUT));
    }

    @Test
    void identifyPayloadMatchesOfficialStructure() {
        Payload p = Payload.identify("TOKEN", Intents.of(Intents.GROUP_AND_C2C_EVENT), new int[]{0, 4});
        JsonNode json = JsonUtil.tree(p.toJson());
        assertEquals(2, json.get("op").asInt());
        JsonNode d = json.get("d");
        assertEquals("QQBot TOKEN", d.get("token").asText());
        assertEquals(1L << 25, d.get("intents").asLong());
        assertEquals(0, d.get("shard").get(0).asInt());
        assertEquals(4, d.get("shard").get(1).asInt());
        assertTrue(d.get("properties").has("$os"));
        assertTrue(d.get("properties").has("$browser"));
        assertTrue(d.get("properties").has("$device"));
    }

    @Test
    void resumePayloadMatchesOfficialStructure() {
        Payload p = Payload.resume("TOKEN", "session-1", 42);
        JsonNode json = JsonUtil.tree(p.toJson());
        assertEquals(6, json.get("op").asInt());
        assertEquals("QQBot TOKEN", json.get("d").get("token").asText());
        assertEquals("session-1", json.get("d").get("session_id").asText());
        assertEquals(42, json.get("d").get("seq").asLong());
    }

    @Test
    void heartbeatCarriesLatestSeqOrNull() {
        JsonNode withSeq = JsonUtil.tree(Payload.heartbeat(42L).toJson());
        assertEquals(1, withSeq.get("op").asInt());
        assertEquals(42, withSeq.get("d").asLong());

        JsonNode first = JsonUtil.tree(Payload.heartbeat(null).toJson());
        assertTrue(first.has("d"));
        assertTrue(first.get("d").isNull());
    }

    @Test
    void payloadParseRoundTrip() {
        Payload p = Payload.parse("{\"id\":\"EV1\",\"op\":0,\"d\":{\"x\":1},\"s\":7,\"t\":\"GROUP_AT_MESSAGE_CREATE\"}");
        assertEquals("EV1", p.getId());
        assertEquals(0, p.getOp());
        assertEquals(1, p.getD().get("x").asInt());
        assertEquals(7L, p.getS());
        assertEquals("GROUP_AT_MESSAGE_CREATE", p.getT());
    }
}