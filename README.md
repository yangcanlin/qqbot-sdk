# QQ 开放平台机器人 Java SDK（V2）

严格对齐 [QQ 机器人官方开发文档](https://bot.q.qq.com/wiki/develop/api-v2/) 的 Java SDK，覆盖 **QQ 群聊（V2）**、**单聊 C2C（V2）** 与 **QQ 频道（Guild）** 三类机器人接口。不兼容任何非官方协议（OneBot / Simbot / NapCat 等）。

- Java 17+，轻量依赖：Jackson + OkHttp + Java-WebSocket + SLF4J
- 自动 AccessToken 管理（60 秒过期窗口自动刷新、401 自动重取）
- WebSocket 网关：心跳、指数退避重连、会话 Resume、seq 管理
- 同步 / 异步（`CompletableFuture`）调用，请求重试、客户端限流、平台错误码解析
- 类型化事件监听模型，未建模事件以原始 JSON 兜底，可扩展
- 富媒体上传：URL / 二进制直传 / 分片上传合并全流程
- Spring Boot 3 Starter 自动装配
- Apache License 2.0

## 模块

| 模块 | 说明 |
|---|---|
| `qqbot-sdk-core` | 协议核心：鉴权、HTTP、网关、事件分发、官方数据模型 |
| `qqbot-sdk-api` | 业务接口：群聊 / 单聊 / 频道 / 互动，`QQBotClient` 统一入口 |
| `qqbot-sdk-spring-boot-starter` | Spring Boot 3 自动装配（可选） |
| `qqbot-sdk-example` | 最小可运行 Demo：回声机器人 |

## Maven 坐标

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Spring Boot 项目改用：

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 快速开始

凭证在 [QQ 开放平台](https://q.qq.com) 创建机器人后获取，请通过环境变量注入，**不要硬编码**。

```java
QQBotConfig config = QQBotConfig.builder(appId, appSecret)
        .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT)) // 1<<25，群聊与单聊事件
        .build();

try (QQBotClient client = new QQBotClient(config)) {
    // 群 @ 消息被动回复（msg_seq 自动递增）
    client.on(GroupAtMessageCreateEvent.class, e ->
            client.group().replyText(e.getGroupOpenid(), e.getId(), "回声: " + e.getContent()));

    // 单聊消息被动回复
    client.on(C2CMessageCreateEvent.class, e ->
            client.c2c().replyText(e.getAuthor().getUserOpenid(), e.getId(), "回声: " + e.getContent()));

    client.start(); // 连接网关，自动心跳与重连
    Thread.currentThread().join();
}
```

### 运行最小 Demo

```bash
export QQBOT_APP_ID=你的AppID
export QQBOT_APP_SECRET=你的AppSecret
mvn -pl qqbot-sdk-example -am compile exec:java
```

### Spring Boot

```yaml
qqbot:
  app-id: ${QQBOT_APP_ID}
  app-secret: ${QQBOT_APP_SECRET}
  # intents: 33554432  # 默认 GROUP_AND_C2C_EVENT；组合值见 Intents
```

```java
@Component
public class GroupEchoListener implements EventListener<GroupAtMessageCreateEvent> {

    private final QQBotClient client;

    public GroupEchoListener(QQBotClient client) {
        this.client = client;
    }

    @Override
    public void onEvent(GroupAtMessageCreateEvent event) {
        client.group().replyText(event.getGroupOpenid(), event.getId(), "回声: " + event.getContent());
    }
}
```

容器内所有 `EventListener` Bean 会被自动注册，应用就绪后自动连接网关（`qqbot.auto-start=false` 可关闭）。

## 更多用法

```java
// 主动消息 / 富媒体 / 卡片 / 键盘
client.group().sendText(groupOpenid, "主动消息");
client.group().sendImageByUrl(groupOpenid, "https://example.com/a.png");
client.group().uploadFile(groupOpenid, 2, Path.of("video.mp4")); // ≤5MB 直传，否则自动分片
client.group().sendMessage(groupOpenid, MessageRequest.card(
        MessageCard.tuwen("标题", "描述", "https://example.com", "https://example.com/a.png")));
client.group().sendMessage(groupOpenid, MessageRequest.text("请选择")
        .setKeyboard(Keyboard.ofRows(List.of(Keyboard.Row.of(button)))));

// 撤回（2 分钟内）
client.group().recall(groupOpenid, messageId);

// 单聊独有：输入中状态、互动召回
client.c2c().sendInputNotify(userOpenid, 15);
client.c2c().sendWakeupText(userOpenid, "回来看看");

// 异步
client.group().sendMessageAsync(groupOpenid, MessageRequest.text("hi"))
        .thenAccept(resp -> log.info("sent {}", resp.getId()));

// 频道（需 WebSocket 在线）
client.channel().sendMessage(channelId, ChannelMessageRequest.text("hello").replyTo(msgId));
client.channel().recall(channelId, messageId, false);
Dms dms = client.channel().createDms(recipientId, sourceGuildId);
client.channel().sendDms(dms.getGuildId(), ChannelMessageRequest.text("私信"));

// 互动事件回执（按钮/菜单点击后必须回应，否则客户端一直 loading）
client.on(InteractionCreateEvent.class, e -> client.interaction().respondSuccess(e.getId()));

// 未建模事件：原始 JSON 监听
client.onRaw("GUILD_MEMBER_ADD", d -> log.info("member add: {}", d));
```

## 支持的事件

类型化事件（`cn.meilinkj.qqbot.core.event`）：

| 事件 | Intent | 事件类 |
|---|---|---|
| READY / RESUMED | 网关生命周期 | `ReadyEvent` / `ResumedEvent` |
| GROUP_AT_MESSAGE_CREATE | GROUP_AND_C2C_EVENT (1<<25) | `GroupAtMessageCreateEvent` |
| GROUP_MESSAGE_CREATE | GROUP_AND_C2C_EVENT | `GroupMessageCreateEvent` |
| C2C_MESSAGE_CREATE | GROUP_AND_C2C_EVENT | `C2CMessageCreateEvent` |
| GROUP_ADD_ROBOT / GROUP_DEL_ROBOT | GROUP_AND_C2C_EVENT | `GroupAddRobotEvent` / `GroupDelRobotEvent` |
| GROUP_MSG_RECEIVE / GROUP_MSG_REJECT | GROUP_AND_C2C_EVENT | `GroupMsgReceiveEvent` / `GroupMsgRejectEvent` |
| FRIEND_ADD | GROUP_AND_C2C_EVENT | `FriendAddEvent` |
| INTERACTION_CREATE | INTERACTION (1<<26) | `InteractionCreateEvent` |
| MESSAGE_CREATE / AT_MESSAGE_CREATE / DIRECT_MESSAGE_CREATE | 频道消息 | `ChannelMessageEvent` |
| MESSAGE_DELETE / PUBLIC_MESSAGE_DELETE / DIRECT_MESSAGE_DELETE | 频道消息 | `MessageDeleteEvent` |
| MESSAGE_AUDIT_PASS / MESSAGE_AUDIT_REJECT | MESSAGE_AUDIT (1<<27) | `MessageAuditedEvent` |

其余官方事件（频道成员、表情表态、论坛、音频等）以 `GenericEvent` / `onRaw` 原始 JSON 分发，事件名与 [官方事件列表](https://bot.q.qq.com/wiki/develop/api-v2/dev-prepare/interface-framework/event-emit.html) 一致。

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
mvn clean install          # 编译 + 全部单元测试
mvn clean deploy -Prelease # 一键发布（source/javadoc/gpg + Maven Central）
```

`release` profile 需在 `~/.m2/settings.xml` 配置 `central` server 凭证与 GPG 密钥，参见 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

[Apache License 2.0](LICENSE)