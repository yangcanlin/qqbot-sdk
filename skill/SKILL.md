---
name: qqbot-sdk
description: 使用 qqbot-sdk（Maven cn.meilinkj.qqbot）进行 QQ 开放平台机器人 Java 开发：群聊@回复、单聊C2C、频道消息、富媒体图片文件、按钮互动、消息撤回、群聊管理（群成员/黑名单/禁言/入群审批）、频道管理（子频道增删改）、自定义菜单与指令面板、单聊流式打字机消息、群成员与入群申请事件、频道与子频道变更事件、Spring Boot 接入。当项目依赖 qqbot-sdk / QQBotClient / QQBotConfig，或用户要求开发 QQ 机器人功能、排查收不到消息/回复失败问题时使用。仅支持官方 api-v2 协议，禁止 OneBot/Simbot/NapCat 等非官方协议。
---

# qqbot-sdk：QQ 机器人 Java 开发

## Overview

qqbot-sdk 是严格对齐 QQ 机器人官方开发文档（V2）的 Java SDK，覆盖群聊、单聊（C2C）、频道三类场景的消息收发，以及群聊管理、频道管理、自定义菜单/指令面板等管理接口。本 Skill 提供准确的方法签名、任务模板与平台规则，避免凭记忆猜测 API 造成幻觉。

- 构件：`cn.meilinkj.qqbot:qqbot-sdk-api:1.1.0`（Java 17+；core 会传递引入）
- Spring Boot 3.x：改用 `qqbot-sdk-spring-boot-starter`
- 主仓库：<https://gitee.com/meilinkj/qqbot-sdk>；详细文档：<https://gitee.com/meilinkj/qqbot-sdk/wikis>
- 官方协议文档（唯一依据）：<https://bot.q.qq.com/wiki/develop/api-v2/>

## 硬约束（生成代码必须遵守）

1. 只使用本 Skill 或源码中真实存在的类与方法，不确定时先查 `references/api-reference.md`，禁止臆造签名；
2. 协议、字段、频控以官方 api-v2 文档为准，禁止生成 OneBot / Simbot / NapCat 等非官方协议代码；
3. appId/appSecret 一律从环境变量或配置注入，严禁硬编码进代码或写入 application.yml 明文；
4. 统一接口域名 `https://api.bot.qq.com`（SDK 默认值，不要改）；AccessToken 由 SDK 自动管理，不要手写换票逻辑；
5. SDK 用 SLF4J，示例项目建议带 logback-classic 或 slf4j-simple。
6. **接口与事件是两条线，不要混**：`client.group()/c2c()/channel()/bot()/groupManage()/channelManage()/menu()/panel()/interaction()/gatewayInfo()` 上的方法是**接口**（官方按「HTTP 方法 + 路径」描述，主动调用、有频控）；`client.on(XxxEvent.class, ...)` / `client.onRaw("事件名", ...)` 收的是**事件**（官方按「事件名 + intents」描述，平台 WebSocket 推送、无频控）。`Intents.MESSAGE_AUDIT`、`GROUP_AND_C2C_EVENT` 这类是 **Intent 名**，不能当事件名写进 `onRaw`；事件名是大写下划线的 `GROUP_AT_MESSAGE_CREATE`、`MESSAGE_AUDIT_PASS` 这种。

## 最小骨架（所有功能都从这里起步）

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.1.0</version>
</dependency>
```

```java
QQBotConfig config = QQBotConfig.builder(
        System.getenv("QQBOT_APP_ID"), System.getenv("QQBOT_APP_SECRET"))
        .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT))
        .build();

try (QQBotClient client = new QQBotClient(config)) {
    client.on(GroupAtMessageCreateEvent.class, e ->
        client.group().replyText(e.getGroupOpenid(), e.getId(), "回声: " + e.getContent()));
    client.start();                      // 自动鉴权、心跳、断线重连、会话恢复
    Thread.currentThread().join();
}
```

## 任务导航

按需求读 `references/recipes.md` 对应模板（编号一一对应，R1~R16），方法签名速查读 `references/api-reference.md`：

| 用户想做的事 | 用哪个模板 | 关键入口 |
|---|---|---|
| 群聊被@后回复 | R1 | `client.group().replyText(groupOpenid, msgId, text)` |
| 主动往群里推消息/公告 | R1 主动消息 | `group().sendText / sendMessage`（需消息模板） |
| 私聊问答机器人 | R2 | `client.c2c()`，含输入中提示、唤醒消息 |
| 发图片/视频/语音/文件 | R3 | `sendImageByUrl` 或 `uploadMedia/uploadBinary/uploadFile` → `MessageRequest.media(fileInfo)` |
| markdown / 图文卡片 | R4 | `MessageRequest.markdown(...) / card(...)` |
| 按钮键盘 + 点击回执 | R5 | 【接口】`Keyboard.ofRows(...)` 随消息发出 → 【事件】`INTERACTION_CREATE`（`client.on(InteractionCreateEvent.class, ...)`）→ 【接口】`interaction().respondSuccess(id)` 回执 |
| 频道/子频道消息、私信 | R6 | `client.channel()` |
| 撤回消息 | R7 | `recall(...)`，仅 2 分钟内 |
| 高并发发送（不阻塞监听线程） | R8 | `*Async` 方法返回 CompletableFuture |
| Spring Boot 项目接入 | R9 | starter：`qqbot.*` 配置 + EventListener Bean |
| 错误处理 / 审核 / 限流 | R10 | `ApiException.isPendingAudit()/isRateLimited()` |
| 官方无事件体规格的事件（频道成员/表态/论坛/语音） | R11 | `client.onRaw(事件名, consumer)` 或 `on(GenericEvent.class, e -> e.getRaw())` |
| 机器人详情/频道列表/群内状态/分享链接 | 见 api-reference「BotApi」 | `client.bot()`（群内状态接口仅白名单机器人可用） |
| 群信息/成员管理/黑名单/禁言/入群审批 | R12 | `client.groupManage()`（群信息与成员接口仅白名单可用） |
| 群成员加入/退出、入群申请事件 | R13 | `Intents.GROUP_MEMBER_EVENT`（需平台开通；申请事件仅群管理员机器人可收） |
| 单聊流式（打字机）输出 | R14 | `c2c().sendStream(userOpenid, StreamMessageRequest...)`，首片响应的 id 是 stream_msg_id |
| 聊天窗口菜单 / 指令面板 | R15 | `client.menu().setMenu(...)`、`client.panel().createPanel(...)` |
| 建分组 / 增删改子频道 | R16 | `client.channelManage()`（需频道管理员权限） |
| 机器人进/出频道、子频道增删事件 | R16 末段 | `GuildCreateEvent`/`GuildDeleteEvent`、`ChannelCreateEvent` 等（`Intents.GUILDS`） |
| 用户删好友、单聊主动消息开关事件 | 见 api-reference 事件表 | `FriendDelEvent`、`C2CMsgReceiveEvent`/`C2CMsgRejectEvent` |
| 查用户信息（含是否接受主动推送） | 见 api-reference「C2CApi」 | `c2c().getUserInfo(userOpenid)`，无好友关系报 40184001 |

## 关键平台规则速记（写代码前先对照）

### 接口侧（你主动调用：频控、窗口、权限）

| 规则 | 数值 |
|---|---|
| 群聊被动回复窗口 | 用户消息后 5 分钟，同一窗口最多 5 次 |
| 单聊被动回复窗口 | 60 分钟，最多 4 次 |
| 撤回时限 | 发送后 2 分钟内 |
| 群/单聊发消息 QPS（平台侧） | 100；撤回 10；素材上传 50；互动回执 50 |
| 频道被动回复 | msg_id 5 分钟内有效，不需要 msg_seq |
| 二进制直传上限 | 5MB，超过自动分片（uploadChunked/uploadFile） |
| 群信息/群成员接口 | 白名单开放，未开通报 err_code=11253 |
| 群管理批量上限 | 移除/黑名单/禁言 ≤20 成员；禁言最长 30 天；策略 ≤20 个 |
| 流式消息 | 仅单聊，POST /v2/users/{openid}/stream_messages，50 QPS；input_state 1=生成中 10=结束，index 从 0 递增，replace 模式改动已下发前缀报 40007 |
| 自定义菜单 | GET 30 QPM / PUT 5 QPM（全量覆盖），仅单聊全局生效；一级≤10 项、名称≤10 字符（中文算 2）、二级≤5 项、链接必须 https |
| 指令面板 | 列表/详情 30 QPM，创建/修改/删除 10 QPM，关联对象 60 QPM；机器人最多 20 个面板、面板最多 20 个元素、关联对象单次≤20 |
| 频道管理 | 6 个端点均 50 QPS，需管理员权限；创建分组 position 必须 ≥2 |
| 单聊用户信息 | GET /v2/users/{openid}/info，10 QPS |
| file_type | 1=图片 2=视频 3=语音 4=文件 |
| msg_type | 0=文本 2=markdown 3=图片 7=小程序卡片（互动键盘走 keyboard 字段） |
| 客户端全局限流 | SDK 默认 20 QPS，`requestsPerSecond(n)` 可调 |

### 事件侧（平台推送：靠 Intent 订阅，无频控概念）

| 规则 | 说明 |
|---|---|
| 群聊收消息条件 | 被 @ 推 `GROUP_AT_MESSAGE_CREATE`；平台开启「接收所有消息」后每条群消息推 `GROUP_MESSAGE_CREATE`（两者字段一致，Intent 均为 `GROUP_AND_C2C_EVENT`） |
| 群成员加入/退出、入群申请 | Intent `GROUP_MEMBER_EVENT`(1<<24)，需平台开通；`GROUP_JOIN_REQUEST` 仅机器人为该群管理员时可收 |
| 消息审核 | Intent 是 `MESSAGE_AUDIT`(1<<27)，**事件名只有 `MESSAGE_AUDIT_PASS` / `MESSAGE_AUDIT_REJECT`**，SDK 都分发成 `MessageAuditedEvent`，用 `isPass()` 分流 |
| 菜单开关 / 面板指令点击 | **不产生 `INTERACTION_CREATE`**：开关切换后消息 ext 带 `<switch_id>=1`，面板 command 把名称填入输入框；只有消息内 Keyboard 按钮需要调回执接口 |
| 官方无事件体规格的事件 | `GUILD_MEMBER_*`（3）、`MESSAGE_REACTION_*`（2）、`FORUM_*`（8）、`AUDIO_*`（4）共 17 个，官方只列名字没有字段表 → 用 `client.onRaw("事件名", d -> ...)` 或 `on(GenericEvent.class, e -> e.getRaw())`，**不要凭记忆写取值字段** |
| 事件外层 ID 与接口衔接 | 被动回复优先用事件外层 `id`（`getEventId()`，作为 `event_id`）；消息体里的 `id` 是 `msg_id`，两者用途不同 |

## 新手问题排查顺序（收不到/回不了消息）

1. 开放平台**沙箱配置**加了测试号/测试群没有（最高频原因）；
2. `intents` 是否订阅了对应事件（群+单聊=GROUP_AND_C2C_EVENT，群成员加入/退出/入群申请=GROUP_MEMBER_EVENT，按钮=INTERACTION，频道=GUILD_MESSAGES 等）；
3. 日志里有没有 READY 事件（没有=没连上网关，查凭证/网络）；
4. 回复报 401 不用管（SDK 自动刷新重试）；网关被踢看关闭码：4013/4014=意图问题（改 intents）、4914/4915=下架/封禁（SDK 停止重连）、4008/4009=SDK 自动 RESUME，详见 references/recipes.md R10 与官方错误码表；
5. 群里只有 @ 机器人才推送 GROUP_AT_MESSAGE_CREATE；平台开启「接收所有消息」后每条群消息都会推送 GROUP_MESSAGE_CREATE（字段一致）。

## Resources

- `references/api-reference.md`：全部公开类与方法签名（从 SDK 1.1.0 源码提取）、Intents 位值表、QQBotConfig 默认值、Starter 配置键
- `references/recipes.md`：R1~R16 完整可运行代码模板