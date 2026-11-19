package cn.meilinkj.qqbot.core.event.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

import java.util.List;

/**
 * SUBSCRIBE_MESSAGE_STATUS（Intents.GROUP_AND_C2C_EVENT）：用户对订阅消息模板的授权状态变更。
 * 群订阅场景带 group_openid，个人订阅场景带 openid；订阅消息发送需使用 result 中的 subscribe_id。
 *
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/subscribe_message_status.html">订阅消息授权状态变更</a>
 */
public class SubscribeMessageStatusEvent extends BotEvent {

    /** 群 OpenID（群订阅场景时有值） */
    @JsonProperty("group_openid")
    private String groupOpenid;

    /** 用户 OpenID（个人订阅场景时有值） */
    @JsonProperty("openid")
    private String openid;

    @JsonProperty("result")
    private List<TemplateResult> result;

    public String getGroupOpenid() {
        return groupOpenid;
    }

    public SubscribeMessageStatusEvent setGroupOpenid(String groupOpenid) {
        this.groupOpenid = groupOpenid;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public SubscribeMessageStatusEvent setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public List<TemplateResult> getResult() {
        return result;
    }

    public SubscribeMessageStatusEvent setResult(List<TemplateResult> result) {
        this.result = result;
        return this;
    }

    /** 单个订阅模板的授权结果 */
    public static class TemplateResult {

        /** 平台提供的订阅模板 ID */
        @JsonProperty("template_id")
        private Long templateId;

        /** 自定义订阅模板 ID */
        @JsonProperty("custom_template_id")
        private String customTemplateId;

        /** 用户操作：1=允许订阅，2=拒绝订阅 */
        @JsonProperty("op")
        private Integer op;

        /** 订阅 ID，发送订阅消息时需使用 */
        @JsonProperty("subscribe_id")
        private String subscribeId;

        /** 订阅操作时间戳（Unix 秒） */
        @JsonProperty("subscribe_ts")
        private Long subscribeTs;

        /** 订阅状态最后更新时间戳（Unix 秒） */
        @JsonProperty("update_ts")
        private Long updateTs;

        public Long getTemplateId() {
            return templateId;
        }

        public TemplateResult setTemplateId(Long templateId) {
            this.templateId = templateId;
            return this;
        }

        public String getCustomTemplateId() {
            return customTemplateId;
        }

        public TemplateResult setCustomTemplateId(String customTemplateId) {
            this.customTemplateId = customTemplateId;
            return this;
        }

        public Integer getOp() {
            return op;
        }

        public TemplateResult setOp(Integer op) {
            this.op = op;
            return this;
        }

        public String getSubscribeId() {
            return subscribeId;
        }

        public TemplateResult setSubscribeId(String subscribeId) {
            this.subscribeId = subscribeId;
            return this;
        }

        public Long getSubscribeTs() {
            return subscribeTs;
        }

        public TemplateResult setSubscribeTs(Long subscribeTs) {
            this.subscribeTs = subscribeTs;
            return this;
        }

        public Long getUpdateTs() {
            return updateTs;
        }

        public TemplateResult setUpdateTs(Long updateTs) {
            this.updateTs = updateTs;
            return this;
        }
    }
}
