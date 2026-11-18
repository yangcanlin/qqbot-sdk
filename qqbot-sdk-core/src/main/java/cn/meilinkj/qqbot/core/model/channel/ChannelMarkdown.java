package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 频道 Markdown 消息对象（原生 content 与模板 template_id+params 互斥）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messagemarkdown">MessageMarkdown</a>
 */
public class ChannelMarkdown {

    @JsonProperty("template_id")
    private Integer templateId;

    @JsonProperty("params")
    private List<Param> params;

    /** 原生 markdown 内容，与 template_id/params 互斥 */
    @JsonProperty("content")
    private String content;

    public Integer getTemplateId() {
        return templateId;
    }

    public ChannelMarkdown setTemplateId(Integer templateId) {
        this.templateId = templateId;
        return this;
    }

    public List<Param> getParams() {
        return params;
    }

    public ChannelMarkdown setParams(List<Param> params) {
        this.params = params;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ChannelMarkdown setContent(String content) {
        this.content = content;
        return this;
    }

    /** 见官方 MessageMarkdownParams */
    public static class Param {

        @JsonProperty("key")
        private String key;

        @JsonProperty("values")
        private List<String> values;

        public String getKey() {
            return key;
        }

        public Param setKey(String key) {
            this.key = key;
            return this;
        }

        public List<String> getValues() {
            return values;
        }

        public Param setValues(List<String> values) {
            this.values = values;
            return this;
        }
    }
}