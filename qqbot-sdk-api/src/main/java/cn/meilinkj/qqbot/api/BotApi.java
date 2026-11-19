package cn.meilinkj.qqbot.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.api.response.BotGuild;
import cn.meilinkj.qqbot.api.response.BotInfo;
import cn.meilinkj.qqbot.api.response.GroupBotState;
import cn.meilinkj.qqbot.api.response.ShareLinkResponse;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 机器人信息接口，对齐官方「服务端接口/机器人」章节：
 *
 * <ul>
 *   <li>获取机器人详情：GET /users/@me（50 QPS）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me.get.html">文档</a></li>
 *   <li>获取机器人频道列表：GET /users/@me/guilds（50 QPS，limit 默认 100，最大 100）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me_guilds.get.html">文档</a></li>
 *   <li>获取机器人群内状态：GET /v2/groups/{group_openid}/bot_state（30 QPM，仅白名单机器人可用，否则报 11253）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_bot_state.get.html">文档</a></li>
 *   <li>生成分享链接：POST /v2/generate_url_link（50 QPS，callback_data 最长 32 字符）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_generate_url_link.post.html">文档</a></li>
 * </ul>
 */
public class BotApi {

    private static final int MAX_GUILD_LIMIT = 100;
    private static final int MAX_CALLBACK_DATA_LENGTH = 32;

    private final HttpApiClient http;

    public BotApi(HttpApiClient http) {
        this.http = http;
    }

    /**
     * 官方：GET /users/@me（获取机器人信息）；50 QPS。返回 id、名称、头像、share_url、welcome_msg 等。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me.get.html">文档</a>
     */
    public BotInfo getBot() {
        return http.get("/users/@me", BotInfo.class);
    }

    public CompletableFuture<BotInfo> getBotAsync() {
        return http.getAsync("/users/@me", BotInfo.class);
    }

    /**
     * 官方：GET /users/@me/guilds（获取频道列表）；50 QPS，默认单页最多 100 条。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/users_me_guilds.get.html">文档</a>
     */
    public List<BotGuild> getGuilds() {
        return getGuilds(null, null, null);
    }

    /**
     * 分页获取机器人加入的频道列表。
     * before 读取此 guildId 之前的数据（设置时先反序再分页）；after 与 before 同时设置时 after 无效。
     */
    public List<BotGuild> getGuilds(String before, String after, Integer limit) {
        if (limit != null && (limit < 1 || limit > MAX_GUILD_LIMIT)) {
            throw new IllegalArgumentException("limit 取值范围为 1~" + MAX_GUILD_LIMIT);
        }
        StringBuilder query = new StringBuilder();
        appendParam(query, "before", before);
        appendParam(query, "after", after);
        appendParam(query, "limit", limit == null ? null : String.valueOf(limit));
        JsonNode node = http.executeRaw("GET", "/users/@me/guilds" + query, null);
        return JsonUtil.mapper().convertValue(node, new TypeReference<List<BotGuild>>() {
        });
    }

    /**
     * 官方：GET /v2/groups/{group_openid}/bot_state（获取机器人群内状态）；30 QPM，仅白名单机器人可用（否则 err_code=11253）。
     * 返回入群时间、主动消息开关、接收消息设置、群内角色。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_bot_state.get.html">文档</a>
     */
    public GroupBotState getGroupBotState(String groupOpenid) {
        return http.get(botStatePath(groupOpenid), GroupBotState.class);
    }

    public CompletableFuture<GroupBotState> getGroupBotStateAsync(String groupOpenid) {
        return http.getAsync(botStatePath(groupOpenid), GroupBotState.class);
    }

    private static String botStatePath(String groupOpenid) {
        Objects.requireNonNull(groupOpenid, "groupOpenid 不能为空");
        if (groupOpenid.isBlank()) {
            throw new IllegalArgumentException("groupOpenid 不能为空");
        }
        return "/v2/groups/" + groupOpenid + "/bot_state";
    }

    /**
     * 官方：POST /v2/generate_url_link（生成分享链接）；50 QPS，邀请用户添加机器人为好友。
     * callbackData 可为 null，最长 32 字符，用户添加后透传给开发者。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_generate_url_link.post.html">文档</a>
     */
    public ShareLinkResponse generateShareLink(String callbackData) {
        Map<String, String> body = new LinkedHashMap<>();
        if (callbackData != null && !callbackData.isBlank()) {
            if (callbackData.length() > MAX_CALLBACK_DATA_LENGTH) {
                throw new IllegalArgumentException("callbackData 最长 " + MAX_CALLBACK_DATA_LENGTH + " 字符");
            }
            body.put("callback_data", callbackData);
        }
        return http.post("/v2/generate_url_link", body, ShareLinkResponse.class);
    }

    private static void appendParam(StringBuilder query, String name, String value) {
        if (value == null || value.isEmpty()) {
            return;
        }
        query.append(query.length() == 0 ? '?' : '&').append(name).append('=').append(encode(value));
    }

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }
}
