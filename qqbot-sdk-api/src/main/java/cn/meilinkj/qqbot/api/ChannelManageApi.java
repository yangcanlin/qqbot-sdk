package cn.meilinkj.qqbot.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.api.request.ChannelCreateRequest;
import cn.meilinkj.qqbot.api.request.ChannelUpdateRequest;
import cn.meilinkj.qqbot.api.response.Channel;
import cn.meilinkj.qqbot.api.response.Guild;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 频道管理接口，对齐官方「服务端接口/频道管理/频道管理」小节全部 6 个端点（均 50 QPS）。
 *
 * <p>注意：这一套用的是频道体系的 {@code guild_id}/{@code channel_id}，与群聊单聊的 openid 体系不同；
 * 创建/修改/删除子频道需要管理员权限，且属私域接口（成功后触发 CHANNEL_CREATE/UPDATE/DELETE 事件）。</p>
 *
 * <ul>
 *   <li>获取频道详情：GET /guilds/{guild_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id.get.html">文档</a></li>
 *   <li>获取子频道列表：GET /guilds/{guild_id}/channels
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.get.html">文档</a></li>
 *   <li>创建子频道：POST /guilds/{guild_id}/channels
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/guilds_guild_id_channels.post.html">文档</a></li>
 *   <li>获取子频道详情：GET /channels/{channel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.get.html">文档</a></li>
 *   <li>修改子频道：PATCH /channels/{channel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.patch.html">文档</a></li>
 *   <li>删除子频道：DELETE /channels/{channel_id}
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/channels_channel_id.delete.html">文档</a></li>
 * </ul>
 *
 * <p>频道内发消息、私信、撤回见 {@link ChannelApi}。</p>
 */
public class ChannelManageApi {

    private final HttpApiClient http;

    public ChannelManageApi(HttpApiClient http) {
        this.http = http;
    }

    /** 官方：GET /guilds/{guild_id}（获取频道详情）；50 QPS。 */
    public Guild getGuild(String guildId) {
        return http.get(guildPath(guildId), Guild.class);
    }

    /** 官方：GET /guilds/{guild_id}（获取频道详情）异步版。 */
    public CompletableFuture<Guild> getGuildAsync(String guildId) {
        return http.getAsync(guildPath(guildId), Guild.class);
    }

    /**
     * 官方：GET /guilds/{guild_id}/channels（获取子频道列表）；50 QPS，官方本页无分页参数，一次返回全部。
     */
    public List<Channel> getChannels(String guildId) {
        JsonNode node = http.executeRaw("GET", guildPath(guildId) + "/channels", null);
        return toChannels(node);
    }

    /**
     * 官方：POST /guilds/{guild_id}/channels（创建子频道）；50 QPS，需管理员权限（私域接口），成功触发 CHANNEL_CREATE。
     */
    public Channel createChannel(String guildId, ChannelCreateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(guildPath(guildId) + "/channels", request, Channel.class);
    }

    /** 官方：POST /guilds/{guild_id}/channels（创建子频道）异步版。 */
    public CompletableFuture<Channel> createChannelAsync(String guildId, ChannelCreateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(guildPath(guildId) + "/channels", request, Channel.class);
    }

    /** 官方：GET /channels/{channel_id}（获取子频道详情）；50 QPS。 */
    public Channel getChannel(String channelId) {
        return http.get(channelPath(channelId), Channel.class);
    }

    /** 官方：GET /channels/{channel_id}（获取子频道详情）异步版。 */
    public CompletableFuture<Channel> getChannelAsync(String channelId) {
        return http.getAsync(channelPath(channelId), Channel.class);
    }

    /**
     * 官方：PATCH /channels/{channel_id}（修改子频道）；50 QPS，需管理员权限，局部更新（仅 name/position/parent_id/private_type/speak_permission），
     * 成功触发 CHANNEL_UPDATE。调整排序与移动到其它分组同样走本接口。
     */
    public Channel updateChannel(String channelId, ChannelUpdateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.patch(channelPath(channelId), request, Channel.class);
    }

    /** 官方：PATCH /channels/{channel_id}（修改子频道）异步版。 */
    public CompletableFuture<Channel> updateChannelAsync(String channelId, ChannelUpdateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.patchAsync(channelPath(channelId), request, Channel.class);
    }

    /** 官方：DELETE /channels/{channel_id}（删除子频道）；50 QPS，需管理员权限，无响应体，删除不可恢复，成功触发 CHANNEL_DELETE。 */
    public void deleteChannel(String channelId) {
        http.delete(channelPath(channelId));
    }

    /** 官方：DELETE /channels/{channel_id}（删除子频道）异步版。 */
    public CompletableFuture<Void> deleteChannelAsync(String channelId) {
        return http.deleteAsync(channelPath(channelId));
    }

    private static String guildPath(String guildId) {
        return "/guilds/" + requireNotBlank(guildId, "guildId");
    }

    private static String channelPath(String channelId) {
        return "/channels/" + requireNotBlank(channelId, "channelId");
    }

    private static String requireNotBlank(String value, String name) {
        Objects.requireNonNull(value, name + " 不能为空");
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " 不能为空");
        }
        return value;
    }

    /** 官方接口页的响应示例是数组根，字段表却写成 channels 对象字段，两种形态都兼容。 */
    private static List<Channel> toChannels(JsonNode node) {
        if (node == null || node.isNull()) {
            return Collections.emptyList();
        }
        JsonNode items = node.isArray() ? node : node.path("channels");
        if (!items.isArray()) {
            return Collections.emptyList();
        }
        return JsonUtil.mapper().convertValue(items, new TypeReference<List<Channel>>() {
        });
    }
}
