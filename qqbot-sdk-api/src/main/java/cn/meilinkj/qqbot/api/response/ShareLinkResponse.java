package cn.meilinkj.qqbot.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 机器人分享链接响应（POST /v2/generate_url_link，50 QPS）。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_generate_url_link.post.html">生成分享链接</a>
 */
public class ShareLinkResponse {

    @JsonProperty("data")
    private Data data;

    public Data getData() {
        return data;
    }

    /** 分享链接快捷读取，data 缺失时返回 null。 */
    public String getUrl() {
        return data == null ? null : data.getUrl();
    }

    public static class Data {

        /** 生成的分享链接，用户通过该链接添加机器人为好友，callback_data 透传给开发者后台 */
        @JsonProperty("url")
        private String url;

        public String getUrl() {
            return url;
        }
    }
}
