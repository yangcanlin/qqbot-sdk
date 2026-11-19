# 贡献指南

> 最后修改时间：2026-11-19 10:24:30

感谢关注 QQ 开放平台机器人 Java SDK！

## 核心原则

1. **官方文档是唯一依据**：所有接口路径、字段、opcode、intents、错误码必须与
   [官方开发文档](https://bot.q.qq.com/wiki/develop/api-v2/) 一致。
   新增或修改能力前，先定位官方文档对应接口，再实现代码，并同步更新 Javadoc 文档链接、枚举与 README。
2. **禁止**兼容非官方协议（OneBot / Simbot / NapCat 等）、硬编码密钥、写死测试账号、耦合第三方机器人框架。
3. 保持轻量依赖：core 仅 Jackson / OkHttp / Java-WebSocket / SLF4J。

## 开发流程

主仓库在 Gitee（<https://gitee.com/meilinkj/qqbot-sdk>），GitHub 为自动同步镜像，不接受反馈与 PR。

```bash
git clone https://gitee.com/meilinkj/qqbot-sdk.git
cd qqbot-sdk
mvn clean install   # 编译 + 单元测试（Java 17+）
```

- 提交前确保 `mvn clean install` 全部通过。
- 新功能需附带单元测试；协议类测试的断言值请注明官方文档出处。
- 公开 API 有增减 / 签名变化时，同步更新 AI Skill 分发源的签名速查：`skill/references/api-reference.md`（发版时重新从源码提取），并运行 `scripts/build-skill-package.sh` 重新生成 `dist/qqbot-sdk-skill.zip`。
- 代码风格：显式 `@JsonProperty` snake_case 映射、流式 setter、SLF4J 日志（禁止 `System.out`）。
- 每个接口 / 枚举 / 关键方法的 Javadoc 附官方文档锚点链接，并且**分清两条线**：接口方法首行写「官方：HTTP方法 路径（官方中文名）」，事件类首行写「官方事件名 + 事件页」；`Intents.XXX` 是订阅开关不是事件名，不要混用。
- 文档只写**当前版本的正确用法**，不写"从旧版本怎么升级 / 旧地址迁移 / 兼容别名"这类历史包袱说明；版本沿革写进 `CHANGELOG.md`，并同步到 Wiki 的 `10-更新日志/<N-版本号>.md`（每个版本一页，新增版本时新建一页并按 `N-` 前缀排在最前面）。
- 改 Wiki（`qqbot-sdk-wiki` 仓库）后运行 `python3 scripts/update-wiki-meta.py <wiki目录>`，
  自动刷新每页页首「最后修改时间」（取文件修改时间，格式 `yyyy-MM-dd HH:mm:ss`）与页尾「上一章 / 下一章」导航；
  新增页面按 `N-` 前缀插入阅读顺序，重跑脚本即可接续链条。本仓库根目录的 `README.md` / `CHANGELOG.md` / `CONTRIBUTING.md` 里的「最后修改时间」是手写的，改动后要同步改成当前时间（同样精确到秒）。

## 提交与 PR

- Commit message 使用简洁的祈使句，说明"为什么"。
- PR 请提交到 [Gitee 主仓库](https://gitee.com/meilinkj/qqbot-sdk/pulls)，描述：对应的官方文档链接、变更内容、测试方式。
- Bug 报告与功能建议请到 [Gitee Issues](https://gitee.com/meilinkj/qqbot-sdk/issues) 提交（Issue 表单模板位于仓库 `.gitee/ISSUE_TEMPLATE/`），附上 `trace_id`（HTTP 响应头 `X-Tps-trace-ID` 或响应体 `trace_id`）便于平台排查。

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