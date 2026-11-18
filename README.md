# QQ 开放平台机器人 Java SDK（V2）

> **📌 主仓库在 [Gitee](https://gitee.com/meilinkj/qqbot-sdk)**：代码更新、[Issues 反馈](https://gitee.com/meilinkj/qqbot-sdk/issues)、[PR 贡献](https://gitee.com/meilinkj/qqbot-sdk/pulls) 请全部前往 Gitee。
> GitHub 仅为自动同步镜像，不处理反馈。📚 **完整使用文档见 [Gitee Wiki](https://gitee.com/meilinkj/qqbot-sdk/wikis)**。

严格对齐 [QQ 机器人官方开发文档（V2）](https://bot.q.qq.com/wiki/develop/api-v2/) 的 Java SDK，覆盖 **QQ 群聊**、**单聊 C2C**、**QQ 频道** 三类机器人接口。不兼容任何非官方协议（OneBot / Simbot / NapCat 等）。

- Java 17+，轻量依赖：Jackson + OkHttp + Java-WebSocket + SLF4J
- AccessToken 自动刷新；网关心跳、指数退避重连、会话 Resume
- 类型化事件监听；同步 / 异步调用；重试、限流、错误码解析
- 富媒体 URL / 直传 / 分片上传；Spring Boot 3 Starter
- 53 项单元测试，Apache License 2.0

## 安装

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.0.0</version>
</dependency>
```

Spring Boot 项目改用 `qqbot-sdk-spring-boot-starter`（同版本）。

## 快速开始

凭证在 [QQ 开放平台](https://q.qq.com) 创建机器人后获取，通过环境变量注入，**不要硬编码**。

```java
QQBotConfig config = QQBotConfig.builder(appId, appSecret)
        .intents(Intents.of(Intents.GROUP_AND_C2C_EVENT))
        .build();

try (QQBotClient client = new QQBotClient(config)) {
    client.on(GroupAtMessageCreateEvent.class, e ->
            client.group().replyText(e.getGroupOpenid(), e.getId(), "回声: " + e.getContent()));
    client.on(C2CMessageCreateEvent.class, e ->
            client.c2c().replyText(e.getAuthor().getUserOpenid(), e.getId(), "回声: " + e.getContent()));
    client.start();
    Thread.currentThread().join();
}
```

## 📚 详细文档（Gitee Wiki）

Wiki 按四大板块组织：**入门 / 核心概念 / 接口指南 / 集成与工程**，每个功能模块独立成页。

| 分类 | 页面 |
|---|---|
| 入门 | [快速开始](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门/快速开始) · [配置指南](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门/配置指南) · [常见问题](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门/常见问题) |
| 核心概念 | [事件系统](https://gitee.com/meilinkj/qqbot-sdk/wikis/核心概念/事件系统) · [网关与连接](https://gitee.com/meilinkj/qqbot-sdk/wikis/核心概念/网关与连接) · [错误处理与调试](https://gitee.com/meilinkj/qqbot-sdk/wikis/核心概念/错误处理与调试) · [模块架构](https://gitee.com/meilinkj/qqbot-sdk/wikis/核心概念/模块架构) |
| 接口指南 | [群聊接口](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/群聊接口) · [单聊C2C接口](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/单聊C2C接口) · [频道接口](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/频道接口) · [富媒体上传](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/富媒体上传) · [消息类型](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/消息类型) · [互动事件](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/互动事件) · [异步调用](https://gitee.com/meilinkj/qqbot-sdk/wikis/接口指南/异步调用) |
| 集成与工程 | [SpringBoot集成](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与工程/SpringBoot集成) · [构建与发布](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与工程/构建与发布) |

## 反馈与贡献

- 🐛 Bug / 功能建议：[Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)（已配置表单模板）
- 🔀 代码贡献：[Gitee PR](https://gitee.com/meilinkj/qqbot-sdk/pulls)，流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 平台侧错误请携带 `trace_id` 通过 QQ 开放平台渠道反馈

## 许可证

[Apache License 2.0](LICENSE)