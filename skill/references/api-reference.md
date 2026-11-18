# qqbot-sdk 1.0.0 API 速查（从源码提取，勿凭记忆增删）

包前缀：`cn.meilinkj.qqbot.api` / `cn.meilinkj.qqbot.core` / `cn.meilinkj.qqbot.spring`

## QQBotClient（api 模块，统一入口，AutoCloseable）

```java
new QQBotClient(QQBotConfig config)
void start()                                            // 连网关：自动鉴权/心跳/重连/会话恢复
<E extends BotEvent> QQBotClient on(Class<E>, Consumer<E>)   // 类型化监听
<E extends BotEvent> QQBotClient on(EventListener<E>)        // 自定义监听器接口
QQBotClient onRaw(String eventType, Consumer<JsonNode>)      // 未建模事件
GroupApi group() | C2CApi c2c() | ChannelApi channel() | InteractionApi interaction()
GatewayClient gateway()   // isConnected() / getSessionId() / getLastSeq()
EventDispatcher dispatcher()
QQBotConfig getConfig()
void shutdown() / void close()
```

## GroupApi（`client.group()`，路径 /v2/groups/{groupOpenid}/...）

```java
SendMessageResponse sendMessage(String groupOpenid, MessageRequest req)
CompletableFuture<SendMessageResponse> sendMessageAsync(String groupOpenid, MessageRequest req)
SendMessageResponse sendText(String groupOpenid, String content)
SendMessageResponse replyText(String groupOpenid, String msgId, String content)  // 被动回复，msg_seq 自动
SendMessageResponse sendImageByUrl(String groupOpenid, String imageUrl)           // 图URL→上传→发送 一步
void recall(String groupOpenid, String messageId)                                 // 2 分钟内
CompletableFuture<Void> recallAsync(String groupOpenid, String messageId)
MediaUploadResponse uploadMedia(String groupOpenid, MediaUploadRequest req)        // URL 方式
MediaUploadResponse uploadBinary(String groupOpenid, int fileType, String fileName, byte[] data)  // ≤5MB
MediaUploadResponse uploadChunked(String groupOpenid, int fileType, String fileName, byte[] data) // 自动分片
MediaUploadResponse uploadFile(String groupOpenid, int fileType, Path file) throws IOException    // 按大小自动选直传/分片
```

## C2CApi（`client.c2c()`，路径 /v2/users/{userOpenid}/...）

与 GroupApi 同签名（首参换成 userOpenid），另有两个独有能力：

```java
void sendInputNotify(String userOpenid, int seconds)     // 输入中提示，1~60 秒
SendMessageResponse sendWakeupText(String userOpenid, String content)  // 唤醒消息（主动触达，与 msg_id 互斥）
```

## ChannelApi（`client.channel()`，频道体系用 guildId/channelId，与 openid 体系不同）

```java
Message sendMessage(String channelId, ChannelMessageRequest req)
CompletableFuture<Message> sendMessageAsync(String channelId, ChannelMessageRequest req)
void recall(String channelId, String messageId, boolean hideTip)
Dms createDms(String recipientId, String sourceGuildId)              // 开私信，Dms.getGuildId()/getChannelId()
Message sendDms(String dmsGuildId, ChannelMessageRequest req)        // 发私信
void recallDms(String dmsGuildId, String messageId, boolean hideTip)
```

频道被动回复：msg_id 5 分钟有效，**无 msg_seq 概念**。每子频道 5 条/秒。

## InteractionApi（`client.interaction()`，按钮点击回执，50 QPS，一次性）

```java
void respond(String interactionId, int code)
void respondSuccess(String interactionId)                       // = respond(id, 0)
CompletableFuture<Void> respondAsync(String interactionId, int code)
// code 常量（源码核实）：CODE_SUCCESS=0 CODE_FAILED=1 CODE_FREQUENT=2
//                          CODE_DUPLICATE=3 CODE_NO_PERMISSION=4 CODE_ADMIN_ONLY=5
```

## MessageRequest（群聊/单聊请求体，静态工厂 + 链式 setter）

```java
static MessageRequest text(String content)
static MessageRequest markdown(MessageMarkdown md)
static MessageRequest media(String fileInfo)        // fileInfo 来自 MediaUploadResponse.getFileInfo()
static MessageRequest card(MessageCard card)        // 图文卡片：MessageCard.tuwen(title,desc,url,picUrl)
MessageRequest replyTo(String msgId, int msgSeq)    // 被动回复；SDK 的 replyText/sendXxx 已自动处理
MessageRequest setKeyboard(Keyboard kb)             // 任何类型消息可挂按钮
setMsgId(String) / setEventId(String) / setIsWakeup(Boolean) / setInputNotify(InputNotify)
setMsgType(Integer) / setContent / setMarkdown / setMedia / setCard / setMessageReference(V2MessageReference.of(id))
```

MessageMarkdown：`ofContent(String)` / `setTemplateId(Integer)` / `setCustomTemplateId(String)`。

## Keyboard（按钮键盘，全静态工厂+链式）

```java
static Keyboard ofTemplate(String id)               // 平台后台模板
static Keyboard ofRows(List<Row> rows)
static Keyboard.Row of(Button... buttons)
Button: setId(String) setRenderData(RenderData) setAction(Action) setGroupId(String)
RenderData: setLabel(String) setVisitedLabel(String) setStyle(Integer)   // style 1蓝2浅4灰等以官方为准
Action: setType(Integer)  # 0=跳转(http/小程序) 1=回调(触发INTERACTION_CREATE) 2=指令(输入框插入@bot data)
        setPermission(Permission.all()) setData(String) setClickLimit(Integer)
        setUnsupportTips(String) setEnter(Boolean) setReply(Boolean) setAnchor(Integer) setModal(Modal)
```

## ChannelMessageRequest（频道请求体）

```java
static ChannelMessageRequest text(String content) | image(String imageUrl)
replyTo(String msgId)  setEmbed(MessageEmbed) setArk(MessageArk)
setMarkdown(ChannelMarkdown) setMessageReference(MessageReference) setMsgId/setEventId
```

## 上传与富媒体

- file_type：1图片 2视频 3语音 4文件；
- `MediaUploadRequest.ofUrl(int fileType, String url)`，`setSrvSendMsg(Boolean)` 平台代发（默认 false）；
- `MediaUploadResponse`：`getFileInfo()`（发消息用）/ `getTtl()`（秒，有效期内复用）/ `getFileUuid()`；
- 分片判定：>5MB 走 uploadChunked（prepare→逐块 PUT→finish，签名校验 md5/sha1/md5_10m）。

## 事件类（core.event 包，均继承 BotEvent：getEventId/getSeq/getEventType/getRaw）

| 事件类 | 触发（EventType 名） | 常用取值 |
|---|---|---|
| ReadyEvent / ResumedEvent | READY / RESUMED | getSessionId() getShard() getUser() |
| GroupAtMessageCreateEvent | GROUP_AT_MESSAGE_CREATE | getId() getContent() getGroupOpenid() getAuthor()（回复用 author.getUserOpenid() 定位成员） |
| C2CMessageCreateEvent | C2C_MESSAGE_CREATE | getId() getContent() getAuthor().getUserOpenid() getAttachments() |
| GroupAddRobotEvent / GroupDelRobotEvent | GROUP_ADD_ROBOT / GROUP_DEL_ROBOT | getGroupOpenid() getOpMemberOpenid() |
| FriendAddEvent | FRIEND_ADD | getOpenid()（单聊欢迎语场景） |
| InteractionCreateEvent | INTERACTION_CREATE | getId()（回执用）getData().getResolved().getButtonId()/getButtonData() getGroupOpenid()/getUserOpenid()/getGuildId()/getChannelId()/getGroupMemberOpenid() |
| MessageAuditedEvent | MESSAGE_AUDIT | getAudited() isPass() |
| MessageDeleteEvent | MESSAGE_DELETE（频道） | getMessage() getOpUser() |
| ChannelMessageEvent 系 | AT_MESSAGE_CREATE / DIRECT_MESSAGE_CREATE 等 | getMessage()（core.model.channel.Message） |
| GenericEvent | 未建模事件 | getRaw() |

## Intents（`Intents.of(...)` 组合成位掩码）

| 常量 | 位值 | 覆盖 |
|---|---|---|
| GUILDS | 1<<0 | 频道增退 |
| GUILD_MEMBERS | 1<<1 | 频道成员 |
| GUILD_MESSAGES | 1<<9 | 频道消息（AT_MESSAGE_CREATE 等） |
| GUILD_MESSAGE_REACTIONS | 1<<10 | 频道表态 |
| DIRECT_MESSAGE | 1<<12 | 频道私信 |
| GROUP_AND_C2C_EVENT | 1<<25 (33554432) | 群@消息、单聊、入群/好友（默认值） |
| INTERACTION | 1<<26 (67108864) | 按钮点击 |
| MESSAGE_AUDIT | 1<<27 | 消息审核结果 |
| FORUMS_EVENT | 1<<28 | 论坛 |
| AUDIO_ACTION | 1<<29 | 音频播放 |
| PUBLIC_GUILD_MESSAGES | 1<<30 | 公域频道消息 |

组合示例：群+单聊+按钮 = `Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.INTERACTION)` = 100663296。

## QQBotConfig.builder(appId, appSecret) 全部可选项与默认值

```java
.baseUrl("https://api.bot.qq.com")  // 默认即官方统一域名，勿改
.intents(33554432)                   // GROUP_AND_C2C_EVENT
.shard(0, 1)                         // [shardId, numShards]
.maxRetries(3) .retryBaseDelayMs(500) .retryMaxDelayMs(30000)
.requestsPerSecond(20)               // 客户端全局限流（固定窗口，超量本地排队）
.connectTimeoutMs(10000) .readTimeoutMs(15000) .writeTimeoutMs(15000)
.reconnectBaseDelayMs(1000) .reconnectMaxDelayMs(60000)
.eventThreads(4)                     // 事件分发线程池
.build()
```

## 异常体系

```java
QQBotException extends RuntimeException
ApiException extends QQBotException
    getHttpStatus()  getErrCode()  getMessage()  getTraceId()  getRawBody()
    isPendingAudit()   // 201/202 审核中语义（err 304023/304024）
    isRateLimited()    // 平台限流
```

SDK 自动处理：401 刷 token 重试、429/5xx/IO 指数退避重试（maxRetries 内）、4xx 不重试直接抛。

## Spring Boot Starter（cn.meilinkj.qqbot.spring）

配置前缀 `qqbot.`（QQBotProperties）：`app-id` `app-secret` `base-url` `intents`(long) `shard-id`(0) `num-shards`(1) `max-retries`(3) `requests-per-second`(20) `event-threads`(4) `auto-start`(true)。

自动装配：`qqbot.app-id` 存在时创建 `QQBotClient` Bean（destroyMethod=shutdown）；容器内所有 `EventListener<?>` Bean 自动注册；`ApplicationRunner` 按 auto-start 自动 `start()`。

## 网关可观测 & 原始调用扩展

```java
client.gateway().isConnected() / getSessionId() / getLastSeq()
// 官方文档没建好的新接口，可用 core 的 HttpApiClient 原样调用（executeRaw/get/post/put/delete）
// 未建模事件用 client.onRaw("EVENT_NAME", json -> ...)
```