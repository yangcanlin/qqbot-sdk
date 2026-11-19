# qqbot-sdk 任务模板 R1~R16

所有模板假设已有基础骨架（QQBotConfig + QQBotClient + start()，见 SKILL.md）。凭证一律 `System.getenv("QQBOT_APP_ID"/"QQBOT_APP_SECRET")`。监听回调运行在 SDK 事件线程池，耗时业务请自行异步（见 R8）。

模板里会把两类代码写在一起，因为一个功能必然要「收事件 + 调接口」，但边界必须分清：

- `client.on(事件类, ...)` / `client.onRaw("事件名", ...)` = **事件**，平台通过 WebSocket 推给你，官方文档按「事件名 + intents」描述，没有 QPS/QPM 概念；
- `client.group()` / `c2c()` / `channel()` / `bot()` / `groupManage()` / `channelManage()` / `menu()` / `panel()` / `interaction()` / `gatewayInfo()` 上的方法 = **接口**，你主动发的 HTTP 请求，官方文档按「HTTP 方法 + 路径」描述，有频控与权限前提。

下面的注释用【事件】/【接口】标出每一行的归属；写新代码时请沿用这个标注习惯，避免把 Intent 名当事件名、或把事件当成可调用的方法。

## R1 群聊：被@回复 / 主动推消息 / 挂按钮

```java
// 【事件】收到被@的消息 → 回调里再调【接口】回复（5 分钟窗口、最多 5 次；msg_seq SDK 自动递增）
client.on(GroupAtMessageCreateEvent.class, e -> {
    String reply = handle(e.getContent());
    client.group().replyText(e.getGroupOpenid(), e.getId(), reply);
});

// 【接口】主动推送（沙箱内需先在测试群；正式上线需平台消息模板，msg_id 传模板场景值由业务决定）
client.group().sendText(groupOpenid, "公告：今晚维护");

// 【接口】文本+按钮：用 sendMessage 完整请求体
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
// 【事件】收到单聊消息 → 回调里调【接口】回复
client.on(C2CMessageCreateEvent.class, e -> {
    String user = e.getAuthor().getUserOpenid();
    client.c2c().sendInputNotify(user, 3);                     // 【接口】显示"正在输入"（体验用，可省）
    client.c2c().replyText(user, e.getId(), "你说的是: " + e.getContent());  // 【接口】60 分钟/4 次窗口
});

// 【接口】主动触达（需消息模板审核通过）：普通主动 or 唤醒
client.c2c().sendText(userOpenid, "任务完成");
client.c2c().sendWakeupText(userOpenid, "回来聊聊？");          // 与 msg_id 互斥的唤醒方式
```

好友添加成功欢迎语（【事件】`FRIEND_ADD` → 【接口】发单聊）：

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

异步变体命名一律 `方法名+Async` 返回 CompletableFuture：消息侧为 sendMessageAsync/recallAsync（群/单聊）、sendMessageAsync（频道）、respondAsync（互动）、getBotAsync/getGroupBotStateAsync（机器人）；群聊管理侧为 getGroupInfoAsync/getGroupMemberAsync/getMuteSettingAsync/setGroupMuteAsync/approveJoinRequestAsync/deleteJoinApprovalStrategyAsync。replyText 与上传接口是同步的（内部走 OkHttp 同步调用）；在监听器里做慢业务请自己丢线程池。

## R9 Spring Boot 3 接入

```yaml
# application.yml —— app-id/app-secret 用占位符引用环境变量
qqbot:
  app-id: ${QQBOT_APP_ID}
  app-secret: ${QQBOT_APP_SECRET}
  intents: 33554432        # GROUP_AND_C2C_EVENT；加按钮回执 100663296；加群成员事件 50331648
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

网关被踢的官方关闭码（SDK 常量见 `WsCloseCode`）：4001 无效 opcode、4002 无效 payload、4006 无效 session id、4007 seq 错误、4008 发送过快、4009 连接过期、4010 无效 shard、4011 需要分片、4012 无效 version、4013 无效 intent、4014 intent 无权限、4900~4913 内部错误、4914 机器人已下架、4915 机器人已封禁。SDK 行为：4008/4009 尝试 RESUME，4914/4915 属致命错误停止重连，4013/4014 需改 `intents` 后再连。HTTP 401 由 SDK 自动刷 token，无需处理。官方错误码表中没有 4004，任何"4004=鉴权失败"的说法都不可信（HTTP 认证失败是状态码 401，网关鉴权问题表现为 4013/4014 或连不上后报 READY 缺失）。

## R11 原始事件兜底 / 新接口原样调用

官方 api-v2 文档只在「事件订阅 Intents」清单里列出名字、**没有事件体字段规格**的事件，SDK 一律以 `GenericEvent` 分发，不要凭其他平台经验猜字段：

- `GUILD_MEMBER_ADD` / `GUILD_MEMBER_UPDATE` / `GUILD_MEMBER_REMOVE`（`Intents.GUILD_MEMBERS`）
- `MESSAGE_REACTION_ADD` / `MESSAGE_REACTION_REMOVE`（`Intents.GUILD_MESSAGE_REACTIONS`）
- `FORUM_THREAD_*`、`FORUM_POST_*`、`FORUM_REPLY_*`、`FORUM_PUBLISH_AUDIT_RESULT`（`Intents.FORUMS_EVENT`，仅私域机器人）
- `AUDIO_START` / `AUDIO_FINISH` / `AUDIO_ON_MIC` / `AUDIO_OFF_MIC`（`Intents.AUDIO_ACTION`）

```java
// 按官方事件名接原始 d（Jackson JsonNode）
client.onRaw("GUILD_MEMBER_ADD", json -> log.info("原始事件: {}", json));
// 或一把捞所有未强类型映射的事件，再按事件名 e.getEventType() 分流
client.on(GenericEvent.class, e -> log.info("{} -> {}", e.getEventType(), e.getRaw()));
// 平台新增接口 SDK 未跟随时：注入/构造 core 的 HttpApiClient 调 executeRaw(method, path, body)
```

已建模、**不要**再用 onRaw 接的官方事件：`GUILD_CREATE/UPDATE/DELETE`、`CHANNEL_CREATE/UPDATE/DELETE`、`FRIEND_DEL`、`C2C_MSG_RECEIVE/REJECT`（见 R16 与 api-reference 事件表）。

遇到 Skill 里没有的签名，先到源码仓库 `qqbot-sdk-api|core/src/main/java` grep 确认，或查 Wiki <https://gitee.com/meilinkj/qqbot-sdk/wikis>，仍不确定就明确告诉用户"该 API 未经验证"，不要编造。

## R12 群聊管理（client.groupManage()，对齐官方「群聊管理」16 端点）

```java
// 群信息/成员（GET /info、/members 为白名单接口，未开通报 err_code=11253）
GroupInfo info = client.groupManage().getGroupInfo(groupOpenid);
Page<GroupMember> page = client.groupManage().getGroupMembers(groupOpenid, null); // 单页≤30
while (page.hasNext()) {
    page = client.groupManage().getGroupMembers(groupOpenid, page.getNextCursor());
}

// 批量移除成员（≤20/次，成功返回 "success"；需群主开"移除群成员"权限，否则 40103012/40103011）
client.groupManage().batchRemoveMembers(groupOpenid, List.of(memberOpenid));

// 黑名单：查询（cursor+limit≤100）+ 操作（add/del，≤20/次，返回 fail_openids）
client.groupManage().operateBlacklist(groupOpenid, BlacklistUserRequest.add(List.of(memberOpenid)));

// 禁言：查询状态 + 设置（≤20 成员/次，最长 30 天；机器人需群管理员/群主）
MuteSetting setting = client.groupManage().getMuteSetting(groupOpenid);
client.groupManage().setGroupMute(groupOpenid, MuteSettingRequest.muteTo(memberOpenid, "2026-12-01T00:00:00+08:00"));
client.groupManage().setGroupMute(groupOpenid, MuteSettingRequest.unmute(memberOpenid)); // del+空串=立即解禁

// 入群申请：拉取（需群管理员，limit≤50）→ 审批（approve/decline，decline 可带理由并拉黑）
Page<JoinRequest> reqs = client.groupManage().getJoinRequests(groupOpenid, null, null);
for (JoinRequest jr : reqs.getItems()) {
    client.groupManage().approveJoinRequest(groupOpenid, jr.getMemberOpenid(),
            JoinApprovalOpRequest.approve(jr.getJoinRequestId()));
}

// 自动审批策略：创建（groupOpenids 与 groupIds 互斥，≤100 群，全局最多 20 策略）→ 白名单号码（QQ 号字符串，≤10000/次）
StrategyOperateResult st = client.groupManage().createJoinApprovalStrategy(
        JoinApprovalStrategyRequest.ofGroupOpenids(List.of(groupOpenid)).setRemark("白名单免审"));
client.groupManage().operateJoinApprovalWhitelist(st.getStrategyId(),
        StrategyWhitelistUsersRequest.add(List.of("1234567")));
client.groupManage().executeJoinApprovalStrategy(st.getStrategyId());   // 全量扫描，约 10 分钟异步生效
// 修改用 updateJoinApprovalStrategy(strategyId, new JoinApprovalStrategyUpdateRequest().setIsEnable("off")...)，删除用 deleteJoinApprovalStrategy(strategyId)
```

groupOpenid/memberOpenid 均来自事件或接口返回，不能用 QQ 号推算。

## R13 群成员事件与入群申请审批（Intents.GROUP_MEMBER_EVENT，1&lt;&lt;24 = 16777216）

```java
// 特殊事件，需先在开放平台申请 GROUP_MEMBER_EVENT 权限
client.on(GroupMemberAddEvent.class, e ->
        welcome(e.getGroupOpenid(), e.getMemberOpenid()));          // getTimestamp() Unix 秒

client.on(GroupMemberRemoveEvent.class, e ->
        cleanLocalCache(e.getGroupOpenid(), e.getMemberOpenid()));  // getUserOpenid() 可能为空

// 用户申请加群：只有机器人是群管理员时才会收到；join_request_id 要回传给审批接口
client.on(GroupJoinRequestEvent.class, e -> {
    if (e.getAutoApproved() != null) {
        return;                                      // 已被自动审批策略放行（strategy_id 见 getAutoApproved()）
    }
    boolean pass = e.getVerifyInfo() != null
            && "verify_message".equals(e.getVerifyInfo().getMethod())
            && "正确口令".equals(e.getVerifyInfo().getVerifyMessage());
    client.groupManage().approveJoinRequest(e.getGroupOpenid(), e.getMemberOpenid(),
            pass ? JoinApprovalOpRequest.approve(e.getJoinRequestId())
                 : JoinApprovalOpRequest.decline(e.getJoinRequestId(), "验证答案不正确", false));
});
```

Spring Boot 用位掩码订阅：群+单聊+群成员事件 = `intents: 50331648`（33554432 | 16777216）。

## R14 单聊流式（打字机）回复

```java
// 【事件】C2C_MESSAGE_CREATE → 【接口】POST /v2/users/{user_openid}/stream_messages（sendStream，50 QPS）
client.on(C2CMessageCreateEvent.class, e -> {
    String userOpenid = e.getAuthor().getUserOpenid();
    String msgId = e.getId();                       // 被动回复用，60 分钟内有效
    StreamMessageResponse first = client.c2c().sendStream(userOpenid,
            StreamMessageRequest.replaceMarkdown(msgId, "正在思考…").setMsgSeq(1));
    String streamMsgId = first.getId();             // 首片返回的即 stream_msg_id，后续分片必须携带
    int idx = 1;

    StringBuilder full = new StringBuilder("正在思考…");
    for (String chunk : llmStream) {                // index 从 0 递增，全量正文必须以上一片为前缀
        full.append(chunk);
        client.c2c().sendStream(userOpenid, StreamMessageRequest.replaceMarkdown(msgId, full.toString())
                .setStreamMsgId(streamMsgId).setIndex(idx++).setMsgSeq(1));
    }
    client.c2c().sendStream(userOpenid, StreamMessageRequest.replaceMarkdown(msgId, full.toString())
            .setStreamMsgId(streamMsgId).setIndex(idx).setMsgSeq(1).finish());   // input_state=10 收尾
});
```

append 模式（只发增量）：把 `replaceMarkdown` 换成 `appendText`，content_raw 传本次新增片段即可。改动了已下发前缀会报 err_code=40007；50 QPS 超限报 50002。

## R15 自定义菜单与指令面板（client.menu() / client.panel()）

```java
// 菜单：仅单聊场景、对所有用户生效，PUT 是全量覆盖
client.menu().setMenu(Menu.of(
        MenuItem.sendMessage("帮助", "/help"),
        MenuItem.link("官网", "https://meilinkj.cn"),
        MenuItem.submenu("更多", SubMenuItem.sendMessage("设置", "/settings")),
        MenuItem.toggle("深度搜索", "search", false)));
MenuResponse current = client.menu().getMenu();     // current.getVersion()（Integer）、未设置过则 getMenu()==null

// 面板：按场景 + 生效范围配置，返回 panel_id
String panelId = client.panel().createPanel(PanelCreateRequest.forGroups(
        Panel.of(PanelItem.command("群签到", "每日签到"),
                 PanelItem.link("帮助中心", "https://meilinkj.cn").setOnlyAdmin(true)).setRemark("群面板"),
        List.of(groupOpenid)));
Page<PanelRecord> c2cPanels = client.panel().getPanels(PanelCreateRequest.SCOPE_C2C, null, 20);
client.panel().updatePanelTarget(panelId, PanelTargetRequest.addGroups(newGroupOpenid));
client.panel().updatePanel(panelId, Panel.of(PanelItem.command("新指令", "更新后的指令")));
client.panel().deletePanel(panelId);
```

要点：菜单一级项 name≤10 字符（中文算 2）、最多 10 项，二级最多 5 项；面板最多 20 个、元素最多 20 个、关联对象单次≤20 且必须 target_type=specific（channel/dm 只能 all）。菜单开关与面板指令点击**都不产生 INTERACTION_CREATE**：开关切换后消息 ext 携带 `<switch_id>=1`，面板 command 点击把名称填入输入框，按钮回执（R5）只适用于消息里的 Keyboard 按钮。

## R16 频道与子频道管理（client.channelManage()，需管理员权限）

```java
Guild guild = client.channelManage().getGuild(guildId);
List<Channel> channels = client.channelManage().getChannels(guildId);

Channel category = client.channelManage().createChannel(guildId,
        ChannelCreateRequest.category("新分组", 2));                    // 官方：分组 position 必须 ≥2
Channel text = client.channelManage().createChannel(guildId,
        ChannelCreateRequest.text("公告栏").setParentId(category.getId()).setPosition(1));
client.channelManage().updateChannel(text.getId(), ChannelUpdateRequest.rename("重要公告"));
client.channelManage().updateChannel(text.getId(), ChannelUpdateRequest.move(3, category.getId()));
client.channelManage().deleteChannel(text.getId());
```

频道体系用 guild_id/channel_id，与群聊单聊的 openid 不通用；创建/修改/删除成功后平台会下发 CHANNEL_CREATE / CHANNEL_UPDATE / CHANNEL_DELETE 事件，用强类型事件类接（intents 需含 `Intents.GUILDS`）：

```java
client.on(ChannelCreateEvent.class, e -> log.info("新子频道 {} / 父频道 {}", e.getId(), e.getGuildId()));
client.on(ChannelDeleteEvent.class, e -> log.info("子频道已删除 {}", e.getId()));
// 机器人被拉进/移出频道：GUILD_CREATE / GUILD_DELETE → GuildCreateEvent / GuildDeleteEvent（同包 core.event.guild）
```
