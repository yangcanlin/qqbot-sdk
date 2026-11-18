package cn.meilinkj.qqbot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 消息场景上下文。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/c2c_message_create.html">单聊消息事件-MessageScene</a>
 */
public class MessageScene {

    /** 场景来源。default=默认聊天窗口 */
    @JsonProperty("source")
    private String source;

    /**
     * 扩展数据列表，key=value 格式：
     * msg_idx=消息索引（引用场景）、ref_msg_idx=引用的消息索引、auth_token=鉴权令牌
     */
    @JsonProperty("ext")
    private List<String> ext;

    public String getSource() {
        return source;
    }

    public MessageScene setSource(String source) {
        this.source = source;
        return this;
    }

    public List<String> getExt() {
        return ext;
    }

    public MessageScene setExt(List<String> ext) {
        this.ext = ext;
        return this;
    }

    /** 从 ext 中取指定 key 的值（如 msg_idx）。 */
    public String extValue(String key) {
        if (ext == null) {
            return null;
        }
        String prefix = key + "=";
        for (String item : ext) {
            if (item != null && item.startsWith(prefix)) {
                return item.substring(prefix.length());
            }
        }
        return null;
    }
}