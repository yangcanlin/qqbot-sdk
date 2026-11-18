package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 消息附件（图片、文件、语音等）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/group_at_message_create.html">群@机器人消息-MessageAttachment</a>
 */
public class MessageAttachment {

    /** 附件下载 URL */
    @JsonProperty("url")
    private String url;

    @JsonProperty("filename")
    private String filename;

    /** 图片宽度（像素），非图片附件无此字段 */
    @JsonProperty("width")
    private Integer width;

    /** 图片高度（像素） */
    @JsonProperty("height")
    private Integer height;

    /** 文件大小（字节） */
    @JsonProperty("size")
    private Integer size;

    /** MIME 类型：voice/image/jpeg/image/png/image/gif/video/mp4/file */
    @JsonProperty("content_type")
    private String contentType;

    /** 语音消息 SILK 等转换后的 WAV 文件 URL */
    @JsonProperty("voice_wav_url")
    private String voiceWavUrl;

    /** 语音消息 ASR 参考结果 */
    @JsonProperty("asr_refer_text")
    private String asrReferText;

    public String getUrl() {
        return url;
    }

    public MessageAttachment setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getFilename() {
        return filename;
    }

    public MessageAttachment setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public MessageAttachment setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public MessageAttachment setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getSize() {
        return size;
    }

    public MessageAttachment setSize(Integer size) {
        this.size = size;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public MessageAttachment setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String getVoiceWavUrl() {
        return voiceWavUrl;
    }

    public MessageAttachment setVoiceWavUrl(String voiceWavUrl) {
        this.voiceWavUrl = voiceWavUrl;
        return this;
    }

    public String getAsrReferText() {
        return asrReferText;
    }

    public MessageAttachment setAsrReferText(String asrReferText) {
        this.asrReferText = asrReferText;
        return this;
    }
}