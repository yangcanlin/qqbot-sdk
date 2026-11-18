# Changelog

本文件记录 SDK 的版本变更。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.0.0] - 2026-11-18

### 新增

- core：AccessToken 自动管理（60 秒过期窗口刷新、401 自动重取）、OpenAPI HTTP 客户端
  （同步/异步、指数退避重试、客户端限流、平台错误码与 trace_id 解析、201/202 等待审核语义）。
- core：WebSocket 网关（Hello/Identify/Heartbeat/ACK/Resume/Reconnect/InvalidSession 全 opcode，
  官方 intents 位掩码、关闭码处理规则、指数退避重连、seq 管理）。
- core：类型化事件模型与分发器（群@/群消息/单聊/机器人进出群/好友/互动/频道消息/审核等），
  未建模事件以 GenericEvent / 原始 JSON 兜底。
- api：群聊 V2（发送文本/Markdown/富媒体/卡片/键盘、被动回复 msg_seq 自动递增、撤回、
  URL/二进制/分片上传）、单聊 V2（另含输入中状态、互动召回）、频道消息与私信、互动事件回执。
- api：`QQBotClient` 统一入口（AutoCloseable）。
- spring-boot-starter：`qqbot.*` 配置属性、QQBotClient 自动装配、EventListener Bean 自动发现。
- example：环境变量驱动的最小回声机器人 Demo。
- 单元测试 53 项（协议常量、Payload、token 生命周期、HTTP 重试/错误解析、事件分发、
  请求序列化、分片上传全流程、接口路径、starter 装配）。
- 文档：README / CONTRIBUTING / ISSUE 模板 / CI；Javadoc 全量附官方文档锚点。