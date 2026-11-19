package cn.meilinkj.qqbot.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import cn.meilinkj.qqbot.api.request.BlacklistUserRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalOpRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalStrategyRequest;
import cn.meilinkj.qqbot.api.request.JoinApprovalStrategyUpdateRequest;
import cn.meilinkj.qqbot.api.request.MuteSettingRequest;
import cn.meilinkj.qqbot.api.request.StrategyWhitelistUsersRequest;
import cn.meilinkj.qqbot.api.response.BlacklistEntry;
import cn.meilinkj.qqbot.api.response.BlacklistOperateResult;
import cn.meilinkj.qqbot.api.response.GroupInfo;
import cn.meilinkj.qqbot.api.response.GroupMember;
import cn.meilinkj.qqbot.api.response.JoinApprovalStrategy;
import cn.meilinkj.qqbot.api.response.JoinRequest;
import cn.meilinkj.qqbot.api.response.MuteSetting;
import cn.meilinkj.qqbot.api.response.Page;
import cn.meilinkj.qqbot.api.response.StrategyOperateResult;
import cn.meilinkj.qqbot.api.response.StrategyWhitelistResult;
import cn.meilinkj.qqbot.api.support.Paging;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * 群聊管理接口，对齐官方「服务端接口/群聊管理」章节（群信息/群成员/黑名单/禁言/入群申请/自动审批策略）。
 *
 * <ul>
 *   <li>获取群基本信息：GET /v2/groups/{group_openid}/info（30 QPM，白名单开放，否则 err_code=11253）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_info.get.html">文档</a></li>
 *   <li>获取群成员列表：GET /v2/groups/{group_openid}/members（60 QPM，白名单，单页最多 30）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members.get.html">文档</a></li>
 *   <li>获取群成员信息：GET /v2/groups/{group_openid}/members/{member_openid}（30 QPM，白名单）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_members_member_openid.get.html">文档</a></li>
 *   <li>群成员批量移除：POST /v2/groups/{group_openid}/batch_remove_members（30 QPM，单次≤20）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_batch_remove_members.post.html">文档</a></li>
 *   <li>群黑名单查询：GET /v2/groups/{group_openid}/member_blacklist（30 QPM，limit 默认 20 最大 100）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.get.html">文档</a></li>
 *   <li>群黑名单操作：POST /v2/groups/{group_openid}/member_blacklist（60 QPM，单次≤20）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_member_blacklist.post.html">文档</a></li>
 *   <li>查询群禁言状态：GET /v2/groups/{group_openid}/restrict_chat_setting（30 QPM）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.get.html">文档</a></li>
 *   <li>设置群成员禁言：POST /v2/groups/{group_openid}/restrict_chat_setting（60 QPM，单次≤20，最长 30 天）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_restrict_chat_setting.post.html">文档</a></li>
 *   <li>入群申请列表拉取：GET /v2/groups/{group_openid}/join_request_list（30 QPM，需群管理员，limit 默认 20 最大 50）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_join_request_list.get.html">文档</a></li>
 *   <li>入群申请审批：POST /v2/groups/{group_openid}/approval_join_request/{member_openid}（60 QPM，需群管理员）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_approval_join_request_member_openid.post.html">文档</a></li>
 *   <li>查询入群自动审批策略列表：GET /v2/groups/join_approval_strategy（60 QPM，limit 默认 20 最大 50）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.get.html">文档</a></li>
 *   <li>创建入群自动审批策略：POST /v2/groups/join_approval_strategy（60 QPM，最多 20 个策略）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy.post.html">文档</a></li>
 *   <li>修改入群自动审批策略：PATCH /v2/groups/join_approval_strategy/{strategy_id}（60 QPM）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.patch.html">文档</a></li>
 *   <li>删除入群自动审批策略：DELETE /v2/groups/join_approval_strategy/{strategy_id}（60 QPM）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id.delete.html">文档</a></li>
 *   <li>执行入群自动审批策略：POST /v2/groups/join_approval_strategy/{strategy_id}/execute（60 QPM，异步执行约 10 分钟）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_execute.post.html">文档</a></li>
 *   <li>修改入群自动审批策略的白名单号码：POST /v2/groups/join_approval_strategy/{strategy_id}/whitelist_users（60 QPM，单次≤10000）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_join_approval_strategy_strategy_id_whitelist_users.post.html">文档</a></li>
 * </ul>
 */
public class GroupManageApi {

    private static final int MAX_REMOVE_MEMBER_SIZE = 20;
    private static final String STRATEGY_BASE = "/v2/groups/join_approval_strategy";

    private final HttpApiClient http;

    public GroupManageApi(HttpApiClient http) {
        this.http = http;
    }

    private static String base(String groupOpenid) {
        Objects.requireNonNull(groupOpenid, "groupOpenid 不能为空");
        if (groupOpenid.isBlank()) {
            throw new IllegalArgumentException("groupOpenid 不能为空");
        }
        return "/v2/groups/" + groupOpenid;
    }

    private static String strategyPath(String strategyId) {
        Objects.requireNonNull(strategyId, "strategyId 不能为空");
        if (strategyId.isBlank()) {
            throw new IllegalArgumentException("strategyId 不能为空");
        }
        return STRATEGY_BASE + "/" + strategyId;
    }

    /** 官方：GET /v2/groups/{group_openid}/info（获取群基本信息）；30 QPM，白名单开放，未开通报 err_code=11253。 */
    public GroupInfo getGroupInfo(String groupOpenid) {
        return http.get(base(groupOpenid) + "/info", GroupInfo.class);
    }

    /** 官方：GET /v2/groups/{group_openid}/info（获取群基本信息）异步版。 */
    public CompletableFuture<GroupInfo> getGroupInfoAsync(String groupOpenid) {
        return http.getAsync(base(groupOpenid) + "/info", GroupInfo.class);
    }

    /** 官方：GET /v2/groups/{group_openid}/members（获取群成员列表）；60 QPM，白名单，单页最多 30。 */
    public Page<GroupMember> getGroupMembers(String groupOpenid, String cursor) {
        JsonNode node = http.executeRaw("GET", base(groupOpenid) + "/members" + Paging.cursor(cursor), null);
        return Paging.page(node, "members", items -> JsonUtil.mapper().convertValue(items, new TypeReference<List<GroupMember>>() {
        }));
    }

    /** 官方：GET /v2/groups/{group_openid}/members/{member_openid}（获取群成员信息）；30 QPM，白名单。 */
    public GroupMember getGroupMember(String groupOpenid, String memberOpenid) {
        return http.get(memberPath(groupOpenid, memberOpenid), GroupMember.class);
    }

    /** 官方：GET /v2/groups/{group_openid}/members/{member_openid}（获取群成员信息）异步版。 */
    public CompletableFuture<GroupMember> getGroupMemberAsync(String groupOpenid, String memberOpenid) {
        return http.getAsync(memberPath(groupOpenid, memberOpenid), GroupMember.class);
    }

    private static String memberPath(String groupOpenid, String memberOpenid) {
        Objects.requireNonNull(memberOpenid, "memberOpenid 不能为空");
        if (memberOpenid.isBlank()) {
            throw new IllegalArgumentException("memberOpenid 不能为空");
        }
        return base(groupOpenid) + "/members/" + memberOpenid;
    }

    /** 官方：POST /v2/groups/{group_openid}/batch_remove_members（群成员批量移除）；30 QPM，单次最多 20 个；成功返回 "success"。 */
    public String batchRemoveMembers(String groupOpenid, List<String> memberOpenids) {
        if (memberOpenids == null || memberOpenids.isEmpty()) {
            throw new IllegalArgumentException("memberOpenids 不能为空");
        }
        if (memberOpenids.size() > MAX_REMOVE_MEMBER_SIZE) {
            throw new IllegalArgumentException("单次最多移除 " + MAX_REMOVE_MEMBER_SIZE + " 个成员");
        }
        Map<String, Object> body = Collections.singletonMap("member_openids", memberOpenids);
        JsonNode node = http.executeRaw("POST", base(groupOpenid) + "/batch_remove_members", body);
        return node == null ? null : node.path("remove_members_result").asText(null);
    }

    /** 官方：GET /v2/groups/{group_openid}/member_blacklist（群黑名单查询）；30 QPM，limit 默认 20、最大 100。 */
    public Page<BlacklistEntry> getBlacklistMembers(String groupOpenid, String cursor, Integer limit) {
        if (limit != null && (limit < 1 || limit > 100)) {
            throw new IllegalArgumentException("limit 取值范围为 1~100");
        }
        StringBuilder query = Paging.cursor(cursor);
        Paging.add(query, "limit", limit == null ? null : String.valueOf(limit));
        JsonNode node = http.executeRaw("GET", base(groupOpenid) + "/member_blacklist" + query, null);
        return Paging.page(node, "users", items -> JsonUtil.mapper().convertValue(items, new TypeReference<List<BlacklistEntry>>() {
        }));
    }

    /** 官方：POST /v2/groups/{group_openid}/member_blacklist（群黑名单操作）；60 QPM，返回操作失败列表 fail_openids。 */
    public BlacklistOperateResult operateBlacklist(String groupOpenid, BlacklistUserRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(base(groupOpenid) + "/member_blacklist", request, BlacklistOperateResult.class);
    }

    /** 官方：GET /v2/groups/{group_openid}/restrict_chat_setting（查询群禁言状态）；30 QPM。 */
    public MuteSetting getMuteSetting(String groupOpenid) {
        return http.get(mutePath(groupOpenid), MuteSetting.class);
    }

    /** 官方：GET /v2/groups/{group_openid}/restrict_chat_setting（查询群禁言状态）异步版。 */
    public CompletableFuture<MuteSetting> getMuteSettingAsync(String groupOpenid) {
        return http.getAsync(mutePath(groupOpenid), MuteSetting.class);
    }

    /** 官方：POST /v2/groups/{group_openid}/restrict_chat_setting（设置群成员禁言）；60 QPM，机器人需群管理员/群主身份。 */
    public void setGroupMute(String groupOpenid, MuteSettingRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        http.post(mutePath(groupOpenid), request, Void.class);
    }

    /** 官方：POST /v2/groups/{group_openid}/restrict_chat_setting（设置群成员禁言）异步版。 */
    public CompletableFuture<Void> setGroupMuteAsync(String groupOpenid, MuteSettingRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(mutePath(groupOpenid), request, Void.class);
    }

    private static String mutePath(String groupOpenid) {
        return base(groupOpenid) + "/restrict_chat_setting";
    }

    /** 官方：GET /v2/groups/{group_openid}/join_request_list（入群申请列表拉取）；30 QPM，需群管理员，limit 默认 20、最大 50。 */
    public Page<JoinRequest> getJoinRequests(String groupOpenid, String cursor, Integer limit) {
        if (limit != null && (limit < 1 || limit > 50)) {
            throw new IllegalArgumentException("limit 取值范围为 1~50");
        }
        StringBuilder query = Paging.cursor(cursor);
        Paging.add(query, "limit", limit == null ? null : String.valueOf(limit));
        JsonNode node = http.executeRaw("GET", base(groupOpenid) + "/join_request_list" + query, null);
        return Paging.page(node, "list", items -> JsonUtil.mapper().convertValue(items, new TypeReference<List<JoinRequest>>() {
        }));
    }

    /** 官方：POST /v2/groups/{group_openid}/approval_join_request/{member_openid}（入群申请审批）；60 QPM，无响应体。 */
    public void approveJoinRequest(String groupOpenid, String memberOpenid, JoinApprovalOpRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        http.post(approvalPath(groupOpenid, memberOpenid), request, Void.class);
    }

    /** 官方：POST /v2/groups/{group_openid}/approval_join_request/{member_openid}（入群申请审批）异步版。 */
    public CompletableFuture<Void> approveJoinRequestAsync(String groupOpenid, String memberOpenid, JoinApprovalOpRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(approvalPath(groupOpenid, memberOpenid), request, Void.class);
    }

    private static String approvalPath(String groupOpenid, String memberOpenid) {
        Objects.requireNonNull(memberOpenid, "memberOpenid 不能为空");
        if (memberOpenid.isBlank()) {
            throw new IllegalArgumentException("memberOpenid 不能为空");
        }
        return base(groupOpenid) + "/approval_join_request/" + memberOpenid;
    }

    /** 官方：GET /v2/groups/join_approval_strategy（查询入群自动审批策略列表）；60 QPM，limit 默认 20、最大 50。 */
    public Page<JoinApprovalStrategy> getJoinApprovalStrategies(String cursor, Integer limit) {
        if (limit != null && (limit < 1 || limit > 50)) {
            throw new IllegalArgumentException("limit 取值范围为 1~50");
        }
        StringBuilder query = Paging.cursor(cursor);
        Paging.add(query, "limit", limit == null ? null : String.valueOf(limit));
        JsonNode node = http.executeRaw("GET", STRATEGY_BASE + query, null);
        return Paging.page(node, "strategies", items -> JsonUtil.mapper().convertValue(items, new TypeReference<List<JoinApprovalStrategy>>() {
        }));
    }

    /** 官方：POST /v2/groups/join_approval_strategy（创建入群自动审批策略）；60 QPM，最多 20 个策略。 */
    public StrategyOperateResult createJoinApprovalStrategy(JoinApprovalStrategyRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(STRATEGY_BASE, request, StrategyOperateResult.class);
    }

    /** 官方：PATCH /v2/groups/join_approval_strategy/{strategy_id}（修改入群自动审批策略）；60 QPM。 */
    public StrategyOperateResult updateJoinApprovalStrategy(String strategyId, JoinApprovalStrategyUpdateRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.patch(strategyPath(strategyId), request, StrategyOperateResult.class);
    }

    /** 官方：DELETE /v2/groups/join_approval_strategy/{strategy_id}（删除入群自动审批策略）；60 QPM，无响应体。 */
    public void deleteJoinApprovalStrategy(String strategyId) {
        http.delete(strategyPath(strategyId));
    }

    /** 官方：DELETE /v2/groups/join_approval_strategy/{strategy_id}（删除入群自动审批策略）异步版。 */
    public CompletableFuture<Void> deleteJoinApprovalStrategyAsync(String strategyId) {
        return http.deleteAsync(strategyPath(strategyId));
    }

    /** 官方：POST /v2/groups/join_approval_strategy/{strategy_id}/execute（执行入群自动审批策略）；60 QPM，异步执行约 10 分钟，无响应体。 */
    public void executeJoinApprovalStrategy(String strategyId) {
        http.post(strategyPath(strategyId) + "/execute", Collections.emptyMap(), Void.class);
    }

    /** 官方：POST /v2/groups/join_approval_strategy/{strategy_id}/whitelist_users（修改入群自动审批策略的白名单号码）；60 QPM，单次最多 10000 个。 */
    public StrategyWhitelistResult operateJoinApprovalWhitelist(String strategyId, StrategyWhitelistUsersRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(strategyPath(strategyId) + "/whitelist_users", request, StrategyWhitelistResult.class);
    }

}
