package cn.meilinkj.qqbot.core.event.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * FRIEND_ADD：用户添加机器人好友。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/friend_add.html">用户添加好友</a>
 */
public class FriendAddEvent extends BotEvent {

    /** 添加时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    /** 用户 OpenID */
    @JsonProperty("openid")
    private String openid;

    /**
     * 加好友场景值：1000=缺省默认, 1001=网络搜索(全部tab), 1002=网络搜索(机器人tab),
     * 1003=群场景, 1004=空间场景, 2001=站内分享资料页, 2002=站外分享资料页,
     * 2003=开发者生成的分享链接(站内), 2004=开发者生成的分享链接(站外)
     */
    @JsonProperty("scene")
    private Integer scene;

    /** 开发者自定义的回调数据（callback_data），用于区分不同来源 */
    @JsonProperty("scene_param")
    private String sceneParam;

    @JsonProperty("author")
    private FriendAuthor author;

    /** 机器人分享链接的短链 code */
    @JsonProperty("short_code")
    private String shortCode;

    public Long getTimestamp() {
        return timestamp;
    }

    public FriendAddEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public FriendAddEvent setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public Integer getScene() {
        return scene;
    }

    public FriendAddEvent setScene(Integer scene) {
        this.scene = scene;
        return this;
    }

    public String getSceneParam() {
        return sceneParam;
    }

    public FriendAddEvent setSceneParam(String sceneParam) {
        this.sceneParam = sceneParam;
        return this;
    }

    public FriendAuthor getAuthor() {
        return author;
    }

    public FriendAddEvent setAuthor(FriendAuthor author) {
        this.author = author;
        return this;
    }

    public String getShortCode() {
        return shortCode;
    }

    public FriendAddEvent setShortCode(String shortCode) {
        this.shortCode = shortCode;
        return this;
    }

    /** 见官方 FriendAuthor */
    public static class FriendAuthor {

        /** 用户统一 OpenID（跨应用标识） */
        @JsonProperty("union_openid")
        private String unionOpenid;

        public String getUnionOpenid() {
            return unionOpenid;
        }

        public FriendAuthor setUnionOpenid(String unionOpenid) {
            this.unionOpenid = unionOpenid;
            return this;
        }
    }
}