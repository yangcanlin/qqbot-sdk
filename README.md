# QQ 开放平台机器人 Java SDK（V2）

> ## 📌 主仓库在 Gitee
>
> **本项目的唯一主仓库是 [Gitee：meilinkj/qqbot-sdk](https://gitee.com/meilinkj/qqbot-sdk)**，代码更新、Issue 反馈、Pull Request 请全部前往 Gitee：
>
> - 🐛 **Bug 反馈 / 功能建议：[Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)**（已配置表单模板，请按模板填写）
> - 🔀 **代码贡献：[Gitee Pull Requests](https://gitee.com/meilinkj/qqbot-sdk/pulls)**（流程见 [CONTRIBUTING.md](CONTRIBUTING.md)）
> - 🪞 GitHub（[yangcanlin/qqbot-sdk](https://github.com/yangcanlin/qqbot-sdk)）仅为**自动同步镜像**，不处理任何反馈与 PR
>
> 如果你正在 GitHub 上阅读本页面，建议切换到 [Gitee 主仓库](https://gitee.com/meilinkj/qqbot-sdk) 获取最新内容。

严格对齐 [QQ 机器人官方开发文档（V2）](https://bot.q.qq.com/wiki/develop/api-v2/) 的 Java SDK，覆盖 **QQ 群聊（V2）**、**单聊 C2C（V2）** 与 **QQ 频道（Guild）** 三类机器人接口。**不兼容任何非官方协议**（OneBot / Simbot / NapCat 等），所有接口路径、字段、opcode、intents、错误码均以官方文档为唯一依据。

**核心特性**

- Java 17+，轻量依赖：Jackson + OkHttp + Java-WebSocket + SLF4J
- 自动 AccessToken 管理（60 秒过期窗口自动刷新、401 自动重取重试）
- WebSocket 网关：心跳、指数退避重连、会话 Resume、seq 管理
- 同步 / 异步（`CompletableFuture`）调用，请求重试、客户端限流、平台错误码解析
- 类型化事件监听模型，未建模事件以原始 JSON 兜底，可扩展
- 富媒体上传：URL / 二进制直传（≤5MB）/ 分片上传合并全流程
- Spring Boot 3 Starter 自动装配
- 53 项单元测试，Apache License 2.0

## 目录

- [环境要求](#环境要求)
- [安装](#安装)
- [获取机器人凭证](#获取机器人凭证)
- [快速开始（完整可运行示例）](#快速开始完整可运行示例)
- [配置项详解](#配置项详解)
- [Intents 事件订阅](#intents-事件订阅)
- [事件监听](#事件监听)
- [群聊（V2）接口](#群聊v2接口)
- [单聊 C2C（V2）接口](#单聊-c2cv2接口)
- [富媒体上传](#富媒体上传)
- [Markdown / 卡片 / 键盘](#markdown--卡片--键盘)
- [频道（Guild）接口](#频道guild接口)
- [互动事件响应](#互动事件响应)
- [异步调用](#异步调用)
- [Spring Boot 3 集成](#spring-boot-3-集成)
- [错误处理与调试](#错误处理与调试)
- [网关行为说明](#网关行为说明)
- [运行官方示例 Demo](#运行官方示例-demo)
- [常见问题 FAQ](#常见问题-faq)
- [官方文档对照](#官方文档对照)
- [构建与发布](#构建与发布)
- [许可证](#许可证)

## 环境要求

| 项 | 要求 |
|---|---|
| JDK | 17 及以上 |
| 构建工具 | Maven 3.6+（或 Gradle） |
| Spring Boot（可选） | 3.x（仅使用 Starter 时需要） |
| 日志实现 | SLF4J 绑定任选（Logback / log4j2 / slf4j-simple） |
| 网络 | 可访问 `https://api.bot.qq.com` 与 QQ WebSocket 网关 |

## 安装

Maven（普通 Java 项目，引入 `qqbot-sdk-api` 即可，core 会传递引入）：

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Spring Boot 项目改用 Starter：

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

Gradle：

```groovy
implementation 'cn.meilinkj.qqbot:qqbot-sdk-api:1.0.0'
// 或 Spring Boot：
// implementation 'cn.meilinkj.qqbot:qqbot-sdk-spring-boot-starter:1.0.0'
```

模块说明：

| 模块 | 说明 |
|---|---|
| `qqbot-sdk-core` | 协议核心：鉴权、HTTP、网关、事件分发、官方数据模型 |
| `qqbot-sdk-api` | 业务接口：群聊 / 单聊 / 频道 / 互动，`QQBotClient` 统一入口 |
| `qqbot-sdk-spring-boot-starter` | Spring Boot 3 自动装配（可选） |
| `qqbot-sdk-example` | 最小可运行 Demo：回声机器人（不发布到 Maven Central，源码仓库内运行） |

## 获取机器人凭证

1. 打开 [QQ 开放平台](https://q.qq.com)，登录后进入「机器人」控制台，创建机器人。
2. 在机器人的「开发设置」页获取 **AppID** 与 **AppSecret**（AppSecret 即 clientSecret）。
3. 按官方文档完成「沙箱配置」：把测试 QQ 号 / 测试群加入沙箱，否则收不到消息事件。
4. **凭证安全**：请通过环境变量或配置中心注入，**严禁硬编码到代码或提交到仓库**。

```bash
export QQBOT_APP_ID=你的AppID
export QQBOT_APP_SECRET=你的AppSecret
```

> SDK 内部使用 AppID + AppSecret 自动换取 AccessToken（官方接口 `POST /app/getAppAccessToken`），
> 并在过期前 60 秒自动刷新，开发者无需手动管理 Token。

## 快速开始（完整可运行示例）

一个最小的「回声机器人」：群里 @ 机器人或单聊发消息时，机器人原样回复。

```java
import cn.meilinkj.qqbot.api.QQBotClient;
import cn.meilinkj.qqbot.core.QQBotConfig;
import cn.meilinkj.qqbot.core.gateway.Intents;
import cn.meilinkj.qqbot.core.event.lifecycle.ReadyEvent;
import cn.meilinkj.qqbot.core.event.message.C2CMessageCreateEvent;
import cn.meilinkj.qqbot.core.event.message.GroupAtMessageCreateEvent;

public class EchoBot {

    public static void main(String[] args) throws Exception {
        // 1. 从环境变量读取凭证（不要硬编码）
        String appId = System.getenv("QQBOT_APP_ID");
        String appSecret = System.getenv("QQBOT_APP_SECRET");

        // 2. 构建配置：订阅群聊与单聊事件（1<<25）
        QQBotConfig config = QQBotConfig.builder(appId, appSecret)
                .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT))
                .build();

        // 3. 创建客户端（AutoCloseable，退出时自动释放资源）
        try (QQBotClient client = new QQBotClient(config)) {

            client.on(ReadyEvent.class, e ->
                    System.out.println("机器人已上线，session=" + e.getSessionId()));

            // 群 @ 消息被动回复（msg_seq 自动递增，无需手动管理）
            client.on(GroupAtMessageCreateEvent.class, e ->
                    client.group().replyText(e.getGroupOpenid(), e.getId(),
                            "回声: " + e.getContent()));

            // 单聊消息被动回复
            client.on(C2CMessageCreateEvent.class, e ->
                    client.c2c().replyText(e.getAuthor().getUserOpenid(), e.getId(),
                            "回声: " + e.getContent()));

            // 4. 连接网关（自动鉴权、心跳、断线重连、会话恢复）
            client.start();

            // 保持主线程存活
            Thread.currentThread().join();
        }
    }
}
```

运行前确保已 `export QQBOT_APP_ID / QQBOT_APP_SECRET`，且测试群 / QQ 号已加入沙箱。
在群里 @ 机器人发一句话，或单聊给机器人发消息，即可收到回声回复。

## 配置项详解

`QQBotConfig.builder(appId, appSecret)` 支持的全部配置（括号内为默认值）：

| 方法 | 默认值 | 说明 |
|---|---|---|
| `baseUrl(String)` | `https://api.bot.qq.com` | 官方统一接口域名，一般无需修改 |
| `intents(long)` | `GROUP_AND_C2C_EVENT`（33554432） | 事件订阅掩码，用 `Intents.of(...)` 组合 |
| `shard(int shardId, int numShards)` | `[0, 1]` | 分片配置，大型机器人按官方 Shard 机制分片 |
| `maxRetries(int)` | `3` | HTTP 请求失败最大重试次数（429 / 5xx / 网络错误） |
| `retryBaseDelayMs(long)` | `500` | 重试退避基础延迟（毫秒，指数递增） |
| `retryMaxDelayMs(long)` | `30000` | 重试退避上限（毫秒） |
| `requestsPerSecond(int)` | `20` | 客户端全局 QPS 限流；官方各接口另有独立频控 |
| `connectTimeoutMs(int)` | `10000` | HTTP 连接超时 |
| `readTimeoutMs(int)` | `15000` | HTTP 读超时 |
| `writeTimeoutMs(int)` | `15000` | HTTP 写超时 |
| `reconnectBaseDelayMs(long)` | `1000` | 网关断线重连基础延迟（指数退避） |
| `reconnectMaxDelayMs(long)` | `60000` | 网关重连延迟上限 |
| `eventThreads(int)` | `4` | 事件分发线程池大小 |

示例：

```java
QQBotConfig config = QQBotConfig.builder(appId, appSecret)
        .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.INTERACTION))
        .requestsPerSecond(10)
        .maxRetries(5)
        .eventThreads(8)
        .build();
```

## Intents 事件订阅

`Intents` 枚举与官方文档完全一致，用 `Intents.of(...)` 按位组合：

| 枚举 | 值 | 覆盖事件 |
|---|---|---|
| `GUILDS` | 1<<0 | 频道 / 子频道增删改 |
| `GUILD_MEMBERS` | 1<<1 | 频道成员增删改 |
| `GUILD_MESSAGES` | 1<<9 | 私域频道消息（MESSAGE_CREATE 等） |
| `GUILD_MESSAGE_REACTIONS` | 1<<10 | 频道表情表态 |
| `DIRECT_MESSAGE` | 1<<12 | 频道私信 |
| `GROUP_AND_C2C_EVENT` | 1<<25 | **QQ 群聊与单聊全部事件**（最常用） |
| `INTERACTION` | 1<<26 | 互动事件（按钮 / 菜单点击） |
| `MESSAGE_AUDIT` | 1<<27 | 消息审核结果 |
| `FORUMS_EVENT` | 1<<28 | 私域论坛事件 |
| `AUDIO_ACTION` | 1<<29 | 音频操作 |
| `PUBLIC_GUILD_MESSAGES` | 1<<30 | 公域频道消息（AT_MESSAGE_CREATE 等） |

```java
// 群聊 + 单聊 + 互动按钮
.intents(Intents.of(Intents.GROUP_AND_C2C_EVENT, Intents.INTERACTION))
```

> 注意：订阅了哪些 Intents 才能收到对应事件；私域 / 公域频道消息事件类型不同，详见官方文档。

## 事件监听

三种注册方式：

```java
// 方式一：Class + Lambda（最常用）
client.on(GroupAtMessageCreateEvent.class, e -> { ... });

// 方式二：实现 EventListener 接口（适合独立类 / Spring Bean）
public class MyListener implements EventListener<GroupAtMessageCreateEvent> {
    @Override
    public void onEvent(GroupAtMessageCreateEvent event) { ... }
}
client.on(new MyListener());

// 方式三：原始 JSON（未建模事件兜底，事件名与官方一致）
client.onRaw("GUILD_MEMBER_ADD", d -> log.info("member add: {}", d));
```

**线程模型**：事件在独立线程池（`eventThreads`，默认 4）中分发，不阻塞网关读线程；
单个监听器抛异常会被隔离并记录日志，不影响其他监听器。**监听器内不要做长时间阻塞操作**，
耗时任务请自行提交到业务线程池。

类型化事件一览（包 `cn.meilinkj.qqbot.core.event`）：

| 事件 | Intent | 事件类 |
|---|---|---|
| READY / RESUMED | 网关生命周期 | `ReadyEvent` / `ResumedEvent` |
| GROUP_AT_MESSAGE_CREATE | GROUP_AND_C2C_EVENT | `GroupAtMessageCreateEvent` |
| GROUP_MESSAGE_CREATE | GROUP_AND_C2C_EVENT | `GroupMessageCreateEvent` |
| C2C_MESSAGE_CREATE | GROUP_AND_C2C_EVENT | `C2CMessageCreateEvent` |
| GROUP_ADD_ROBOT / GROUP_DEL_ROBOT | GROUP_AND_C2C_EVENT | `GroupAddRobotEvent` / `GroupDelRobotEvent` |
| GROUP_MSG_RECEIVE / GROUP_MSG_REJECT | GROUP_AND_C2C_EVENT | `GroupMsgReceiveEvent` / `GroupMsgRejectEvent` |
| FRIEND_ADD | GROUP_AND_C2C_EVENT | `FriendAddEvent` |
| INTERACTION_CREATE | INTERACTION | `InteractionCreateEvent` |
| MESSAGE_CREATE / AT_MESSAGE_CREATE / DIRECT_MESSAGE_CREATE | 频道消息 | `ChannelMessageEvent` |
| MESSAGE_DELETE / PUBLIC_MESSAGE_DELETE / DIRECT_MESSAGE_DELETE | 频道消息 | `MessageDeleteEvent` |
| MESSAGE_AUDIT_PASS / MESSAGE_AUDIT_REJECT | MESSAGE_AUDIT | `MessageAuditedEvent` |

其余官方事件（频道成员、表情表态、论坛、音频等）以 `GenericEvent` / `onRaw` 原始 JSON 分发，
事件名与 [官方事件列表](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) 一致。

常用事件字段：

```java
client.on(GroupAtMessageCreateEvent.class, e -> {
    e.getId();            // 消息 ID（被动回复时作为 msg_id 回传）
    e.getGroupOpenid();   // 群 OpenID（发消息的目标地址）
    e.getContent();       // 文本内容（@ 部分已按官方规则处理）
    e.getAuthor();        // 发送者：getMemberOpenid()/getMemberNickname() 等
    e.getTimestamp();     // 时间戳
    e.getAttachments();   // 附件列表
});

client.on(C2CMessageCreateEvent.class, e -> {
    e.getId();
    e.getAuthor().getUserOpenid();  // 用户 OpenID（单聊发消息的目标地址）
    e.getContent();
});

client.on(GroupAddRobotEvent.class, e ->
        log.info("机器人被拉入群 {}", e.getGroupOpenid()));

client.on(FriendAddEvent.class, e ->
        log.info("新增好友 {}", e.getOpenid()));
```

> **OpenID 说明**：群聊 / 单聊 V2 接口使用 `group_openid` / `user_openid`（来自事件），
> 不是 QQ 号也不是频道 ID；同一用户在不同群的 openid 不同，跨群身份可用 `union_openid` 关联。

## 群聊（V2）接口

入口：`client.group()`。

### 被动回复与主动消息

```java
// 被动回复：必须携带收到消息的 msg_id，SDK 自动分配 msg_seq
client.group().replyText(groupOpenid, msgId, "回复内容");

// 主动消息（不引用任何消息，受官方主动消息频控限制）
client.group().sendText(groupOpenid, "主动推送内容");

// 完整请求对象方式
SendMessageResponse resp = client.group().sendMessage(groupOpenid,
        MessageRequest.text("hello").replyTo(msgId, 1));
resp.getId();         // 发出的消息 ID（可用于撤回）
resp.getTimestamp();
```

**官方被动回复规则**（务必遵守，否则接口报错）：

- 群聊：收到消息后 **5 分钟内**回复，同一条消息最多回复 **5 次**；
- `msg_id` + `msg_seq` 组合用于去重：同一 `msg_id` 下相同 `msg_seq` 的消息会被平台去重。
  `replyText` 内部按 `msg_id` 自动递增 `msg_seq`，多次回复同一条消息直接连续调用即可；
- 超过时限 / 次数后只能走主动消息。

### 撤回

```java
// 撤回机器人自己发出的消息（官方限制：发出后 2 分钟内）
client.group().recall(groupOpenid, messageId);
client.group().recallAsync(groupOpenid, messageId);   // 异步
```

### 发图片（URL 方式）

```java
client.group().sendImageByUrl(groupOpenid, "https://example.com/a.png");
```

更多富媒体（视频 / 语音 / 文件、本地上传）见 [富媒体上传](#富媒体上传)。

## 单聊 C2C（V2）接口

入口：`client.c2c()`，方法与群聊对称，目标参数换成 `userOpenid`：

```java
client.c2c().replyText(userOpenid, msgId, "回复内容");  // 被动回复
client.c2c().sendText(userOpenid, "主动消息");
client.c2c().sendImageByUrl(userOpenid, "https://example.com/a.png");
client.c2c().recall(userOpenid, messageId);
```

**官方被动回复规则**：单聊为 **60 分钟内**、同一条消息最多 **4 次**。

单聊独有能力：

```java
// 「输入中」状态提示（1~60 秒），提升体验
client.c2c().sendInputNotify(userOpenid, 15);

// 唤醒消息（is_wakeup=true，与 msg_id/event_id 互斥，受官方唤醒频控）
client.c2c().sendWakeupText(userOpenid, "回来看看新功能");
```

## 富媒体上传

群聊与单聊均支持，`file_type`：**1 图片、2 视频、3 语音、4 文件**（各类型开放范围以官方文档为准）。

### 方式一：URL 上传（平台拉取）

```java
MediaUploadResponse media = client.group().uploadMedia(groupOpenid,
        MediaUploadRequest.ofUrl(1, "https://example.com/a.png"));
```

### 方式二：本地文件（推荐，自动选择直传 / 分片）

```java
// ≤5MB 自动二进制直传；>5MB 自动走分片上传（prepare → 逐片 PUT → finish → merge）
MediaUploadResponse media = client.group().uploadFile(groupOpenid, 2, Path.of("video.mp4"));
```

也可显式指定：

```java
client.group().uploadBinary(groupOpenid, 1, "a.png", bytes);    // 直传，≤5MB
client.group().uploadChunked(groupOpenid, 2, "video.mp4", bytes); // 分片，任意大小
```

### 发送富媒体消息

上传得到的 `file_info` 用于发消息（`msg_type=7`）：

```java
MediaUploadResponse media = client.group().uploadFile(groupOpenid, 1, Path.of("a.png"));
client.group().sendMessage(groupOpenid, MessageRequest.media(media.getFileInfo()));
```

> 注意：`file_info` 有有效期（`media.getTtl()`），过期需重新上传；
> 富媒体消息同样支持被动回复（`MessageRequest.media(...).replyTo(msgId, seq)`）。
> 单聊接口完全对称：`client.c2c().uploadFile(...)` 等。

## Markdown / 卡片 / 键盘

### Markdown（需官方模板权限）

```java
// 原生 markdown 内容
client.group().sendMessage(groupOpenid,
        MessageRequest.markdown(MessageMarkdown.ofContent("# 标题\n内容")));

// 模板 markdown（template_id 为官方申请的原生模板）
client.group().sendMessage(groupOpenid, MessageRequest.markdown(
        MessageMarkdown.ofContent(null).setTemplateId(10001)));
```

### 图文卡片

```java
client.group().sendMessage(groupOpenid, MessageRequest.card(
        MessageCard.tuwen("标题", "描述", "https://example.com", "https://example.com/a.png")));
```

### 消息键盘（按钮）

```java
Keyboard.Button button = new Keyboard.Button()
        .setId("btn_hello")
        .setRenderData(new Keyboard.RenderData().setLabel("点我").setStyle(1))
        .setAction(new Keyboard.Action()
                .setType(1)                    // 1 = 回调交互按钮
                .setData("hello")              // 点击后回传的数据
                .setPermission(Keyboard.Permission.all()));

client.group().sendMessage(groupOpenid, MessageRequest.text("请选择")
        .setKeyboard(Keyboard.ofRows(List.of(Keyboard.Row.of(button)))));

// 也可使用在官方平台申请的模板键盘
// .setKeyboard(Keyboard.ofTemplate("模板ID"))
```

用户点击按钮后会收到 `INTERACTION_CREATE` 事件（需订阅 `Intents.INTERACTION`），
**必须调用互动响应接口回执**，见 [互动事件响应](#互动事件响应)。

## 频道（Guild）接口

入口：`client.channel()`。频道接口需订阅对应频道 Intents，且**部分能力要求 WebSocket 在线**。

```java
// 发送子频道消息（被动回复携带 msg_id）
Message msg = client.channel().sendMessage(channelId,
        ChannelMessageRequest.text("hello").replyTo(msgId));

// 纯图片 / embed / ark / markdown 通过请求对象设置
client.channel().sendMessage(channelId, ChannelMessageRequest.image("https://example.com/a.png"));
client.channel().sendMessage(channelId,
        ChannelMessageRequest.text("").setEmbed(embed));

// 撤回（hideTip=true 不显示撤回提示）
client.channel().recall(channelId, messageId, false);

// 频道私信：先创建 DMS 会话，再向 DMS 的 guild_id 发消息
Dms dms = client.channel().createDms(recipientId, sourceGuildId);
client.channel().sendDms(dms.getGuildId(), ChannelMessageRequest.text("私信内容"));
client.channel().recallDms(dms.getGuildId(), messageId, false);

// 异步
client.channel().sendMessageAsync(channelId, ChannelMessageRequest.text("hi"));
```

频道消息事件（`ChannelMessageEvent`）与群聊 / 单聊事件模型不同，注意区分三类场景。

## 互动事件响应

用户点击消息键盘按钮 / 菜单后，平台推送 `INTERACTION_CREATE`。
**必须回应一次**（官方接口 `PUT /interactions/{interaction_id}`），否则用户客户端一直显示 loading。

```java
client.on(InteractionCreateEvent.class, e -> {
    // e.getData().getResolved().getButtonData() 可取回按钮 action.data
    client.interaction().respondSuccess(e.getId());   // 等价于 respond(id, 0)
});
```

响应码（`InteractionApi.CODE_*` 常量）：

| code | 含义 |
|---|---|
| 0 | 成功 |
| 1 | 失败 |
| 2 | 操作频繁 |
| 3 | 重复操作 |
| 4 | 无权限 |
| 5 | 仅管理员可操作 |

```java
client.interaction().respond(e.getId(), InteractionApi.CODE_SUCCESS);
client.interaction().respondAsync(e.getId(), 0);   // 异步
```

> 注意：interaction_id 直接使用事件的 `e.getId()`（不带前缀）；每个互动只能回应一次；官方频控 50 QPS。

## 异步调用

所有发送 / 撤回 / 互动接口均有 `*Async` 变体，返回 `CompletableFuture`：

```java
client.group().sendMessageAsync(groupOpenid, MessageRequest.text("hi"))
        .thenAccept(resp -> log.info("sent {}", resp.getId()))
        .exceptionally(ex -> {
            log.error("发送失败", ex);
            return null;
        });
```

异步调用共享同一套鉴权、限流与重试机制；失败时 future 以 `ApiException` 完成。

## Spring Boot 3 集成

引入 `qqbot-sdk-spring-boot-starter` 后，只需配置 + 写监听器 Bean。

### 1. 配置（application.yml）

```yaml
qqbot:
  app-id: ${QQBOT_APP_ID}
  app-secret: ${QQBOT_APP_SECRET}
  # 以下为可选配置（括号内默认值）
  # base-url: https://api.bot.qq.com
  # intents: 33554432        # GROUP_AND_C2C_EVENT；组合值 = 各 Intents 值相加
  # shard-id: 0
  # num-shards: 1
  # max-retries: 3
  # requests-per-second: 20
  # event-threads: 4
  # auto-start: true         # 应用就绪后自动连接网关
```

| 配置项 | 默认值 | 说明 |
|---|---|---|
| `qqbot.app-id` | 无（**必填**，缺失时不装配） | 机器人 AppID |
| `qqbot.app-secret` | 无（必填） | 机器人 AppSecret |
| `qqbot.base-url` | 官方域名 | 接口地址 |
| `qqbot.intents` | 33554432 | 事件订阅掩码 |
| `qqbot.shard-id` / `qqbot.num-shards` | 0 / 1 | 分片 |
| `qqbot.max-retries` | 3 | 请求重试次数 |
| `qqbot.requests-per-second` | 20 | 客户端限流 |
| `qqbot.event-threads` | 4 | 事件线程数 |
| `qqbot.auto-start` | true | false 时可注入 `QQBotClient` 后手动 `start()` |

### 2. 编写监听器 Bean

容器内**所有 `EventListener` Bean 会被自动注册**：

```java
@Component
public class GroupEchoListener implements EventListener<GroupAtMessageCreateEvent> {

    private final QQBotClient client;

    public GroupEchoListener(QQBotClient client) {
        this.client = client;   // Starter 自动装配的客户端，可直接注入
    }

    @Override
    public void onEvent(GroupAtMessageCreateEvent event) {
        client.group().replyText(event.getGroupOpenid(), event.getId(),
                "回声: " + event.getContent());
    }
}
```

应用启动完成后自动连接网关；容器关闭时自动 `shutdown()` 释放资源。
也可以直接注入 `QQBotClient` 调用任意接口（主动推送、定时任务等场景）。

## 错误处理与调试

### 异常类型

接口调用失败抛出 `ApiException`（运行时异常），携带完整平台错误信息：

```java
try {
    client.group().sendText(groupOpenid, "hello");
} catch (ApiException e) {
    e.getHttpStatus();  // HTTP 状态码
    e.getErrCode();     // 平台错误码（官方错误码表）
    e.getMessage();     // 平台错误描述
    e.getTraceId();     // trace_id：向 QQ 开放平台提工单排查的关键凭据
    e.getRawBody();     // 原始响应体
}
```

### SDK 自动处理的情况

| 情况 | SDK 行为 |
|---|---|
| Token 过期 / 401 | 自动刷新 AccessToken 并重试一次 |
| 429 限流 / 5xx / 网络错误 | 指数退避重试（`maxRetries` 次，默认 3） |
| 201 / 202 异步成功 | 视为成功返回；若消息进入审核，结果通过 `MESSAGE_AUDIT_PASS/REJECT` 事件推送（需订阅 `Intents.MESSAGE_AUDIT`） |
| 客户端 QPS 超限 | 本地令牌桶排队等待（`requestsPerSecond`） |

### 日志

SDK 使用 SLF4J 输出日志（鉴权刷新、网关连接 / 重连、请求重试等）。接入你的日志实现后，
调试时可把 `cn.meilinkj.qqbot` 包日志级别调为 `DEBUG`。

### 向平台反馈问题

平台侧错误（非 SDK 问题）请携带 `trace_id`（响应头 `X-Tps-trace-ID` 或响应体 `trace_id`）
通过 QQ 开放平台渠道反馈；SDK 自身问题请到 [Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues) 提交。

## 网关行为说明

`client.start()` 之后 SDK 自动完成（协议细节见官方文档）：

1. 获取 WebSocket 网关地址（`GET /gateway`）并连接；
2. 收到 Hello（op 10）后按心跳周期发送心跳（op 1，携带最新 seq）；
3. 发送 Identify（op 2）：`QQBot {token}` + intents + shard；
4. 收到 READY 后进入正常事件分发；
5. **断线重连**：指数退避（默认 1s 起、上限 60s）；
6. **会话恢复**：关闭码 4008 / 4009 时优先 Resume（op 6，携带 session_id + seq），
   其余可重连错误重新 Identify；4914 / 4915 等致命错误停止重连并记录日志；
7. seq 全程自动管理，业务代码无需关心。

可用 `client.gateway().isConnected()` / `getSessionId()` / `getLastSeq()` 观察状态。

## 运行官方示例 Demo

仓库内 `qqbot-sdk-example` 提供回声机器人 Demo（群 @ + 单聊回声、入群 / 好友日志）：

```bash
git clone https://gitee.com/meilinkj/qqbot-sdk.git
cd qqbot-sdk
export QQBOT_APP_ID=你的AppID
export QQBOT_APP_SECRET=你的AppSecret
mvn -pl qqbot-sdk-example -am compile exec:java
```

## 常见问题 FAQ

**Q：连上了网关但收不到消息事件？**
依次检查：① Intents 是否订阅了对应事件（群聊 / 单聊是 `GROUP_AND_C2C_EVENT`）；
② 测试群 / QQ 号是否在开放平台沙箱配置内；③ 群里是否 @ 了机器人（`GROUP_AT_MESSAGE_CREATE` 只推送 @ 消息）；
④ 机器人是否已在该群 / 已被用户添加。

**Q：被动回复报错 / 发不出去？**
检查是否超过官方时限与次数（群 5 分钟 5 次、单聊 60 分钟 4 次）；`msg_id` 必须来自收到的事件；
不要对同一 `msg_id` 手动重复相同 `msg_seq`（`replyText` 已自动递增）。

**Q：报 401 / token 相关错误？**
SDK 会自动刷新重试；若持续失败，请核对 AppID / AppSecret 是否正确、机器人是否被停用。

**Q：主动消息被限制？**
主动消息有官方频控与场景限制（与被动回复不同），请对照官方文档的主动消息规则；必要时改用被动回复或唤醒消息（单聊）。

**Q：富媒体发送失败？**
确认 `file_type` 与内容匹配、`file_info` 未过期（`ttl`）、URL 可被平台公网拉取；大文件请用 `uploadFile`（自动分片）。

**Q：按钮点击后一直 loading？**
未回应互动事件。订阅 `Intents.INTERACTION` 并在 `InteractionCreateEvent` 中调用 `respondSuccess`。

**Q：支持 OneBot / NapCat 等非官方协议吗？**
不支持。本 SDK 严格对齐 QQ 开放平台官方 V2 文档，这是项目核心原则。

## 官方文档对照

| 能力 | 官方文档 |
|---|---|
| 获取调用凭证 | https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/api-use.html |
| 网关 / Payload / Intents / 错误码 | https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html |
| 发送群聊消息 | https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_messages.post.html |
| 发送单聊消息 | https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_users_user_openid_messages.post.html |
| 群聊富媒体上传 | https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_openid_files.post.html |
| 分片上传预处理 | https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/v2_groups_group_id_upload_prepare.post.html |
| 互动事件响应 | https://bot.q.qq.com/wiki/develop/api-v2/autogen/api/interactions_interaction_id.put.html |
| 频道消息 | https://bot.q.qq.com/wiki/develop/api-v2/server-inter/channel/message/send.html |
| 错误与调试 | https://bot.q.qq.com/wiki/develop/api-v2/openapi/error/error.html |

代码中每个接口、枚举与关键方法的 Javadoc 均附官方文档锚点链接。

## 构建与发布

```bash
git clone https://gitee.com/meilinkj/qqbot-sdk.git
cd qqbot-sdk
mvn clean install          # 编译 + 全部单元测试
mvn clean deploy -Prelease # 一键发布（source/javadoc/gpg + Maven Central，维护者）
```

`release` profile 需在 `~/.m2/settings.xml` 配置 `central` server 凭证与 GPG 密钥，参见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

[Apache License 2.0](LICENSE)

---

**再次提醒：反馈请前往 [Gitee 主仓库 Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)，GitHub 镜像不处理反馈。**