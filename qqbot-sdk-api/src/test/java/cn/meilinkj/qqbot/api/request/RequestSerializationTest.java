package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.api.support.MsgSeqAllocator;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 请求体序列化测试，字段名与结构对齐官方发送消息接口文档：
 * https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html
 */
class RequestSerializationTest {

    @Test
    void textMessageWithPassiveReply() {
        MessageRequest req = MessageRequest.text("hello").replyTo("MSG1", 2);
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(req));
        assertEquals(0, json.get("msg_type").asInt());
        assertEquals("hello", json.get("content").asText());
        assertEquals("MSG1", json.get("msg_id").asText());
        assertEquals(2, json.get("msg_seq").asInt());
        assertFalse(json.has("markdown"));
        assertFalse(json.has("media"));
    }

    @Test
    void mediaMessage() {
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(MessageRequest.media("FILE_INFO")));
        assertEquals(7, json.get("msg_type").asInt());
        assertEquals("FILE_INFO", json.get("media").get("file_info").asText());
    }

    @Test
    void markdownMessage() {
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(
                MessageRequest.markdown(MessageMarkdown.ofContent("# title"))));
        assertEquals(2, json.get("msg_type").asInt());
        assertEquals("# title", json.get("markdown").get("content").asText());
    }

    @Test
    void cardMessage() {
        MessageCard card = MessageCard.tuwen("标题", "描述", "https://example.com", "https://example.com/a.png");
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(MessageRequest.card(card)));
        assertEquals(8, json.get("msg_type").asInt());
        assertEquals("tuwen", json.get("card").get("type").asText());
        assertEquals("标题", json.get("card").get("content").get("title").asText());
    }

    @Test
    void keyboardStructureMatchesOfficialSchema() {
        Keyboard.Button button = new Keyboard.Button()
                .setId("btn1")
                .setRenderData(new Keyboard.RenderData().setLabel("点赞").setVisitedLabel("已赞").setStyle(1))
                .setAction(new Keyboard.Action()
                        .setType(1)
                        .setData("like")
                        .setPermission(Keyboard.Permission.all())
                        .setUnsupportTips("请升级版本"));
        Keyboard keyboard = Keyboard.ofRows(List.of(Keyboard.Row.of(button)));
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(keyboard));

        JsonNode btn = json.get("content").get("rows").get(0).get("buttons").get(0);
        assertEquals("btn1", btn.get("id").asText());
        assertEquals("点赞", btn.get("render_data").get("label").asText());
        assertEquals("已赞", btn.get("render_data").get("visited_label").asText());
        assertEquals(1, btn.get("action").get("type").asInt());
        assertEquals("like", btn.get("action").get("data").asText());
        assertEquals(2, btn.get("action").get("permission").get("type").asInt());
        assertEquals("请升级版本", btn.get("action").get("unsupport_tips").asText());
    }

    @Test
    void inputNotifyValidation() {
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(InputNotify.typing(30)));
        assertEquals(1, json.get("input_type").asInt());
        assertEquals(30, json.get("input_second").asInt());
        assertThrows(IllegalArgumentException.class, () -> InputNotify.typing(0));
        assertThrows(IllegalArgumentException.class, () -> InputNotify.typing(61));
    }

    @Test
    void uploadPrepareUsesStringSizes() {
        UploadPrepareRequest req = new UploadPrepareRequest(2, 123456789L, "v.mp4", "MD5", "SHA1", "MD510M");
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(req));
        assertEquals(2, json.get("file_type").asInt());
        assertTrue(json.get("file_size").isTextual());
        assertEquals("123456789", json.get("file_size").asText());
        assertEquals("MD510M", json.get("md5_10m").asText());
    }

    @Test
    void uploadPartFinishUsesStringBlockSize() {
        UploadPartFinishRequest req = new UploadPartFinishRequest("UP1", 0, 1024, "MD5");
        JsonNode json = JsonUtil.tree(JsonUtil.toJson(req));
        assertEquals("UP1", json.get("upload_id").asText());
        assertEquals(0, json.get("part_index").asInt());
        assertTrue(json.get("block_size").isTextual());
        assertEquals("1024", json.get("block_size").asText());
    }

    @Test
    void msgSeqAllocatorIncrementsPerMsgId() {
        MsgSeqAllocator allocator = new MsgSeqAllocator();
        assertEquals(1, allocator.next("M1"));
        assertEquals(2, allocator.next("M1"));
        assertEquals(1, allocator.next("M2"));
        assertEquals(3, allocator.next("M1"));
    }
}