package cn.meilinkj.qqbot.core.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Ark 消息对象（template_id 需先向平台申请）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/message/template/model.html#messageark">MessageArk</a>
 */
public class MessageArk {

    @JsonProperty("template_id")
    private Integer templateId;

    @JsonProperty("kv")
    private List<Kv> kv;

    public Integer getTemplateId() {
        return templateId;
    }

    public MessageArk setTemplateId(Integer templateId) {
        this.templateId = templateId;
        return this;
    }

    public List<Kv> getKv() {
        return kv;
    }

    public MessageArk setKv(List<Kv> kv) {
        this.kv = kv;
        return this;
    }

    /** 见官方 MessageArkKv */
    public static class Kv {

        @JsonProperty("key")
        private String key;

        @JsonProperty("value")
        private String value;

        @JsonProperty("obj")
        private List<Obj> obj;

        public String getKey() {
            return key;
        }

        public Kv setKey(String key) {
            this.key = key;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Kv setValue(String value) {
            this.value = value;
            return this;
        }

        public List<Obj> getObj() {
            return obj;
        }

        public Kv setObj(List<Obj> obj) {
            this.obj = obj;
            return this;
        }
    }

    /** 见官方 MessageArkObj */
    public static class Obj {

        @JsonProperty("obj_kv")
        private List<ObjKv> objKv;

        public List<ObjKv> getObjKv() {
            return objKv;
        }

        public Obj setObjKv(List<ObjKv> objKv) {
            this.objKv = objKv;
            return this;
        }
    }

    /** 见官方 MessageArkObjKv */
    public static class ObjKv {

        @JsonProperty("key")
        private String key;

        @JsonProperty("value")
        private String value;

        public String getKey() {
            return key;
        }

        public ObjKv setKey(String key) {
            this.key = key;
            return this;
        }

        public String getValue() {
            return value;
        }

        public ObjKv setValue(String value) {
            this.value = value;
            return this;
        }
    }
}