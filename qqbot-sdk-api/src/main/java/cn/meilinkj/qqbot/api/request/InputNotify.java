package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 输入中状态（仅单聊，msg_type=6）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html">发送单聊消息-InputNotify</a>
 */
public class InputNotify {

    /** 填 1 */
    @JsonProperty("input_type")
    private Integer inputType;

    /** 状态持续时间，最长 60s */
    @JsonProperty("input_second")
    private Integer inputSecond;

    public static InputNotify typing(int seconds) {
        if (seconds < 1 || seconds > 60) {
            throw new IllegalArgumentException("input_second 取值 1~60");
        }
        return new InputNotify().setInputType(1).setInputSecond(seconds);
    }

    public Integer getInputType() {
        return inputType;
    }

    public InputNotify setInputType(Integer inputType) {
        this.inputType = inputType;
        return this;
    }

    public Integer getInputSecond() {
        return inputSecond;
    }

    public InputNotify setInputSecond(Integer inputSecond) {
        this.inputSecond = inputSecond;
        return this;
    }
}