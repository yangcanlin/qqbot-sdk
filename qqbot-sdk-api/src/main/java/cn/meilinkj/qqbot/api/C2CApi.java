package cn.meilinkj.qqbot.api;

import cn.meilinkj.qqbot.api.request.InputNotify;
import cn.meilinkj.qqbot.api.request.MediaUploadRequest;
import cn.meilinkj.qqbot.api.request.MessageRequest;
import cn.meilinkj.qqbot.api.request.StreamMessageRequest;
import cn.meilinkj.qqbot.api.response.MediaUploadResponse;
import cn.meilinkj.qqbot.api.response.SendMessageResponse;
import cn.meilinkj.qqbot.api.response.StreamMessageResponse;
import cn.meilinkj.qqbot.api.response.UserInfo;
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
 *   <li>流式消息：POST /v2/users/{user_openid}/stream_messages
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_stream_messages.post.html">文档</a></li>
 *   <li>用户信息：GET /v2/users/{user_openid}/info
 *       <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_info.get.html">文档</a></li>
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

    /**
     * 官方：POST /v2/users/{user_openid}/messages（发送单聊消息）；100 QPS。完整请求体。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html">文档</a>
     */
    public SendMessageResponse sendMessage(String userOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(base(userOpenid) + "/messages", request, SendMessageResponse.class);
    }

    public CompletableFuture<SendMessageResponse> sendMessageAsync(String userOpenid, MessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(base(userOpenid) + "/messages", request, SendMessageResponse.class);
    }

    /** 官方：POST /v2/users/{user_openid}/messages（发送单聊消息）主动文本消息。 */
    public SendMessageResponse sendText(String userOpenid, String content) {
        return sendMessage(userOpenid, MessageRequest.text(content));
    }

    /**
     * 官方：POST /v2/users/{user_openid}/messages（发送单聊消息）被动回复，自动分配递增 msg_seq（同一 msg_id 最多 4 次）。
     * msgId 来自 C2C_MESSAGE_CREATE 事件的 d.id。
     */
    public SendMessageResponse replyText(String userOpenid, String msgId, String content) {
        Objects.requireNonNull(msgId, "msgId 不能为空");
        return sendMessage(userOpenid, MessageRequest.text(content).replyTo(msgId, seqAllocator.next(msgId)));
    }

    /** 官方：POST /v2/users/{user_openid}/messages（发送单聊消息 input_notify 能力，msg_type=6），展示 seconds 秒（1~60）。 */
    public void sendInputNotify(String userOpenid, int seconds) {
        sendMessage(userOpenid, new MessageRequest().setMsgType(6).setInputNotify(InputNotify.typing(seconds)));
    }

    /** 官方：POST /v2/users/{user_openid}/messages（发送单聊消息 is_wakeup=true 互动召回），与被动回复参数互斥。 */
    public SendMessageResponse sendWakeupText(String userOpenid, String content) {
        return sendMessage(userOpenid, MessageRequest.text(content).setIsWakeup(true));
    }

    /**
     * 官方：POST /v2/users/{user_openid}/stream_messages（流式发送单聊消息）；50 QPS。
     * 首片不传 stream_msg_id，响应里的 id 即 stream_msg_id，后续分片携带它并把 index 递增；结束时把 input_state 置 10。
     * 常见错误码：40007（replace 模式下改动了已下发前缀）、50002（频率限制）。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_stream_messages.post.html">文档</a>
     */
    public StreamMessageResponse sendStream(String userOpenid, StreamMessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.post(base(userOpenid) + "/stream_messages", request, StreamMessageResponse.class);
    }

    public CompletableFuture<StreamMessageResponse> sendStreamAsync(String userOpenid, StreamMessageRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return http.postAsync(base(userOpenid) + "/stream_messages", request, StreamMessageResponse.class);
    }

    /**
     * 官方：GET /v2/users/{user_openid}/info（获取用户基本信息）；10 QPS。
     * 用户与机器人无好友关系时返回 err_code=40184001。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_info.get.html">文档</a>
     */
    public UserInfo getUserInfo(String userOpenid) {
        return http.get(base(userOpenid) + "/info", UserInfo.class);
    }

    public CompletableFuture<UserInfo> getUserInfoAsync(String userOpenid) {
        return http.getAsync(base(userOpenid) + "/info", UserInfo.class);
    }

    /** 官方：POST files + POST messages（上传单聊富媒体并发送图片消息，file_type=1）一步完成。 */
    public SendMessageResponse sendImageByUrl(String userOpenid, String imageUrl) {
        MediaUploadResponse uploaded = uploadMedia(userOpenid, MediaUploadRequest.ofUrl(1, imageUrl));
        return sendMessage(userOpenid, MessageRequest.media(uploaded.getFileInfo()));
    }

    /**
     * 官方：DELETE /v2/users/{user_openid}/messages/{message_id}（撤回单聊消息）；仅支持 2 分钟内。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages_message_id.delete.html">文档</a>
     */
    public void recall(String userOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        http.delete(base(userOpenid) + "/messages/" + messageId);
    }

    public CompletableFuture<Void> recallAsync(String userOpenid, String messageId) {
        Objects.requireNonNull(messageId, "messageId 不能为空");
        return http.deleteAsync(base(userOpenid) + "/messages/" + messageId);
    }

    /**
     * 官方：POST /v2/users/{user_openid}/files（单聊上传富媒体）；50 QPS，文件仅能发送到单聊。
     * <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_files.post.html">文档</a>
     */
    public MediaUploadResponse uploadMedia(String userOpenid, MediaUploadRequest request) {
        Objects.requireNonNull(request, "request 不能为空");
        return MediaUploader.upload(http, base(userOpenid), request);
    }

    /** 官方：POST /v2/users/{user_openid}/files（单聊上传富媒体）二进制直传方式（≤5MB）。 */
    public MediaUploadResponse uploadBinary(String userOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadBinary(http, base(userOpenid), fileType, fileName, data);
    }

    /** 官方：POST /v2/users/{user_openid}/files（单聊上传富媒体）分片上传方式（大文件推荐）。 */
    public MediaUploadResponse uploadChunked(String userOpenid, int fileType, String fileName, byte[] data) {
        return MediaUploader.uploadChunked(http, base(userOpenid), fileType, fileName, data);
    }

    /** 官方：POST /v2/users/{user_openid}/files（单聊上传富媒体）本地文件封装：≤5MB 直传，否则分片。 */
    public MediaUploadResponse uploadFile(String userOpenid, int fileType, Path file) throws IOException {
        return MediaUploader.uploadAuto(http, base(userOpenid), fileType,
                file.getFileName().toString(), Files.readAllBytes(file));
    }
}