package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * V2 Markdown 消息（msg_type=2）。原生 content 与模板字段互斥。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-MessageMarkdown</a>
 */
public class MessageMarkdown {

    /** 【已废弃】平台 Markdown 模板 ID */
    @JsonProperty("template_id")
    private Integer templateId;

    /** Markdown 原生内容 */
    @JsonProperty("content")
    private String content;

    /** 【已废弃】自定义模板 ID，与 template_id 二选一 */
    @JsonProperty("custom_template_id")
    private String customTemplateId;

    /** 是否校验图片转存结果，true 时转存失败将报错且不发送，默认 false */
    @JsonProperty("force_verify_image_resource")
    private Boolean forceVerifyImageResource;

    public static MessageMarkdown ofContent(String content) {
        return new MessageMarkdown().setContent(content);
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public MessageMarkdown setTemplateId(Integer templateId) {
        this.templateId = templateId;
        return this;
    }

    public String getContent() {
        return content;
    }

    public MessageMarkdown setContent(String content) {
        this.content = content;
        return this;
    }

    public String getCustomTemplateId() {
        return customTemplateId;
    }

    public MessageMarkdown setCustomTemplateId(String customTemplateId) {
        this.customTemplateId = customTemplateId;
        return this;
    }

    public Boolean getForceVerifyImageResource() {
        return forceVerifyImageResource;
    }

    public MessageMarkdown setForceVerifyImageResource(Boolean forceVerifyImageResource) {
        this.forceVerifyImageResource = forceVerifyImageResource;
        return this;
    }
}