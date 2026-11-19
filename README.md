# QQ 开放平台机器人 Java SDK（V2）

> **📌 主仓库在 [Gitee](https://gitee.com/meilinkj/qqbot-sdk)**：代码更新、[Issues 反馈](https://gitee.com/meilinkj/qqbot-sdk/issues)、[PR 贡献](https://gitee.com/meilinkj/qqbot-sdk/pulls) 请全部前往 Gitee。
> GitHub 仅为自动同步镜像，不处理反馈。📚 **完整使用文档见 [Gitee Wiki](https://gitee.com/meilinkj/qqbot-sdk/wikis)**。
>
> 最后修改时间：2026-11-19 10:24:30

严格对齐 [QQ 机器人官方开发文档（V2）](https://bot.q.qq.com/wiki/develop/api-v2/) 的 Java SDK，覆盖 **QQ 群聊**、**单聊 C2C**（含流式打字机消息、用户基本信息）、**QQ 频道** 消息与 **频道管理**（频道详情/子频道增删改查）消息接口、**机器人信息**（详情/频道列表/群内状态/分享链接）、**群聊管理**（群信息/群成员/批量移除/黑名单/禁言/入群申请审批/自动审批策略）、**自定义菜单 / 指令面板**（聊天窗口按钮与指令入口）与 **网关接入点**（通用 WSS 地址 / 带分片地址与 Session 配额）。不兼容任何非官方协议（OneBot / Simbot / NapCat 等）。

- Java 17+，轻量依赖：Jackson + OkHttp + Java-WebSocket + SLF4J
- AccessToken 自动刷新；网关心跳、指数退避重连、会话 Resume
- 类型化事件监听（群成员加入/退出、用户申请加群、订阅消息授权状态、频道与子频道变更、好友删除、单聊主动消息开关）；同步 / 异步调用；重试、限流、错误码解析
- 富媒体 URL / 直传 / 分片上传；流式打字机消息；Spring Boot 3 Starter
- 77 项单元测试，Apache License 2.0

## 安装

```xml
<dependency>
    <groupId>cn.meilinkj.qqbot</groupId>
    <artifactId>qqbot-sdk-api</artifactId>
    <version>1.1.0</version>
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

Wiki 目录**镜像官方开发文档的章节层级**，并按官方口径把两条线分开写：**接口**（你主动调用的 REST 请求，官方页给方法+路径）与**事件**（平台 WebSocket 推送，官方页给事件名+intents）。官方每个接口大类对应一个目录，每个功能页里接口表与事件表各归各的，附录再各给一张全量对照表，照着官方名字就能找到对应的 Java 写法。新手请先看 [Wiki 首页的三条学习路线](https://gitee.com/meilinkj/qqbot-sdk/wikis/Home)。

| 目录（对应官方章节） | 页面 |
|---|---|
| 1-起步指南（开发准备） | [启动接入](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/1-启动接入) · [获取访问凭证与调用接口](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/2-获取访问凭证与调用接口) · [事件订阅与意图](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/3-事件订阅与意图) · [快速开始](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/4-快速开始) · [新手常见问题](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/5-新手常见问题) · [给AI助手装SDK知识](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/6-给AI助手装SDK知识) |
| 2-消息收发（服务端接口 › 消息收发） | [消息总览](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/1-消息总览) · [单聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/2-单聊消息) · [单聊流式消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/3-单聊流式消息) · [群聊消息](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/4-群聊消息) · [频道消息与私信](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/5-频道消息与私信) · [消息类型与内容格式](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/6-消息类型与内容格式) · [富媒体上传](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/7-富媒体上传) · [按钮与互动回执](https://gitee.com/meilinkj/qqbot-sdk/wikis/2-消息收发/8-按钮与互动回执) |
| 3-机器人（服务端接口 › 机器人） | [获取机器人详情与频道列表](https://gitee.com/meilinkj/qqbot-sdk/wikis/3-机器人/1-获取机器人详情与频道列表) · [获取用户基本信息与群内状态](https://gitee.com/meilinkj/qqbot-sdk/wikis/3-机器人/2-获取用户基本信息与群内状态) · [生成分享链接](https://gitee.com/meilinkj/qqbot-sdk/wikis/3-机器人/3-生成分享链接) |
| 4-群聊管理（服务端接口 › 群聊管理） | [群信息与群成员](https://gitee.com/meilinkj/qqbot-sdk/wikis/4-群聊管理/1-群信息与群成员) · [批量移除成员与黑名单](https://gitee.com/meilinkj/qqbot-sdk/wikis/4-群聊管理/2-批量移除成员与黑名单) · [群禁言](https://gitee.com/meilinkj/qqbot-sdk/wikis/4-群聊管理/3-群禁言) · [入群申请与审批](https://gitee.com/meilinkj/qqbot-sdk/wikis/4-群聊管理/4-入群申请与审批) · [入群自动审批策略](https://gitee.com/meilinkj/qqbot-sdk/wikis/4-群聊管理/5-入群自动审批策略) |
| 5-频道管理（服务端接口 › 频道管理） | [频道与子频道管理](https://gitee.com/meilinkj/qqbot-sdk/wikis/5-频道管理/1-频道与子频道管理) |
| 6-菜单与面板（服务端接口 › 菜单与面板） | [自定义菜单](https://gitee.com/meilinkj/qqbot-sdk/wikis/6-菜单与面板/1-自定义菜单) · [指令面板](https://gitee.com/meilinkj/qqbot-sdk/wikis/6-菜单与面板/2-指令面板) |
| 7-网关与协议（接口框架 / 错误与调试） | [网关连接与重连](https://gitee.com/meilinkj/qqbot-sdk/wikis/7-网关与协议/1-网关连接与重连) · [获取WSS接入点](https://gitee.com/meilinkj/qqbot-sdk/wikis/7-网关与协议/2-获取WSS接入点) · [错误码与排查](https://gitee.com/meilinkj/qqbot-sdk/wikis/7-网关与协议/3-错误码与排查) |
| 8-集成与开发（SDK 工程化） | [SpringBoot集成](https://gitee.com/meilinkj/qqbot-sdk/wikis/8-集成与开发/1-SpringBoot集成) · [配置参考](https://gitee.com/meilinkj/qqbot-sdk/wikis/8-集成与开发/2-配置参考) · [异步调用与线程模型](https://gitee.com/meilinkj/qqbot-sdk/wikis/8-集成与开发/3-异步调用与线程模型) · [模块架构与扩展](https://gitee.com/meilinkj/qqbot-sdk/wikis/8-集成与开发/4-模块架构与扩展) · [构建与发布](https://gitee.com/meilinkj/qqbot-sdk/wikis/8-集成与开发/5-构建与发布) |
| 9-附录 | [示例工程](https://gitee.com/meilinkj/qqbot-sdk/wikis/9-附录/1-示例工程) · [官方接口覆盖对照表](https://gitee.com/meilinkj/qqbot-sdk/wikis/9-附录/2-官方接口覆盖对照表) · [官方事件覆盖对照表](https://gitee.com/meilinkj/qqbot-sdk/wikis/9-附录/3-官方事件覆盖对照表) |
| 10-更新日志 | [1.1.0](https://gitee.com/meilinkj/qqbot-sdk/wikis/10-更新日志/1-1.1.0) · [1.0.0](https://gitee.com/meilinkj/qqbot-sdk/wikis/10-更新日志/2-1.0.0) |

官方 api-v2 的 **48 个接口页**（你主动调用的 REST 请求）逐条对应到 SDK 方法，见 [官方接口覆盖对照表](https://gitee.com/meilinkj/qqbot-sdk/wikis/9-附录/2-官方接口覆盖对照表)；官方累计 **49 个事件名**（平台通过 WebSocket 推送的消息，用事件名 + Intent 标识）逐条对应到 SDK 事件类，见 [官方事件覆盖对照表](https://gitee.com/meilinkj/qqbot-sdk/wikis/9-附录/3-官方事件覆盖对照表)。两条线分开列，不混在一张表里。

## 🤖 给你的 AI 编程助手装上 SDK 知识（Trae / Claude Code / CodeBuddy / Qoder / Cursor / Codex 均可用）

AI 编程工具直接写 QQ 机器人代码时，容易凭记忆臆造不存在的方法。本仓库提供 **qqbot-sdk Skill 技能包**（源码在 [skill/](skill/) 目录，打包好的文件在 [dist/qqbot-sdk-skill.zip](dist/qqbot-sdk-skill.zip)），内含 1.1.0 全部方法签名速查 + 16 个可运行任务模板，让 AI 照着真实签名写代码。

> 每次发版会重新生成，内容与 Maven 构件版本严格一致。

**它是什么：** 三个 Markdown 文件——`SKILL.md`（硬约束 + 任务导航 + 平台频控规则）、`references/api-reference.md`（方法签名）、`references/recipes.md`（R1~R16 代码模板）。装到你的 AI 工具的 skills 目录即可自动生效。

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

- Maven 依赖：cn.meilinkj.qqbot:qqbot-sdk-api:1.1.0（Spring Boot 项目用 qqbot-sdk-spring-boot-starter）
- 写任何 QQ 机器人代码前，先读取以下三个文件，严格按其中签名开发；
  仅支持官方 api-v2 协议（api.bot.qq.com），禁止 OneBot / Simbot / NapCat 等非官方协议：
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/SKILL.md
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/api-reference.md
  - https://gitee.com/meilinkj/qqbot-sdk/raw/main/skill/references/recipes.md
```

装好后直接对 AI 说「用 qqbot-sdk 实现群聊 @消息回声」这类需求即可。详细图文说明见 Wiki：[给 AI 助手装 SDK 知识](https://gitee.com/meilinkj/qqbot-sdk/wikis/1-起步指南/6-给AI助手装SDK知识)。

## 反馈与贡献

- 🐛 Bug / 功能建议：[Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues)（已配置表单模板）
- 🔀 代码贡献：[Gitee PR](https://gitee.com/meilinkj/qqbot-sdk/pulls)，流程见 [CONTRIBUTING.md](CONTRIBUTING.md)
- 平台侧错误请携带 `trace_id` 通过 QQ 开放平台渠道反馈

## 许可证

[Apache License 2.0](LICENSE)