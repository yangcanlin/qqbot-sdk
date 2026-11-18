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
| 入门指南 | [启动接入](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/1-启动接入) · [快速开始](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/2-快速开始) · [凭证与接口调用](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/3-凭证与接口调用) · [新手常见问题](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/5-新手常见问题) |
| 功能 | [消息总览](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/1-消息总览) · [群聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/2-群聊消息) · [单聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/3-单聊消息) · [频道消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/4-频道消息) · [消息类型](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/5-消息类型) · [富媒体上传](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/6-富媒体上传) · [按钮与互动](https://gitee.com/meilinkj/qqbot-sdk/wikis/功能/7-按钮与互动) |
| 进阶 | [理解事件与意图](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/1-理解事件与意图) · [网关与重连](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/2-网关与重连) · [配置参考](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/3-配置参考) · [错误处理与调试](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/5-错误处理与调试) · [异步调用](https://gitee.com/meilinkj/qqbot-sdk/wikis/进阶/4-异步调用) |
| 集成与开发 | [SpringBoot集成](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/1-SpringBoot集成) · [模块架构](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/2-模块架构) · [构建与发布](https://gitee.com/meilinkj/qqbot-sdk/wikis/集成与开发/3-构建与发布) |

## 🤖 给你的 AI 编程助手装上 SDK 知识（Trae / Claude Code / CodeBuddy / Qoder / Cursor / Codex 均可用）

AI 编程工具直接写 QQ 机器人代码时，容易凭记忆臆造不存在的方法。本仓库提供 **qqbot-sdk Skill 技能包**（源码在 [skill/](skill/) 目录，打包好的文件在 [dist/qqbot-sdk-skill.zip](dist/qqbot-sdk-skill.zip)），内含 1.0.0 全部方法签名速查 + 11 个可运行任务模板，让 AI 照着真实签名写代码。

> 每次发版会重新生成，内容与 Maven 构件版本严格一致。

**它是什么：** 三个 Markdown 文件——`SKILL.md`（硬约束 + 任务导航 + 平台频控规则）、`references/api-reference.md`（方法签名）、`references/recipes.md`（R1~R11 代码模板）。装到你的 AI 工具的 skills 目录即可自动生效。

### 按工具安装（任选其一）

**Trae**：设置 → 技能与命令 → 创建 → **导入本地技能**，选择下载的 [qqbot-sdk-skill.zip](https://gitee.com/meilinkj/qqbot-sdk/raw/main/dist/qqbot-sdk-skill.zip)。或命令行安装到全局：

```bash
mkdir -p ~/.trae-cn/skills/qqbot-sdk/references && cd ~/.trae-cn/skills/qqbot-sdk \
  && curl -fsSL -o SKILL.md https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/SKILL.md \
  && curl -fsSL -o references/api-reference.md https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/api-reference.md \
  && curl -fsSL -o references/recipes.md https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/recipes.md
```

**Claude Code**：把同样的三个文件放到 `~/.claude/skills/qqbot-sdk/`（全局）或你项目的 `.claude/skills/qqbot-sdk/`（仅当前项目）——上面命令把路径 `~/.trae-cn/skills` 换成 `~/.claude/skills` 即可。

**CodeBuddy（腾讯CodeBuddy IDE/CLI）**：同样认 SKILL.md 格式，放到工作区 `.codebuddy/skills/qqbot-sdk/`，或用 IDE 里的「导入 Skill」按钮选择上面那个 zip。

**Qoder**：放到 `~/.qoder/skills/qqbot-sdk/`（全局）或项目 `.qoder/skills/qqbot-sdk/`，也可在技能市场/本地技能导入里直接选 zip。

**Cursor / Codex / 其他支持 AGENTS.md 的工具**：没有 Skill 机制，把下面这段粘到你项目的 `AGENTS.md`（Codex 是 `AGENTS.md`，Cursor 也官方支持；Claude Code 可放 `CLAUDE.md`）：

```markdown
## QQ 机器人开发（qqbot-sdk）

- Maven 依赖：cn.meilinkj.qqbot:qqbot-sdk-api:1.0.0（Spring Boot 项目用 qqbot-sdk-spring-boot-starter）
- 写任何 QQ 机器人代码前，先读取以下三个文件，严格按其中签名开发；
  仅支持官方 api-v2 协议（api.bot.qq.com），禁止 OneBot / Simbot / NapCat 等非官方协议：
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/SKILL.md
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/api-reference.md
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/recipes.md
```

装好后直接对 AI 说「用 qqbot-sdk 实现群聊 @消息回声」这类需求即可。详细图文说明见 Wiki：[给 AI 助手装 SDK 知识](https://gitee.com/meilinkj/qqbot-sdk/wikis/入门指南/4-给AI助手装SDK知识)。

## 反馈与贡献

- 🐛 Bug / 功能建议：[Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)（已配置表单模板）
- 🔀 代码贡献：[Gitee PR](https://gitee.com/meilinkj/qqbot-sdk/pulls)，流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 平台侧错误请携带 `trace_id` 通过 QQ 开放平台渠道反馈

## 许可证

[Apache License 2.0](LICENSE)