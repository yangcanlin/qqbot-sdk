package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.InputNotify;
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
 * QQ 单聊（C2C）V2 接口。
 *
 * <ul>
 *   <li>发送消息：POST /v2/users/{user_openid}/messages
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html">文档</a></li>
 *   <li>撤回消息：DELETE /v2/users/{user_openid}/messages/{message_id}（2 分钟内）
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages_message_id.delete.html">文档</a></li>
 *   <li>富媒体上传：POST /v2/users/{user_openid}/files
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_files.post.html">文档</a></li>
 * </ul>
 *
 * <p>被动回复：单聊 msg_id 60 分钟内有效，最多回复 4 次。
 * 单聊独有能力：输入中状态（msg_type=6）、互动召回（is_wakeup，与 msg_id/event_id 互斥）。</p>
 */
public class C2CApi {

    private final HttpApiClient http;
    private final MsgSeqAllocator seqAllocator;

    public C2CApi(HttpApiClient http, MsgSeqAllocator seqAllocator) {
        this.http = http;
        this.seqAllocator = seqAllocator;
    }

    private static String base(String userOpenid) {
        Objects.requireNonNull(userOpenid, "userOpenid 不能为空");
        if (userOpenid.isBlank()) {
            throw new IllegalArgumentException("userOpenid 不能为空");
        }
        return "/v2/users/" + userOpenid;
    }

    /** 发送消息（完整请求体）。 */
    public SendMessageResponse sendMessage(String userOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(base(userOpenid) + "/messages", request, SendMessageResponse.class);
    }

    public CompletableFuture<SendMessageResponse> sendMessageAsync(String userOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(base(userOpenid) + "/messages", request, SendMessageResponse.class);
    }

    /** 主动发送文本消息。 */
    public SendMessageResponse sendText(String userOpenid, String content) {
        return sendMessage(userOpenid, MessageRequest.text(content));
    }

    /**
     * 被动回复文本消息，自动分配递增 msg_seq（同一 msg_id 最多 4 次）。
     * msgId 来自 C2C_MESSAGE_CREATE 事件的 d.id。
     */
    public SendMessageResponse replyText(String userOpenid, String msgId, String content) {
        Objects.requireNonNull(msgId, "msgId 不能为空");
        return sendMessage(userOpenid, MessageRequest.text(content).replyTo(msgId, seqAllocator.next(msgId)));
    }

    /** 发送"输入中"状态（msg_type=6），展示 seconds 秒（1~60）。 */
    public void sendInputNotify(String userOpenid, int seconds) {
        sendMessage(userOpenid, new MessageRequest().setMsgType(6).setInputNotify(InputNotify.typing(seconds)));
    }

    /** 互动召回消息（is_wakeup=true），用于召回长时间未互动的用户，与被动回复参数互斥。 */
    public SendMessageResponse sendWakeupText(String userOpenid, String content) {
        return sendMessage(userOpenid, MessageRequest.text(content).setIsWakeup(true));
    }

    /** 上传 URL 资源后发送图片消息（file_type=1）。 */
    public SendMessageResponse sendImageByUrl(String userOpenid, String imageUrl) {
        MediaUploadResponse uploaded = uploadMedia(userOpenid, MediaUploadRequest.ofUrl(1, imageUrl));
        return sendMessage(userOpenid, MessageRequest.media(uploaded.getFileInfo()));
    }

    /** 撤回消息，仅支持 2 分钟内发送的消息。 */
    public void recall(String userOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        http.delete(base(userOpenid) + "/messages/" + messageId);
    }

    public CompletableFuture<Void> recallAsync(String userOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        return http.deleteAsync(base(userOpenid) + "/messages/" + messageId);
    }

    /** 富媒体上传（URL 方式或分片合并）。单聊上传的文件仅能发送到单聊。 */
    public MediaUploadResponse uploadMedia(String userOpenid, MediaUploadRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return MediaUploader.upload(http, base(userOpenid), request);
    }

    /** 二进制直传（≤5MB）。 */
    public MediaUploadResponse uploadBinary(String userOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadBinary(http, base(userOpenid), fileType, fileName, data);
    }

    /** 分片上传（大文件推荐）。 */
    public MediaUploadResponse uploadChunked(String userOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadChunked(http, base(userOpenid), fileType, fileName, data);
    }

    /** 上传本地文件：≤5MB 自动二进制直传，否则分片上传。 */
    public MediaUploadResponse uploadFile(String userOpenid, int fileType, Path file) throws IOException {
        return MediaUploader.uploadAuto(http, base(userOpenid), fileType,
                file.getFileName().toString(), Files.readAllBytes(file));
    }
}