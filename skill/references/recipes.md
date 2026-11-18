# qqbot-sdk 任务模板 R1~R11

所有模板假设已有基础骨架（QQBotConfig + QQBotClient + start()，见 SKILL.md）。凭证一律 `System.getenv("QQBOT_APP_ID"/"QQBOT_APP_SECRET")`。监听回调运行在 SDK 事件线程池，耗时业务请自行异步（见 R8）。

## R1 群聊：被@回复 / 主动推消息 / 挂按钮

```java
// 被动回复（5 分钟窗口、最多 5 次；msg_seq SDK 自动递增）
client.on(GroupAtMessageCreateEvent.class, e -> {
    String reply = handle(e.getContent());
    client.group().replyText(e.getGroupOpenid(), e.getId(), reply);
});

// 主动推送（沙箱内需先在测试群；正式上线需平台消息模板，msg_id 传模板场景值由业务决定）
client.group().sendText(groupOpenid, "公告：今晚维护");

// 文本+按钮：用 sendMessage 完整请求体
client.group().sendMessage(groupOpenid,
        MessageRequest.text("点一下签到")
                .setKeyboard(Keyboard.ofRows(List.of(
                        Keyboard.Row.of(new Keyboard.Button()
                                .setId("sign_in")
                                .setRenderData(new Keyboard.RenderData().setLabel("签到").setStyle(1))
                                .setAction(new Keyboard.Action()
                                        .setType(1)                       // 1=回调按钮（点击触发 INTERACTION_CREATE，data 回传）；0=跳转 2=指令
                                        .setData("sign_in")
                                        .setPermission(Keyboard.Permission.all())))))));
```

注意：groupOpenid 来自事件（`e.getGroupOpenid()`），不是 QQ 群号，无法凭群号推算。

## R2 单聊：回复 / 输入中 / 唤醒

```java
client.on(C2CMessageCreateEvent.class, e -> {
    String user = e.getAuthor().getUserOpenid();
    client.c2c().sendInputNotify(user, 3);                     // 显示"正在输入"（体验用，可省）
    client.c2c().replyText(user, e.getId(), "你说的是: " + e.getContent());  // 60 分钟/4 次窗口
});

// 主动触达（需消息模板审核通过）：普通主动 or 唤醒
client.c2c().sendText(userOpenid, "任务完成");
client.c2c().sendWakeupText(userOpenid, "回来聊聊？");          // 与 msg_id 互斥的唤醒方式
```

好友添加成功欢迎语：

```java
client.on(FriendAddEvent.class, e ->
        client.c2c().sendText(e.getOpenid(), "你好，有什么可以帮你？"));  // 仍在被动窗口内
```

## R3 富媒体：发图片/文件（file_type: 1图 2视频 3语音 4文件）

```java
// 最省事：图片 URL 一步发送（平台自己拉取）
client.group().sendImageByUrl(groupOpenid, "https://example.com/a.png");

// 可控版：上传 → 拿 fileInfo → 发 media 消息
MediaUploadResponse up = client.group().uploadMedia(groupOpenid,
        MediaUploadRequest.ofUrl(1, "https://example.com/a.png"));
client.group().sendMessage(groupOpenid, MessageRequest.media(up.getFileInfo()));
// up.getTtl() 秒内可复用同一 fileInfo，勿重复上传

// 本地文件：≤5MB 直传 Base64，>5MB 自动分片（prepare/PUT/finish，md5/sha1/md5_10m 校验 SDK 内部完成）
MediaUploadResponse up2 = client.group().uploadFile(groupOpenid, 4, Path.of("/tmp/report.pdf"));
client.group().sendMessage(groupOpenid, MessageRequest.media(up2.getFileInfo()));
```

单聊同理用 `client.c2c()`。收到的图片消息在 `e.getAttachments()`（url/filename/size 等）。

## R4 markdown / 图文卡片

```java
// markdown（需平台模板或自定义语法）
client.group().sendMessage(groupOpenid,
        MessageRequest.markdown(MessageMarkdown.ofContent("## 标题\n内容")));

// 图文链接卡片（msg_type=7  tuwen）
client.group().sendMessage(groupOpenid,
        MessageRequest.card(MessageCard.tuwen("标题", "描述",
                "https://jump.url", "https://pic.url")));
```

## R5 按钮点击回执（必须回执，否则用户端一直转圈；50 QPS、一次性）

```java
client.on(InteractionCreateEvent.class, e -> {
    String buttonId = e.getData().getResolved().getButtonId();
    boolean ok = doBusiness(buttonId, e.getUserOpenid());
    client.interaction().respond(e.getId(),
            ok ? InteractionApi.CODE_SUCCESS : InteractionApi.CODE_FAILED);
    // 后续回复消息用消息侧接口（replyText 或主动发送）
});
```

intents 需含 `Intents.INTERACTION`。响应码：0成功 1失败 2频繁 3重复 4无权限 5仅管理员。

## R6 频道：子频道发消息 / 私信

```java
// 子频道文本（被动回复：ChannelMessageRequest.text("...").replyTo 或 setMsgId(事件消息id)，5 分钟窗口，无 msg_seq）
client.channel().sendMessage(channelId, ChannelMessageRequest.text("大家好"));

// embed / ark
client.channel().sendMessage(channelId, ChannelMessageRequest.text(" ")
        .setEmbed(new MessageEmbed().setTitle("t").setPrompt("p")));

// 私信三步：createDms → sendDms（dmsGuildId），recallDms 撤回
Dms dms = client.channel().createDms(userOpenid, guildId);
client.channel().sendDms(dms.getGuildId(), ChannelMessageRequest.text("hi"));
```

事件：AT_MESSAGE_CREATE / DIRECT_MESSAGE_CREATE → `ChannelMessageEvent.getMessage()`（getId/getContent/getAuthor）。

## R7 撤回（2 分钟内）

```java
SendMessageResponse sent = client.group().sendText(groupOpenid, "稍后撤回");
client.group().recall(groupOpenid, sent.getId());
client.c2c().recall(userOpenid, messageId);
client.channel().recall(channelId, messageId, false);   // 频道多一个 hideTip 参数
```

## R8 异步（不阻塞事件线程 / 并发批量）

```java
client.on(GroupAtMessageCreateEvent.class, e ->
    client.group().sendMessageAsync(e.getGroupOpenid(), MessageRequest.text("hi"))
        .whenComplete((resp, err) -> {
            if (err != null) log.error("发送失败", err);   // CompletionException 包裹 ApiException
        }));
```

异步变体只有：sendMessage/sendMessageAsync、recall/recallAsync、respondAsync。replyText 与上传接口是同步的（内部走 OkHttp 同步调用）；在监听器里做慢业务请自己丢线程池。

## R9 Spring Boot 3 接入

```yaml
# application.yml —— app-id/app-secret 用占位符引用环境变量
qqbot:
  app-id: ${QQBOT_APP_ID}
  app-secret: ${QQBOT_APP_SECRET}
  intents: 33554432        # GROUP_AND_C2C_EVENT；加按钮回执用 100663296
  auto-start: true
```

```java
@Component
public class MyListener implements EventListener<GroupAtMessageCreateEvent> {
    private final QQBotClient client;
    public MyListener(QQBotClient client) { this.client = client; }
    @Override public void onEvent(GroupAtMessageCreateEvent e) {
        client.group().replyText(e.getGroupOpenid(), e.getId(), "收到");
    }
}
```

QQBotClient 是单例 Bean（容器关闭自动 shutdown）；`EventListener<?>` Bean 会被自动注册；`auto-start: false` 时可自己调 `client.start()`。

## R10 错误处理与审核分流

```java
try {
    client.group().sendText(groupOpenid, content);
} catch (ApiException ex) {
    if (ex.isPendingAudit()) {        // 201/202：消息进审核，属正常业务态
        log.info("已提交审核 err={} trace={}", ex.getErrCode(), ex.getTraceId());
    } else if (ex.isRateLimited()) {  // 平台频控
        // 退避后重试，或调低 requestsPerSecond
    } else {
        log.error("调用失败 http={} err={} trace={}",
                ex.getHttpStatus(), ex.getErrCode(), ex.getTraceId());
        // 报障给平台时附 trace_id
    }
}
```

网关被踢常见关闭码：4004 鉴权失败；4914 机器人下架；4915 封禁（后两者 SDK 停止重连）。401 由 SDK 自动刷 token，无需处理。

## R11 未建模事件 / 新接口兜底

```java
client.onRaw("PUBLIC_GUILD_MESSAGE_CREATE", json -> log.info("原始事件: {}", json));
// 事件对象里也带原文：e.getRaw()（Jackson JsonNode）
// 平台新增接口 SDK 未跟随时：注入/构造 core 的 HttpApiClient 调 executeRaw(method, path, body)
```

遇到 Skill 里没有的签名，先到源码仓库 `qqbot-sdk-api|core/src/main/java` grep 确认，或查 Wiki <https://gitee.com/meilinkj/qqbot-sdk/wikis>，仍不确定就明确告诉用户"该 API 未经验证"，不要编造。