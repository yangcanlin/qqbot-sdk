package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.ChannelMessageRequest;
import cn.meilinkj.qqbot.api.response.Dms;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.model.channel.Message;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * QQ 频道（Guild）消息接口。与群聊/单聊 V2 接口相互独立：
 * 使用 channel_id / guild_id，而非 group_openid / user_openid。
 *
 * <ul>
 *   <li>发送子频道消息：POST /channels/{channel_id}/messages（每子频道 5 条/秒，被动回复 5 分钟有效）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html">文档</a></li>
 *   <li>撤回消息：DELETE /channels/{channel_id}/messages/{message_id}?hidetip=（私域机器人）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/recall.html">文档</a></li>
 *   <li>私信：POST /users/@me/dms 创建会话，POST /dms/{guild_id}/messages 发送
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/dms.html">文档</a></li>
 * </ul>
 *
 * <p>注意：频道发消息要求机器人通过 WebSocket 保持在线。</p>
 */
public class ChannelApi {

    private final HttpApiClient http;

    public ChannelApi(HttpApiClient http) {
        this.http = http;
    }

    /**
     * 官方：POST /channels/{channel_id}/messages（发表频道发言）；每子频道 5 条/秒，msg_id 被动回复 5 分钟内有效（无 msg_seq）。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html">文档</a>
     */
    public Message sendMessage(String channelId, ChannelMessageRequest request) {
        Objects.requireNonNull(channelId, "channelId 不能为空");
        Objects.requireNonNull(request, "request 不能为空");
        return http.post("/channels/" + channelId + "/messages", request, Message.class);
    }

    public CompletableFuture<Message> sendMessageAsync(String channelId, ChannelMessageRequest request) {
        Objects.requireNonNull(channelId, "channelId 不能为空");
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync("/channels/" + channelId + "/messages", request, Message.class);
    }

    /** 官方：DELETE /channels/{channel_id}/messages/{message_id}?hidetip=（撤回频道消息，私域机器人）；hideTip=true 隐藏撤回提示小灰条。 */
    public void recall(String channelId, String messageId, boolean hideTip) {
        Objects.requireNonNull(channelId, "channelId 不能为空");
        Objects.requireNonNull(messageId, "messageId 不能为空");
        http.delete("/channels/" + channelId + "/messages/" + messageId + "?hidetip=" + hideTip);
    }

    /** 官方：POST /users/@me/dms（创建私信会话）。recipientId 为接收者用户 ID，sourceGuildId 为来源频道 ID。 */
    public Dms createDms(String recipientId, String sourceGuildId) {
        Objects.requireNonNull(recipientId, "recipientId 不能为空");
        Objects.requireNonNull(sourceGuildId, "sourceGuildId 不能为空");
        Map<String, String> body = new LinkedHashMap<>();
        body.put("recipient_id", recipientId);
        body.put("source_guild_id", sourceGuildId);
        return http.post("/users/@me/dms", body, Dms.class);
    }

    /** 官方：POST /dms/{guild_id}/messages（发送私信消息），dmsGuildId 来自 createDms 返回的 guild_id。 */
    public Message sendDms(String dmsGuildId, ChannelMessageRequest request) {
        Objects.requireNonNull(dmsGuildId, "dmsGuildId 不能为空");
        Objects.requireNonNull(request, "request 不能为空");
        return http.post("/dms/" + dmsGuildId + "/messages", request, Message.class);
    }

    /** 官方：DELETE /dms/{guild_id}/messages/{message_id}?hidetip=（撤回私信消息）。 */
    public void recallDms(String dmsGuildId, String messageId, boolean hideTip) {
        Objects.requireNonNull(dmsGuildId, "dmsGuildId 不能为空");
        Objects.requireNonNull(messageId, "messageId 不能为空");
        http.delete("/dms/" + dmsGuildId + "/messages/" + messageId + "?hidetip=" + hideTip);
    }
}