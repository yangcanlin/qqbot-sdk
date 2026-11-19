# qqbot-sdk 1.1.0 API 速查（从源码提取，勿凭记忆增删）

包前缀：`cn.meilinkj.qqbot.api` / `cn.meilinkj.qqbot.core` / `cn.meilinkj.qqbot.spring`

本文件按官方的两条线组织：**接口**（各 `XxxApi` 的方法签名，主动调用 HTTP，官方文档用「方法 + 路径」标识）在前，**事件**（`core.event` 包的事件类与 Intent 对照，平台 WebSocket 推送，官方文档用「事件名 + intents」标识）在文末「事件类」一节。写代码时不要把两者混为一谈：`client.group().sendText(...)` 是接口，`client.on(GroupAtMessageCreateEvent.class, ...)` 是事件；`Intents.GROUP_AND_C2C_EVENT` 是订阅开关，`GROUP_AT_MESSAGE_CREATE` 才是事件名。

## QQBotClient（api 模块，统一入口，AutoCloseable）

```java
new QQBotClient(QQBotConfig config)
void start()                                            // 连网关：自动鉴权/心跳/重连/会话恢复
<E extends BotEvent> QQBotClient on(Class<E>, Consumer<E>)   // 类型化监听
<E extends BotEvent> QQBotClient on(EventListener<E>)        // 自定义监听器接口
QQBotClient onRaw(String eventType, Consumer<JsonNode>)      // 原始事件（官方无字段规格的事件）
GroupApi group() | GroupManageApi groupManage() | C2CApi c2c() | ChannelApi channel()
ChannelManageApi channelManage() | InteractionApi interaction() | BotApi bot()
MenuApi menu() | PanelApi panel() | GatewayInfoApi gatewayInfo()
GatewayClient gateway()   // isConnected() / getSessionId() / getLastSeq()
EventDispatcher dispatcher()
QQBotConfig getConfig()
void shutdown() / void close()
```

## BotApi（`client.bot()`，机器人信息，对齐官方「服务端接口/机器人」）

```java
BotInfo getBot() / CompletableFuture<BotInfo> getBotAsync()        // GET /users/@me，50 QPS，id/username/avatar/bot/share_url/welcome_msg
List<BotGuild> getGuilds()                                          // GET /users/@me/guilds，50 QPS，默认单页最多 100
List<BotGuild> getGuilds(String before, String after, Integer limit) // limit 1~100；before 与 after 同设时 after 无效
GroupBotState getGroupBotState(String groupOpenid)                  // GET /v2/groups/{openid}/bot_state，30 QPM，仅白名单可用（否则 err_code=11253）
CompletableFuture<GroupBotState> getGroupBotStateAsync(String groupOpenid)
ShareLinkResponse generateShareLink(String callbackData)            // POST /v2/generate_url_link，50 QPS，callbackData≤32 字符，可 null；getUrl() 取链接
```

## GroupManageApi（`client.groupManage()`，官方「服务端接口/群聊管理」16 端点）

分页统一返回 `Page<T>`：`getItems()` / `getNextCursor()` / `hasNext()`（cursor 翻页）。

```java
GroupInfo getGroupInfo(String groupOpenid)                         // GET /v2/groups/{g}/info，30QPM，白名单(否则11253)；getGroupInfoAsync 同
Page<GroupMember> getGroupMembers(String groupOpenid, String cursor)      // GET .../members，60QPM，白名单，单页≤30
GroupMember getGroupMember(String groupOpenid, String memberOpenid)       // GET .../members/{m}，30QPM，白名单；Async 同
String batchRemoveMembers(String groupOpenid, List<String> memberOpenids) // POST .../batch_remove_members，30QPM，≤20/次，成功返回 "success"
Page<BlacklistEntry> getBlacklistMembers(String groupOpenid, String cursor, Integer limit) // GET .../member_blacklist，limit≤100 默认20
BlacklistOperateResult operateBlacklist(String groupOpenid, BlacklistUserRequest req)       // POST 同路径，≤20/次；getFailOpenids()
MuteSetting getMuteSetting(String groupOpenid)                     // GET .../restrict_chat_setting，30QPM；getMuteSettingAsync 同
void setGroupMute(String groupOpenid, MuteSettingRequest req)      // POST 同路径，60QPM，≤20 成员，最长30天，机器人需群管理员
Page<JoinRequest> getJoinRequests(String groupOpenid, String cursor, Integer limit) // GET .../join_request_list，limit≤50 默认20，需群管理员
void approveJoinRequest(String groupOpenid, String memberOpenid, JoinApprovalOpRequest req) // POST .../approval_join_request/{m}，60QPM
Page<JoinApprovalStrategy> getJoinApprovalStrategies(String cursor, Integer limit)  // GET /v2/groups/join_approval_strategy，limit≤50 默认20
StrategyOperateResult createJoinApprovalStrategy(JoinApprovalStrategyRequest req)   // POST 同路径，groupOpenids/groupIds 互斥必传其一(≤100)，全局≤20 策略
StrategyOperateResult updateJoinApprovalStrategy(String strategyId, JoinApprovalStrategyUpdateRequest req) // PATCH .../{id}，isEnable/expireAt/groupAction/remark 局部更新
void deleteJoinApprovalStrategy(String strategyId)                 // DELETE .../{id}；deleteJoinApprovalStrategyAsync 同
void executeJoinApprovalStrategy(String strategyId)                // POST .../{id}/execute，全量扫描，异步约10分钟
StrategyWhitelistResult operateJoinApprovalWhitelist(String strategyId, StrategyWhitelistUsersRequest req) // POST .../{id}/whitelist_users，≤10000 号/次
```

请求体工厂：`BlacklistUserRequest.add/remove(List<openid>)`、`MuteSettingRequest.muteTo(openid, rfc3339) / .unmute(openid) / .addMemberOp(op, openid, expireAt)`、`JoinApprovalOpRequest.approve(joinRequestId) / .decline(joinRequestId, reason, addToBlacklist)`、`JoinApprovalStrategyRequest.ofGroupOpenids(...) / .ofGroupIds(List<String>群号)` + `setIsEnable/setExpireAt/setRemark`、`StrategyGroupAction.addOpenids/removeOpenids/addGroupIds/removeGroupIds`、`StrategyWhitelistUsersRequest.add/remove(List<QQ号字符串>)`。
MuteSetting：`getGlobalRule()`（mode=none/always/schedule，scheduleRules/recurringRules）+ `getMembers()`（memberOpenid/muteExpireAt）。
JoinRequest：`getJoinRequestId()`（审批回传）`getMemberOpenid()`（路径参数）`getApplySource()`（self_apply/invited）`getVerifyInfo()`。

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

与 GroupApi 同签名（首参换成 userOpenid），另有单聊独有能力：

```java
void sendInputNotify(String userOpenid, int seconds)     // POST .../messages msg_type=6 输入中提示，1~60 秒
SendMessageResponse sendWakeupText(String userOpenid, String content)  // POST .../messages is_wakeup=true 唤醒消息（与 msg_id 互斥）
StreamMessageResponse sendStream(String userOpenid, StreamMessageRequest req)  // POST .../stream_messages，50 QPS，流式打字机；sendStreamAsync 同
UserInfo getUserInfo(String userOpenid)                  // GET .../info，10 QPS；getUnionOpenid/getUsername/getAvatar/getAllowProactiveMsg；getUserInfoAsync 同
// 无好友关系 → err_code=40184001
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

## ChannelManageApi（`client.channelManage()`，官方「服务端接口/频道管理」6 端点，均 50 QPS，需管理员权限）

```java
Guild getGuild(String guildId)                                          // GET /guilds/{g}；getGuildAsync 同
List<Channel> getChannels(String guildId)                               // GET /guilds/{g}/channels，官方无分页，一次返回全部
Channel createChannel(String guildId, ChannelCreateRequest req)         // POST .../channels，成功触发 CHANNEL_CREATE；createChannelAsync 同
Channel getChannel(String channelId)                                    // GET /channels/{c}；getChannelAsync 同
Channel updateChannel(String channelId, ChannelUpdateRequest req)       // PATCH /channels/{c} 局部更新；updateChannelAsync 同
void deleteChannel(String channelId)                                    // DELETE /channels/{c}，无响应体，不可恢复；deleteChannelAsync 同

// 请求体（静态工厂 + 链式 setter）
ChannelCreateRequest.text(name) / .voice(name) / .category(name, position) // position<2 抛 IAE（官方：分组 position 必须 ≥2）
ChannelUpdateRequest.rename(name) / .move(position, parentId)
// Channel 常量：TYPE_TEXT=0 TYPE_VOICE=2 TYPE_CATEGORY=4 TYPE_LIVE=10005 TYPE_APP=10006 TYPE_FORUM=10007
//              SUB_TYPE_CHAT/NOTICE/STRATEGY/GAME=0..3  PRIVATE_TYPE_PUBLIC/ADMIN/ASSIGN=0..2
//              SPEAK_PERMISSION_INVALID/ALL/ADMIN_ASSIGN=0..2
```

## MenuApi（`client.menu()`，自定义菜单，仅 C2C 单聊场景、对所有用户生效）

```java
MenuResponse getMenu()                               // GET /v2/menu，30 QPM；version 为 Integer，未设置过菜单时 getMenu() 为 null
VersionResponse setMenu(Menu menu)                   // PUT /v2/menu，5 QPM，全量覆盖原有配置，返回新的 version（Integer）；setMenuAsync / getMenuAsync 同
// Menu.of(MenuItem...)；官方限制：items≤10（超出抛 IAE）
MenuItem.sendMessage(name, content)  // type=send_message：点击后文本填入输入框（不产生互动事件）
MenuItem.link(name, url)             // type=link：非 https:// 前缀直接抛 IAE（官方 40030008）
MenuItem.submenu(name, SubMenuItem...)  // type=menu：子项 1~5 个（>5 或空抛 IAE），二级仅 send_message/link
MenuItem.toggle(name, switchId, defaultOn) // type=switch：用户打开开关后消息 ext 携带 "<switchId>=1"
// 名称长度（官方）：一级≤10 字符（中文算 2）、二级≤14 字符；错误码 40030013 数量超限 / 40030014 类型非法 / 40030016 必填缺失 / 40030020 内容安全风险
```

## PanelApi（`client.panel()`，指令面板，官方 6 端点，一个机器人最多 20 个面板）

```java
Page<PanelRecord> getPanels(String scope)                       // GET /v2/panels?scope=，30 QPM，按设置时间倒序
Page<PanelRecord> getPanels(String scope, String cursor, Integer limit) // limit 1~50 默认 20（越界抛 IAE）；next_cursor 空串=最后一页
String createPanel(PanelCreateRequest req)                      // POST /v2/panels，10 QPM，返回 panel_id；createPanelAsync 同
PanelRecord getPanel(String panelId)                            // GET /v2/panels/{id}，30 QPM，specific 面板附 user_openids/group_openids（≤1000）；getPanelAsync 同
VersionResponse updatePanel(String panelId, Panel panel)        // PUT /v2/panels/{id}，10 QPM，覆盖 items+remark、不影响关联对象，返回新 version；updatePanelAsync 同
void deletePanel(String panelId)                                // DELETE /v2/panels/{id}，10 QPM，无响应体；deletePanelAsync 同
void updatePanelTarget(String panelId, PanelTargetRequest req)  // PUT /v2/panels/{id}/target，60 QPM，无响应体；Async 同

PanelCreateRequest.global(scope, panel)                        // scope 常量 SCOPE_C2C/GROUP/CHANNEL/DM，target_type=all
PanelCreateRequest.forUsers(panel, List<String> userOpenids)    // scope=c2c + specific，≤20（空或>20 抛 IAE）
PanelCreateRequest.forGroups(panel, List<String> groupOpenids)  // scope=group + specific，≤20
// channel / dm 场景传 specific → 抛 IAE（官方 40030012）
Panel.of(PanelItem...) .setRemark(≤255)                        // items≤20（超出抛 IAE，官方 40030013）
PanelItem.command(name, desc)   // type=command：点击后 name 填入输入框（不产生互动事件）；name≤14、desc≤30
PanelItem.link(name, url)       // 非 https:// 抛 IAE（官方 40030008）；可 .setOnlyAdmin(true)（仅管理员可点）
PanelTargetRequest.addUsers(...)/delUsers(List)/addGroups(...)/delGroups(List)  // op=add|del，单次≤20；全局面板报 40030021、channel/dm 报 40030018
// PanelRecord：getPanelId/getScope/getTargetType/getPanel/getVersion/getCreatedAt/getUpdatedAt/getUserOpenids/getGroupOpenids
```

## GatewayInfoApi（`client.gatewayInfo()`）

```java
GatewayInfo getGateway()   // GET /gateway，2 QPM / 10 QPM burst，getUrl() 取 wss 地址；getGatewayAsync 同
GatewayBotInfo getGatewayBot()   // GET /gateway/bot（官方 openapi/wss 章节「获取带分片 WSS 接入点」）
                                 // getShards() 为平台建议分片数，getSessionStartLimit() 给出
                                 // total/remaining/resetAfter(ms)/maxConcurrency 配额；getGatewayBotAsync 同
// SDK 建连内部已自动调用 GET /gateway，业务侧一般在探活、自建长连接或多分片部署前才用
```

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
setMsgId(String) / setEventId(String) / setIsWakeup(Boolean) / setInputNotify(InputNotify.typing(seconds))  // typing 限 1~60 秒
setMsgType(Integer) / setContent / setMarkdown / setMedia / setCard / setMessageReference(V2MessageReference.of(id))
```

MessageMarkdown：`ofContent(String)` / `setContent(String)` / `setForceVerifyImageResource(Boolean)`（true 时图片转存失败直接报错不发送，默认 false）；`setTemplateId(Integer)` / `setCustomTemplateId(String)` 源码已标注【已废弃】，新代码只用原生 content。

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

## StreamMessageRequest（流式打字机，POST /v2/users/{openid}/stream_messages）

```java
static StreamMessageRequest appendText(String msgId, String contentRaw)      // input_mode=append + text，index=0，生成中
static StreamMessageRequest replaceMarkdown(String msgId, String fullContent) // input_mode=replace + markdown，index=0
  .setStreamMsgId(String)   // 首片不传；后续分片必须回传首片响应的 getId()
  .setIndex(int)            // 从 0 递增（负数抛 IAE）
  .setMsgSeq(int)           // 去重序号
  .setContentType("text"|"markdown")   // CONTENT_TYPE_TEXT / CONTENT_TYPE_MARKDOWN
  .finish()                 // input_state=10 结束（INPUT_STATE_GENERATING=1 / FINISHED=10）
  .replyByEventId(String)   // 用 event_id 被动回复（与 msg_id 二选一）
  .setIsWakeup(true)        // 召回消息，不校验 msg_id/event_id 有效期
// 常量 INPUT_MODE_APPEND / INPUT_MODE_REPLACE（其它值抛 IAE）
// 响应 StreamMessageResponse：getId()（首片=stream_msg_id）/getTimestamp()/getRemainMsgLen()/getExtInfo().getRefIdx()
// 错误码：40007 replace 模式改动了已下发前缀；50002 频率限制
```

## 上传与富媒体

- file_type：1图片 2视频 3语音 4文件；
- `MediaUploadRequest.ofUrl(int fileType, String url)`，`setSrvSendMsg(Boolean)` 平台代发（默认 false）；
- `MediaUploadResponse`：`getFileInfo()`（发消息用）/ `getTtl()`（秒，有效期内复用）/ `getFileUuid()`；
- 分片判定：>5MB 走 uploadChunked（prepare→逐块 PUT→finish，签名校验 md5/sha1/md5_10m）。

## 事件类（core.event 包，均继承 BotEvent：getEventId/getSeq/getEventType/getRaw）

事件是**平台通过 WebSocket 推送**的，和上面各 `XxxApi` 的**主动调用接口**是两条线：接口用 HTTP 方法与路径标识、有 QPS/QPM 频控；事件用大写下划线**事件名** + Intent 位标识、没有频控概念。写代码时的对应关系：`client.xxx().yyy()` = 调接口，`client.on(事件类, handler)` / `client.onRaw("事件名", handler)` = 收事件。

注意 `MESSAGE_AUDIT`、`GUILD_MESSAGES`、`GROUP_AND_C2C_EVENT` 这些是 **Intent 名（订阅开关）**，不是事件名；下表「事件名」列一律填官方 `t` 字段的值。

| 事件类 | 事件名（EventType 名） | 常用取值 |
|---|---|---|
| ReadyEvent / ResumedEvent | READY / RESUMED（网关握手与恢复，不需要 Intent） | getSessionId() getShard() getUser() |
| GroupAtMessageCreateEvent | GROUP_AT_MESSAGE_CREATE | getId() getContent() getGroupOpenid() getAuthor()（回复用 author.getUserOpenid() 定位成员） |
| C2CMessageCreateEvent | C2C_MESSAGE_CREATE | getId() getContent() getAuthor().getUserOpenid() getAttachments() |
| GroupAddRobotEvent / GroupDelRobotEvent | GROUP_ADD_ROBOT / GROUP_DEL_ROBOT | getGroupOpenid() getOpMemberOpenid() |
| GroupMessageCreateEvent | GROUP_MESSAGE_CREATE | 群消息全量模式（平台开启"接收所有消息"后推送非@消息），字段同 GroupAtMessageCreateEvent |
| GroupMsgReceiveEvent / GroupMsgRejectEvent | GROUP_MSG_RECEIVE / GROUP_MSG_REJECT | getGroupOpenid()（群聊消息接收开启/关闭） |
| GroupMemberAddEvent / GroupMemberRemoveEvent | GROUP_MEMBER_ADD / GROUP_MEMBER_REMOVE | getGroupOpenid() getMemberOpenid() getTimestamp()；Remove 另有 getUserOpenid()（可能为空） |
| GroupJoinRequestEvent | GROUP_JOIN_REQUEST | getJoinRequestId()（审批接口回传）getMemberOpenid() getUsername() getApplyAt() getApplySource() getInvitedBy() getRiskTips() getVerifyInfo() getAutoApproved()；仅机器人是群管理员时可收 |
| SubscribeMessageStatusEvent | SUBSCRIBE_MESSAGE_STATUS | getGroupOpenid()（群订阅）/ getOpenid()（个人订阅）/ getResult()：templateId customTemplateId op(1=允许 2=拒绝) subscribeId(发订阅消息用) subscribeTs updateTs |
| FriendAddEvent | FRIEND_ADD | getOpenid()（单聊欢迎语场景） |
| InteractionCreateEvent | INTERACTION_CREATE | getId()（回执用）getData().getResolved().getButtonId()/getButtonData() getGroupOpenid()/getUserOpenid()/getGuildId()/getChannelId()/getGroupMemberOpenid() |
| MessageAuditedEvent | MESSAGE_AUDIT_PASS / MESSAGE_AUDIT_REJECT（Intent 才是 `MESSAGE_AUDIT`，1<<27） | isPass()（判审核结果，在事件对象上）、getAudited()（MessageAudited 详情） |
| MessageDeleteEvent | MESSAGE_DELETE / PUBLIC_MESSAGE_DELETE / DIRECT_MESSAGE_DELETE（频道撤回，官方无独立事件页，按 MessageDelete 对象建模） | getMessage() getOpUser() |
| ChannelMessageEvent 系 | AT_MESSAGE_CREATE（1<<30）/ MESSAGE_CREATE（1<<9）/ DIRECT_MESSAGE_CREATE（1<<12） | getMessage()（core.model.channel.Message，官方事件页写「内容为 Message 对象」） |
| GuildCreateEvent / GuildUpdateEvent / GuildDeleteEvent（core.event.guild，继承 GuildEvent） | GUILD_CREATE / GUILD_UPDATE / GUILD_DELETE（Intents.GUILDS） | getId() getName() getIcon() getOwnerId() getMemberCount() getMaxMembers() getDescription() getJoinedAt() getOpUserId()；Create=加入频道触发；Update 为变更后数据、Delete 为变更前数据（官方无差分字段）；Delete 也含"机器人被移出频道" |
| ChannelCreateEvent / ChannelUpdateEvent / ChannelDeleteEvent（core.event.guild，继承 ChannelEvent） | CHANNEL_CREATE / CHANNEL_UPDATE / CHANNEL_DELETE（Intents.GUILDS） | getId() getGuildId() getName() getType()（0文字/2语音/4分组/10005直播/10006应用/10007论坛）getSubType() getPosition() getOwnerId() getOpUserId()；channelManage 增删改成功后平台也推这些事件 |
| FriendDelEvent | FRIEND_DEL（Intents.GROUP_AND_C2C_EVENT） | getOpenid() getTimestamp() getAuthor().getUnionOpenid()（官方示例未含 author，按可空处理） |
| C2CMsgReceiveEvent / C2CMsgRejectEvent | C2C_MSG_RECEIVE / C2C_MSG_REJECT（Intents.GROUP_AND_C2C_EVENT） | getOpenid() getTimestamp()；用户在资料卡开启/关闭「主动消息」开关，关闭后主动发消息会失败 |
| GenericEvent | 官方 api-v2 无事件体规格的事件：GUILD_MEMBER_ADD/UPDATE/REMOVE、MESSAGE_REACTION_ADD/REMOVE、FORUM_*（8 个）、AUDIO_*（4 个），以及平台新增未收录事件 | getRaw()（原始 d），或改用 `client.onRaw(事件名, consumer)` |

## Intents（`Intents.of(...)` 组合成位掩码）

| 常量 | 位值 | 覆盖 |
|---|---|---|
| GUILDS | 1<<0 | 频道增退 |
| GUILD_MEMBERS | 1<<1 | 频道成员 |
| GUILD_MESSAGES | 1<<9 | 频道消息（MESSAGE_CREATE、MESSAGE_DELETE） |
| GUILD_MESSAGE_REACTIONS | 1<<10 | 频道表态 |
| DIRECT_MESSAGE | 1<<12 | 频道私信 |
| GROUP_MEMBER_EVENT | 1<<24 (16777216) | 群成员加入/退出、用户申请加群（特殊事件，需平台开通） |
| GROUP_AND_C2C_EVENT | 1<<25 (33554432) | 群@消息、群消息全量、单聊、入群/好友、订阅消息授权状态（默认值） |
| INTERACTION | 1<<26 (67108864) | 按钮点击 |
| MESSAGE_AUDIT | 1<<27 | 消息审核结果 |
| FORUMS_EVENT | 1<<28 | 论坛 |
| AUDIO_ACTION | 1<<29 | 音频播放 |
| PUBLIC_GUILD_MESSAGES | 1<<30 | 公域频道 @消息与撤回（AT_MESSAGE_CREATE、PUBLIC_MESSAGE_DELETE） |

组合示例：群+单聊+按钮 = `Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.INTERACTION)` = 100663296；群+群成员事件 = `Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.GROUP_MEMBER_EVENT)` = 50331648。

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
// 官方 api-v2 没有事件体规格的事件（GUILD_MEMBER_*、MESSAGE_REACTION_*、FORUM_*、AUDIO_*）与平台新增事件，
// 用 client.onRaw("EVENT_NAME", json -> ...) 或 client.on(GenericEvent.class, e -> e.getRaw())
```