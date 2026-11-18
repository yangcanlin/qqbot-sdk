---
name: qqbot-sdk
description: 使用 qqbot-sdk（Maven cn.meilinkj.qqbot）进行 QQ 开放平台机器人 Java 开发：群聊@回复、单聊C2C、频道消息、富媒体图片文件、按钮互动、消息撤回、Spring Boot 接入。当项目依赖 qqbot-sdk / QQBotClient / QQBotConfig，或用户要求开发 QQ 机器人功能、排查收不到消息/回复失败问题时使用。仅支持官方 api-v2 协议，禁止 OneBot/Simbot/NapCat 等非官方协议。
---

# qqbot-sdk：QQ 机器人 Java 开发

## Overview

qqbot-sdk 是严格对齐 QQ 机器人官方开发文档（V2）的 Java SDK，覆盖群聊、单聊（C2C）、频道三类场景。本 Skill 提供准确的方法签名、任务模板与平台规则，避免凭记忆猜测 API 造成幻觉。

- 构件：`cn.meilinkj.qqbot:qqbot-sdk-api:1.0.0`（Java 17+；core 会传递引入）
- Spring Boot 3.x：改用 `qqbot-sdk-spring-boot-starter`
- 主仓库：<https://gitee.com/meilinkj/qqbot-sdk>；详细文档：<https://gitee.com/meilinkj/qqbot-sdk/wikis>
- 官方协议文档（唯一依据）：<https://bot.q.qq.com/wiki/develop/api-v2/>

## 硬约束（生成代码必须遵守）

1. 只使用本 Skill 或源码中真实存在的类与方法，不确定时先查 `references/api-reference.md`，禁止臆造签名；
2. 协议、字段、频控以官方 api-v2 文档为准，禁止生成 OneBot / Simbot / NapCat 等非官方协议代码；
3. appId/appSecret 一律从环境变量或配置注入，严禁硬编码进代码或写入 application.yml 明文；
4. 统一接口域名 `https://api.bot.qq.com`（SDK 默认值，不要改）；AccessToken 由 SDK 自动管理，不要手写换票逻辑；
5. SDK 用 SLF4J，示例项目建议带 logback-classic 或 slf4j-simple。

## 最小骨架（所有功能都从这里起步）

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.0.0</version>
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

按需求读 `references/recipes.md` 对应模板（编号一一对应），方法签名速查读 `references/api-reference.md`：

| 用户想做的事 | 用哪个模板 | 关键入口 |
|---|---|---|
| 群聊被@后回复 | R1 | `client.group().replyText(groupOpenid, msgId, text)` |
| 主动往群里推消息/公告 | R1 主动消息 | `group().sendText / sendMessage`（需消息模板） |
| 私聊问答机器人 | R2 | `client.c2c()`，含输入中提示、唤醒消息 |
| 发图片/视频/语音/文件 | R3 | `sendImageByUrl` 或 `uploadMedia/uploadBinary/uploadFile` → `MessageRequest.media(fileInfo)` |
| markdown / 图文卡片 | R4 | `MessageRequest.markdown(...) / card(...)` |
| 按钮键盘 + 点击回执 | R5 | `Keyboard.ofRows(...)` + `InteractionCreateEvent` + `interaction().respondSuccess(id)` |
| 频道/子频道消息、私信 | R6 | `client.channel()` |
| 撤回消息 | R7 | `recall(...)`，仅 2 分钟内 |
| 高并发发送（不阻塞监听线程） | R8 | `*Async` 方法返回 CompletableFuture |
| Spring Boot 项目接入 | R9 | starter：`qqbot.*` 配置 + EventListener Bean |
| 错误处理 / 审核 / 限流 | R10 | `ApiException.isPendingAudit()/isRateLimited()` |
| 未建模事件 / 原始 JSON | R11 | `client.onRaw(事件名, consumer)` |

## 关键平台规则速记（写代码前先对照）

| 规则 | 数值 |
|---|---|
| 群聊被动回复窗口 | 用户消息后 5 分钟，同一窗口最多 5 次 |
| 单聊被动回复窗口 | 60 分钟，最多 4 次 |
| 撤回时限 | 发送后 2 分钟内 |
| 群/单聊发消息 QPS（平台侧） | 100；撤回 10；素材上传 50；互动回执 50 |
| 频道被动回复 | msg_id 5 分钟内有效，不需要 msg_seq |
| 二进制直传上限 | 5MB，超过自动分片（uploadChunked/uploadFile） |
| 群聊收消息条件 | 必须 @ 机器人（GROUP_AT_MESSAGE_CREATE） |
| file_type | 1=图片 2=视频 3=语音 4=文件 |
| msg_type | 0=文本 2=markdown 3=图片 7=小程序卡片（互动键盘走 keyboard 字段） |
| 客户端全局限流 | SDK 默认 20 QPS，`requestsPerSecond(n)` 可调 |

## 新手问题排查顺序（收不到/回不了消息）

1. 开放平台**沙箱配置**加了测试号/测试群没有（最高频原因）；
2. `intents` 是否订阅了对应事件（群+单聊=GROUP_AND_C2C_EVENT，按钮=INTERACTION，频道=GUILD_MESSAGES 等）；
3. 日志里有没有 READY 事件（没有=没连上网关，查凭证/网络）；
4. 回复报 401 不用管（SDK 自动刷新重试）；报 4004/4914/4915 见 references/recipes.md R10 与错误码表；
5. 群里发消息必须带 @，普通水群消息官方不推送。

## Resources

- `references/api-reference.md`：全部公开类与方法签名（从 SDK 1.0.0 源码提取）、Intents 位值表、QQBotConfig 默认值、Starter 配置键
- `references/recipes.md`：R1~R11 完整可运行代码模板