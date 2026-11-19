package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.MediaUploadRequest;
import cn.meilinkj.qqbot.api.request.MessageRequest;
import cn.meilinkj.qqbot.api.response.MediaUploadResponse;
import cn.meilinkj.qqbot.api.response.SendMessageResponse;
import cn.meilinkj.qqbot.api.support.MediaUploader;
import cn.meilinkj.qqbot.api.support.MsgSeqAllocator;
import cn.meilinkj.qqbot.core.http.HttpApiClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * QQ 群聊 V2 接口。
 *
 * <ul>
 *   <li>发送消息：POST /v2/groups/{group_openid}/messages（100 QPS）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">文档</a></li>
 *   <li>撤回消息：DELETE /v2/groups/{group_openid}/messages/{message_id}（2 分钟内，10 QPS）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages_message_id.delete.html">文档</a></li>
 *   <li>富媒体上传：POST /v2/groups/{group_openid}/files（50 QPS）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">文档</a></li>
 * </ul>
 *
 * <p>被动回复：群聊 msg_id 5 分钟内有效，最多回复 5 次；主动消息消耗主动额度。</p>
 */
public class GroupApi {

    private final HttpApiClient http;
    private final MsgSeqAllocator seqAllocator;

    public GroupApi(HttpApiClient http, MsgSeqAllocator seqAllocator) {
        this.http = http;
        this.seqAllocator = seqAllocator;
    }

    private static String base(String groupOpenid) {
        Objects.requireNonNull(groupOpenid, "groupOpenid 不能为空");
        if (groupOpenid.isBlank()) {
            throw new IllegalArgumentException("groupOpenid 不能为空");
        }
        return "/v2/groups/" + groupOpenid;
    }

    /**
     * 官方：POST /v2/groups/{group_openid}/messages（发送群聊消息）；100 QPS。
     * 完整请求体，支持文本/Markdown/富媒体/卡片/键盘。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html">文档</a>
     */
    public SendMessageResponse sendMessage(String groupOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(base(groupOpenid) + "/messages", request, SendMessageResponse.class);
    }

    public CompletableFuture<SendMessageResponse> sendMessageAsync(String groupOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(base(groupOpenid) + "/messages", request, SendMessageResponse.class);
    }

    /** 官方：POST /v2/groups/{group_openid}/messages（发送群聊消息）主动文本消息。 */
    public SendMessageResponse sendText(String groupOpenid, String content) {
        return sendMessage(groupOpenid, MessageRequest.text(content));
    }

    /**
     * 官方：POST /v2/groups/{group_openid}/messages（发送群聊消息）被动回复，自动分配递增 msg_seq（同一 msg_id 最多 5 次）。
     * msgId 来自 GROUP_AT_MESSAGE_CREATE / GROUP_MESSAGE_CREATE 事件的 d.id。
     */
    public SendMessageResponse replyText(String groupOpenid, String msgId, String content) {
        Objects.requireNonNull(msgId, "msgId 不能为空");
        return sendMessage(groupOpenid, MessageRequest.text(content).replyTo(msgId, seqAllocator.next(msgId)));
    }

    /** 官方：POST files + POST messages（上传群聊富媒体并发送图片消息，file_type=1）一步完成。 */
    public SendMessageResponse sendImageByUrl(String groupOpenid, String imageUrl) {
        MediaUploadResponse uploaded = uploadMedia(groupOpenid, MediaUploadRequest.ofUrl(1, imageUrl));
        return sendMessage(groupOpenid, MessageRequest.media(uploaded.getFileInfo()));
    }

    /**
     * 官方：DELETE /v2/groups/{group_openid}/messages/{message_id}（撤回群聊消息）；10 QPS，仅支持 2 分钟内；管理员机器人可撤回群成员消息。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages_message_id.delete.html">文档</a>
     */
    public void recall(String groupOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        http.delete(base(groupOpenid) + "/messages/" + messageId);
    }

    public CompletableFuture<Void> recallAsync(String groupOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        return http.deleteAsync(base(groupOpenid) + "/messages/" + messageId);
    }

    /**
     * 官方：POST /v2/groups/{group_openid}/files（群聊上传富媒体）；50 QPS。URL 方式或携带 upload_id 分片合并。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">文档</a>
     */
    public MediaUploadResponse uploadMedia(String groupOpenid, MediaUploadRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return MediaUploader.upload(http, base(groupOpenid), request);
    }

    /** 官方：POST /v2/groups/{group_openid}/files（群聊上传富媒体）二进制直传方式（≤5MB）。 */
    public MediaUploadResponse uploadBinary(String groupOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadBinary(http, base(groupOpenid), fileType, fileName, data);
    }

    /** 官方：POST /v2/groups/{group_openid}/files（群聊上传富媒体）分片上传方式（大文件推荐）。 */
    public MediaUploadResponse uploadChunked(String groupOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadChunked(http, base(groupOpenid), fileType, fileName, data);
    }

    /** 官方：POST /v2/groups/{group_openid}/files（群聊上传富媒体）本地文件封装：≤5MB 直传，否则分片。 */
    public MediaUploadResponse uploadFile(String groupOpenid, int fileType, Path file) throws IOException {
        return MediaUploader.uploadAuto(http, base(groupOpenid), fileType,
                file.getFileName().toString(), Files.readAllBytes(file));
    }
}