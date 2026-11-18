package cn.meilinkj.qqbot.api.support;

import cn.meilinkj.qqbot.api.response.MediaUploadResponse;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.auth.AccessTokenManager;
import cn.meilinkj.qqbot.core.exception.QQBotException;
import cn.meilinkj.qqbot.core.http.HttpApiClient;
import cn.meilinkj.qqbot.core.util.JsonUtil;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 分片上传全流程测试（upload_prepare → PUT 分片 → upload_part_finish → files 合并）。
 * 流程见 https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html
 */
class MediaUploaderTest {

    private MockWebServer server;
    private HttpApiClient http;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = new MockWebServer();
        server.start();
        baseUrl = server.url("/").toString().replaceAll("/$", "");
        QQBotConfig config = QQBotConfig.builder("app-id", "app-secret")
                .baseUrl(baseUrl)
                .requestsPerSecond(1000)
                .build();
        OkHttpClient client = new OkHttpClient();
        http = new HttpApiClient(config, new AccessTokenManager(config, client), client);
        server.enqueue(new MockResponse().setBody("{\"access_token\":\"T1\",\"expires_in\":\"7200\"}"));
    }

    @AfterEach
    void tearDown() throws IOException {
        http.shutdown();
        server.shutdown();
    }

    @Test
    void chunkedUploadFullFlow() throws Exception {
        byte[] data = new byte[2500];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 251);
        }
        String md5 = HexFormat.of().formatHex(MessageDigest.getInstance("MD5").digest(data));
        String sha1 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(data));

        server.enqueue(new MockResponse().setBody("{"
                + "\"upload_id\":\"UP1\",\"block_size\":\"1000\","
                + "\"parts\":["
                + "{\"index\":0,\"presigned_url\":\"" + baseUrl + "/chunk0\",\"block_size\":\"1000\"},"
                + "{\"index\":1,\"presigned_url\":\"" + baseUrl + "/chunk1\",\"block_size\":\"1000\"},"
                + "{\"index\":2,\"presigned_url\":\"" + baseUrl + "/chunk2\",\"block_size\":\"500\"}],"
                + "\"upload_config\":{\"concurrency\":1,\"retry_timeout\":300,\"retry_delay\":1}}"));
        server.enqueue(new MockResponse().setResponseCode(200)); // PUT chunk0
        server.enqueue(new MockResponse().setBody("{}"));         // part_finish 0
        server.enqueue(new MockResponse().setResponseCode(200)); // PUT chunk1
        server.enqueue(new MockResponse().setBody("{}"));         // part_finish 1
        server.enqueue(new MockResponse().setResponseCode(200)); // PUT chunk2
        server.enqueue(new MockResponse().setBody("{}"));         // part_finish 2
        server.enqueue(new MockResponse().setBody(
                "{\"file_uuid\":\"UUID1\",\"file_info\":\"INFO1\",\"ttl\":0,\"raw_url\":\"https://cdn/x\"}"));

        MediaUploadResponse response = MediaUploader.uploadChunked(http, "/v2/groups/G1", 2, "v.mp4", data);
        assertEquals("INFO1", response.getFileInfo());
        assertEquals("UUID1", response.getFileUuid());

        server.takeRequest(); // token

        RecordedRequest prepare = server.takeRequest();
        assertEquals("/v2/groups/G1/upload_prepare", prepare.getPath());
        var prepareBody = JsonUtil.tree(prepare.getBody().readUtf8());
        assertEquals("2500", prepareBody.get("file_size").asText());
        assertEquals(md5, prepareBody.get("md5").asText());
        assertEquals(sha1, prepareBody.get("sha1").asText());
        assertEquals(md5, prepareBody.get("md5_10m").asText()); // 文件不足 10002432 字节时等于完整 MD5

        RecordedRequest put0 = server.takeRequest();
        assertEquals("PUT", put0.getMethod());
        assertEquals("/chunk0", put0.getPath());
        assertEquals(1000, put0.getBodySize());

        RecordedRequest finish0 = server.takeRequest();
        assertEquals("/v2/groups/G1/upload_part_finish", finish0.getPath());
        var finishBody = JsonUtil.tree(finish0.getBody().readUtf8());
        assertEquals("UP1", finishBody.get("upload_id").asText());
        assertEquals(0, finishBody.get("part_index").asInt());
        assertEquals("1000", finishBody.get("block_size").asText());

        server.takeRequest(); // PUT chunk1
        server.takeRequest(); // finish 1
        RecordedRequest put2 = server.takeRequest();
        assertEquals(500, put2.getBodySize());
        server.takeRequest(); // finish 2

        RecordedRequest merge = server.takeRequest();
        assertEquals("/v2/groups/G1/files", merge.getPath());
        var mergeBody = JsonUtil.tree(merge.getBody().readUtf8());
        assertEquals("UP1", mergeBody.get("upload_id").asText());
        assertEquals("v.mp4", mergeBody.get("file_name").asText());
        assertEquals(2, mergeBody.get("file_type").asInt());
    }

    @Test
    void binaryUploadRejectsOver5MB() {
        byte[] tooBig = new byte[MediaUploader.BINARY_LIMIT + 1];
        assertThrows(QQBotException.class,
                () -> MediaUploader.uploadBinary(http, "/v2/groups/G1", 1, "a.png", tooBig));
    }

    @Test
    void binaryUploadSendsBase64FileData() throws Exception {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        server.enqueue(new MockResponse().setBody("{\"file_uuid\":\"U\",\"file_info\":\"I\",\"ttl\":3600}"));

        MediaUploadResponse response = MediaUploader.uploadBinary(http, "/v2/users/U1", 1, "a.png", data);
        assertEquals("I", response.getFileInfo());

        server.takeRequest(); // token
        RecordedRequest request = server.takeRequest();
        assertEquals("/v2/users/U1/files", request.getPath());
        var body = JsonUtil.tree(request.getBody().readUtf8());
        assertEquals(java.util.Base64.getEncoder().encodeToString(data), body.get("file_data").asText());
        assertEquals("a.png", body.get("file_name").asText());
        assertTrue(!body.get("srv_send_msg").asBoolean());
    }
}