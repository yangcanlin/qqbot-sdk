package cn.meilinkj.qqbot.api.support;

import cn.meilinkj.qqbot.api.request.MediaUploadRequest;
import cn.meilinkj.qqbot.api.request.UploadPartFinishRequest;
import cn.meilinkj.qqbot.api.request.UploadPrepareRequest;
import cn.meilinkj.qqbot.api.response.MediaUploadResponse;
import cn.meilinkj.qqbot.api.response.UploadPrepareResponse;
import cn.meilinkj.qqbot.core.exception.QQBotException;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.HexFormat;

/**
 * 富媒体上传实现：URL 上传、二进制直传（≤5MB）、分片上传合并（推荐）。
 * 群聊与单聊共用流程，仅 basePath 不同（/v2/groups/{openid} 或 /v2/users/{openid}）。
 *
 * 见 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html">群聊富媒体上传</a>
 * 与 <a href="https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html">分片上传预处理</a>
 */
public final class MediaUploader {

    private static final Logger log = LoggerFactory.getLogger(MediaUploader.class);
    /** 二进制直传上限 5MB */
    public static final int BINARY_LIMIT = 5 * 1024 * 1024;
    /** md5_10m 取文件前 10002432 字节 */
    private static final int MD5_10M_BYTES = 10002432;

    private MediaUploader() {
    }

    /** URL 上传或分片合并（携带 upload_id）。 */
    public static MediaUploadResponse upload(HttpApiClient http, String basePath, MediaUploadRequest request) {
        return http.post(basePath + "/files", request, MediaUploadResponse.class);
    }

    /** 二进制直传，原始数据 ≤5MB。 */
    public static MediaUploadResponse uploadBinary(HttpApiClient http, String basePath,
                                                   int fileType, String fileName, byte[] data) {
        if (data.length > BINARY_LIMIT) {
            throw new QQBotException("二进制直传不能超过 5MB，请使用分片上传: " + data.length);
        }
        MediaUploadRequest request = new MediaUploadRequest()
                .setFileType(fileType)
                .setFileName(fileName)
                .setFileData(Base64.getEncoder().encodeToString(data))
                .setSrvSendMsg(false);
        return upload(http, basePath, request);
    }

    /**
     * 分片上传完整流程：upload_prepare → 逐片 PUT 预签名 URL → upload_part_finish → files 合并。
     */
    public static MediaUploadResponse uploadChunked(HttpApiClient http, String basePath,
                                                    int fileType, String fileName, byte[] data) {
        UploadPrepareRequest prepareRequest = new UploadPrepareRequest(
                fileType, data.length, fileName,
                md5Hex(data), sha1Hex(data), md5Hex(Arrays.copyOf(data, Math.min(data.length, MD5_10M_BYTES))));
        UploadPrepareResponse prepare = http.post(basePath + "/upload_prepare", prepareRequest, UploadPrepareResponse.class);
        if (prepare == null || prepare.getParts() == null || prepare.getUploadId() == null) {
            throw new QQBotException("分片上传预处理响应缺少 upload_id/parts");
        }

        long blockSize = Long.parseLong(prepare.getBlockSize());
        for (UploadPrepareResponse.Part part : prepare.getParts()) {
            int index = part.getIndex();
            long partSize = part.getBlockSize() != null ? Long.parseLong(part.getBlockSize()) : blockSize;
            int from = (int) (index * blockSize);
            int to = (int) Math.min(from + partSize, data.length);
            if (from >= data.length || to <= from) {
                throw new QQBotException("分片范围非法 index=" + index + " fileSize=" + data.length);
            }
            byte[] slice = Arrays.copyOfRange(data, from, to);
            http.putBytes(part.getPresignedUrl(), slice);
            http.post(basePath + "/upload_part_finish",
                    new UploadPartFinishRequest(prepare.getUploadId(), index, slice.length, md5Hex(slice)), Void.class);
            log.debug("分片上传完成 index={} size={}", index, slice.length);
        }

        MediaUploadRequest merge = new MediaUploadRequest()
                .setFileType(fileType)
                .setFileName(fileName)
                .setUploadId(prepare.getUploadId())
                .setSrvSendMsg(false);
        return upload(http, basePath, merge);
    }

    /** 自动选择上传方式：≤5MB 二进制直传，否则分片上传。 */
    public static MediaUploadResponse uploadAuto(HttpApiClient http, String basePath,
                                                 int fileType, String fileName, byte[] data) {
        return data.length <= BINARY_LIMIT
                ? uploadBinary(http, basePath, fileType, fileName, data)
                : uploadChunked(http, basePath, fileType, fileName, data);
    }

    static String md5Hex(byte[] data) {
        return digestHex("MD5", data);
    }

    static String sha1Hex(byte[] data) {
        return digestHex("SHA-1", data);
    }

    private static String digestHex(String algorithm, byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(algorithm).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new QQBotException("摘要算法不可用: " + algorithm, e);
        }
    }
}