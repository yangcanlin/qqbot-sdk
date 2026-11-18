package cn.meilinkj.qqbot.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 富媒体消息 media 字段（msg_type=7）。file_info 来自文件上传接口返回值，直接透传。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">发送群聊消息-MediaInfo</a>
 */
public class MediaInfo {

    @JsonProperty("file_info")
    private String fileInfo;

    public static MediaInfo of(String fileInfo) {
        return new MediaInfo().setFileInfo(fileInfo);
    }

    public String getFileInfo() {
        return fileInfo;
    }

    public MediaInfo setFileInfo(String fileInfo) {
        this.fileInfo = fileInfo;
        return this;
    }
}