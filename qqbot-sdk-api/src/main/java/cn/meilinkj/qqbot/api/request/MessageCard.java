package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 图文卡片消息（msg_type=8）。
 * type=tuwen 时 content 必须包括 title(1-50字符)、description(1-200字符)、
 * url(https，≤1024字符)、pic_url(https，≤1024字符)。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-MessageCard</a>
 */
public class MessageCard {

    /** 卡片类型：tuwen=图文卡片 */
    @JsonProperty("type")
    private String type;

    @JsonProperty("content")
    private Map<String, String> content;

    public static MessageCard tuwen(String title, String description, String url, String picUrl) {
        Map<String, String> content = new LinkedHashMap<>();
        content.put("title", title);
        content.put("description", description);
        content.put("url", url);
        content.put("pic_url", picUrl);
        return new MessageCard().setType("tuwen").setContent(content);
    }

    public String getType() {
        return type;
    }

    public MessageCard setType(String type) {
        this.type = type;
        return this;
    }

    public Map<String, String> getContent() {
        return content;
    }

    public MessageCard setContent(Map<String, String> content) {
        this.content = content;
        return this;
    }
}