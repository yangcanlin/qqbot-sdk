package cn.meilinkj.qqbot.core.event.friend;

import com.fasterxml.jackson.annotation.JsonProperty;
import cn.meilinkj.qqbot.core.event.BotEvent;

/**
 * FRIEND_DEL（Intents.GROUP_AND_C2C_EVENT）：用户删除机器人好友时触发。
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/event/friend_del.html">用户删除好友</a>
 */
public class FriendDelEvent extends BotEvent {

    /** 删除时间戳（Unix 秒） */
    @JsonProperty("timestamp")
    private Long timestamp;

    /** 用户 OpenID */
    @JsonProperty("openid")
    private String openid;

    /** 用户信息；官方示例 JSON 未出现该字段，实际推送中可能缺失 */
    @JsonProperty("author")
    private FriendAuthor author;

    public Long getTimestamp() {
        return timestamp;
    }

    public FriendDelEvent setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public FriendDelEvent setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public FriendAuthor getAuthor() {
        return author;
    }

    public FriendDelEvent setAuthor(FriendAuthor author) {
        this.author = author;
        return this;
    }

    /** 官方 friend_del 页内联的 FriendAuthor */
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
