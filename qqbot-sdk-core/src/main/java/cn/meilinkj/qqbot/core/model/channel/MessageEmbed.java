package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Embed 消息（一种特殊的 ark）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messageembed">MessageEmbed</a>
 */
public class MessageEmbed {

    @JsonProperty("title")
    private String title;

    /** 消息弹窗内容 */
    @JsonProperty("prompt")
    private String prompt;

    @JsonProperty("thumbnail")
    private Thumbnail thumbnail;

    @JsonProperty("fields")
    private List<Field> fields;

    public String getTitle() {
        return title;
    }

    public MessageEmbed setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getPrompt() {
        return prompt;
    }

    public MessageEmbed setPrompt(String prompt) {
        this.prompt = prompt;
        return this;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }

    public MessageEmbed setThumbnail(Thumbnail thumbnail) {
        this.thumbnail = thumbnail;
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public MessageEmbed setFields(List<Field> fields) {
        this.fields = fields;
        return this;
    }

    /** 见官方 MessageEmbedThumbnail */
    public static class Thumbnail {

        @JsonProperty("url")
        private String url;

        public String getUrl() {
            return url;
        }

        public Thumbnail setUrl(String url) {
            this.url = url;
            return this;
        }
    }

    /** 见官方 MessageEmbedField */
    public static class Field {

        @JsonProperty("name")
        private String name;

        public String getName() {
            return name;
        }

        public Field setName(String name) {
            this.name = name;
            return this;
        }
    }
}