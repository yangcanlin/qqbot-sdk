# 贡献指南

感谢关注 QQ 开放平台机器人 Java SDK！

## 核心原则

1. **官方文档是唯一依据**：所有接口路径、字段、opcode、intents、错误码必须与
   [官方开发文档](https://bot.q.qq.com/wiki/develop/api-v2/) 一致。
   新增或修改能力前，先定位官方文档对应接口，再实现代码，并同步更新 Javadoc 文档链接、枚举与 README。
2. **禁止**兼容非官方协议（OneBot / Simbot / NapCat 等）、硬编码密钥、写死测试账号、耦合第三方机器人框架。
3. 保持轻量依赖：core 仅 Jackson / OkHttp / Java-WebSocket / SLF4J。

## 开发流程

```bash
git clone <repo-url>
cd qqbot-sdk
mvn clean install   # 编译 + 单元测试（Java 17+）
```

- 提交前确保 `mvn clean install` 全部通过。
- 新功能需附带单元测试；协议类测试的断言值请注明官方文档出处。
- 代码风格：显式 `@JsonProperty` snake_case 映射、流式 setter、SLF4J 日志（禁止 `System.out`）。
- 每个接口 / 枚举 / 关键方法的 Javadoc 附官方文档锚点链接。

## 提交与 PR

- Commit message 使用简洁的祈使句，说明"为什么"。
- PR 请描述：对应的官方文档链接、变更内容、测试方式。
- Bug 报告请使用 Issue 模板，附上 `trace_id`（HTTP 响应头 `X-Tps-trace-ID` 或响应体 `trace_id`）便于平台排查。

## 发布（Maintainer）

`release` profile 启用 source / javadoc / gpg / central-publishing 插件：

1. `~/.m2/settings.xml` 配置：

```xml
<server>
  <id>central</id>
  <username><!-- Central Portal token username --></username>
  <password><!-- Central Portal token password --></password>
</server>
```

2. 准备 GPG 密钥（`gpg --gen-key` 并上传公钥到公共密钥服务器）。
3. 执行：

```bash
mvn clean deploy -Prelease
```

默认 `autoPublish=false`，需在 [Central Portal](https://central.sonatype.com) 手动确认发布。

## 许可证

贡献的代码将以 [Apache License 2.0](LICENSE) 发布。