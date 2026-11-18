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

Wiki 参考官方文档做法按学习路线组织：**入门指南（手把手）→ 功能（按场景：群聊/单聊/频道…）→ 进阶 → 集成与开发**。新手请直接阅读 [Wiki 首页的三条学习路线](https://gitee.com/meilinkj/qqbot-sdk/wikis/Home)。

| 分类 | 页面 |
|---|---|
| 入门指南 | [启动接入](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/启动接入) · [快速开始](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/快速开始) · [凭证与接口调用](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/凭证与接口调用) · [新手常见问题](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/新手常见问题) |
| 功能 | [消息总览](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/消息总览) · [群聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/群聊消息) · [单聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/单聊消息) · [频道消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/频道消息) · [消息类型](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/消息类型) · [富媒体上传](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/富媒体上传) · [按钮与互动](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/按钮与互动) |
| 进阶 | [理解事件与意图](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/理解事件与意图) · [网关与重连](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/网关与重连) · [配置参考](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/配置参考) · [错误处理与调试](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/错误处理与调试) · [异步调用](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/异步调用) |
| 集成与开发 | [SpringBoot集成](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/SpringBoot集成) · [模块架构](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/模块架构) · [构建与发布](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/构建与发布) |

## 🤖 AI 辅助开发（Qoder / Cursor / Claude Code / Codex / Trae 均可用）

本仓库自带 **qqbot-sdk Skill**（`.qoder/skills/qqbot-sdk/`），为 AI 编程代理提供精确的方法签名速查与 11 个任务模板，避免 AI 凭记忆臆造 API：

| 文件 | 内容 |
|---|---|
| `.qoder/skills/qqbot-sdk/SKILL.md` | 硬约束、最小骨架、任务导航、平台频控/时限规则、排障顺序 |
| `.qoder/skills/qqbot-sdk/references/api-reference.md` | 1.0.0 全部公开类与方法签名、Intents 位值、配置默认值 |
| `.qoder/skills/qqbot-sdk/references/recipes.md` | R1~R11 可运行模板：群聊/单聊/富媒体/按钮互动/频道/Spring Boot 等 |

用法：

- **Qoder**：clone 本仓库后用 Qoder 打开即自动加载项目级 Skill；在自己的机器人项目中把该文件夹复制到 `~/.qoder/skills/`（全局）或项目 `.qoder/skills/`（仅当前项目）；
- **Trae / Cursor / Claude Code / Codex 等其他工具**：无 Skill 机制，直接让 AI 读取上述三个 Markdown 文件作为上下文即可（提示词示例："先阅读 `.qoder/skills/qqbot-sdk/SKILL.md` 及其 references，然后基于 qqbot-sdk 实现 XX 功能"）。

> Skill 内容随每次发版从源码重新提取，与构件版本保持一致。

## 反馈与贡献

- 🐛 Bug / 功能建议：[Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)（已配置表单模板）
- 🔀 代码贡献：[Gitee PR](https://gitee.com/meilinkj/qqbot-sdk/pulls)，流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 平台侧错误请携带 `trace_id` 通过 QQ 开放平台渠道反馈

## 许可证

[Apache License 2.0](LICENSE)