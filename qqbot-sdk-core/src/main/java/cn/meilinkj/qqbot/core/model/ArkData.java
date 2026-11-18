package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 结构化卡片消息数据（接收方向）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_message_create.html">单聊消息事件-ARKData</a>
 */
public class ArkData {

    /** 卡片消息中的用户操作提示文本 */
    @JsonProperty("prompt")
    private String prompt;

    /** 卡片类型：tuwen/feed/miniapp/map/contact_card/video_share/music_together/picture */
    @JsonProperty("ark_type")
    private String arkType;

    /** 卡片消息类型的中文名称 */
    @JsonProperty("ark_name")
    private String arkName;

    /** 卡片字段：title/desc/jump_url/preview/source 等 */
    @JsonProperty("fields")
    private JsonNode fields;

    public String getPrompt() {
        return prompt;
    }

    public ArkData setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public String getArkType() {
        return arkType;
    }

    public ArkData setArkType(String arkType) {
        this.arkType = arkType;
        return this;
    }

    public String getArkName() {
        return arkName;
    }

    public ArkData setArkName(String arkName) {
        this.arkName = arkName;
        return this;
    }

    public JsonNode getFields() {
        return fields;
    }

    public ArkData setFields(JsonNode fields) {
        this.fields = fields;
        return this;
    }
}