# Changelog

> 最后修改时间：2026-11-19 10:24:30

本文件记录 SDK 的版本变更。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [1.1.0] - 未发布（本地构建，暂不提交/发布）

### 新增

- api：`BotApi`（`client.bot()`）机器人信息接口，对齐官方「服务端接口/机器人」章节：
  获取机器人详情（GET /users/@me）、获取机器人频道列表（GET /users/@me/guilds，支持
  before/after/limit 分页）、获取机器人群内状态（GET /v2/groups/{group_openid}/bot_state，
  仅白名单机器人可用）、生成分享链接（POST /v2/generate_url_link，callback_data≤32 字符）。
- api：响应模型 `BotInfo` / `BotGuild` / `GroupBotState` / `ShareLinkResponse`。
- api：`GroupManageApi`（`client.groupManage()`）群聊管理接口，对齐官方「服务端接口/群聊管理」
  全部 16 个端点：群基本信息、群成员列表/信息（白名单）、批量移除成员、群黑名单查询/操作、
  禁言状态查询/成员禁言设置、入群申请列表/审批、入群自动审批策略（查询/创建/修改/删除/执行/白名单号码）。
- api：模型 `GroupInfo` / `GroupMember` / `Page<T>` / `BlacklistEntry` / `BlacklistOperateResult` /
  `MuteSetting` / `JoinRequest` / `JoinApprovalStrategy` / `StrategyOperateResult` /
  `StrategyWhitelistResult` 与请求体 `MuteSettingRequest` / `BlacklistUserRequest` /
  `JoinApprovalOpRequest` / `JoinApprovalStrategyRequest` / `JoinApprovalStrategyUpdateRequest` /
  `StrategyGroupAction` / `StrategyWhitelistUsersRequest`。
- core：`Intents` 补齐官方 `GROUP_MEMBER_EVENT(1<<24)` 群成员事件位。
- core：事件模型与 `EventType` 对齐官方新增 4 个类型化事件——
  `GroupMemberAddEvent`（GROUP_MEMBER_ADD 群成员加入）、`GroupMemberRemoveEvent`
  （GROUP_MEMBER_REMOVE 群成员退出）、`GroupJoinRequestEvent`（GROUP_JOIN_REQUEST 用户申请加群，
  含 verify_info/auto_approved，仅机器人群管理员可收）、`SubscribeMessageStatusEvent`
  （SUBSCRIBE_MESSAGE_STATUS 订阅消息授权状态变更）。
- core：把官方有事件页、此前只走 `GenericEvent` 的 9 个事件补成强类型（字段逐条取自官方事件页字段表与示例 JSON）：
  `GuildCreateEvent` / `GuildUpdateEvent` / `GuildDeleteEvent`（GUILD_CREATE/UPDATE/DELETE，共享基类 `GuildEvent`，
  含 id/name/icon/owner_id/member_count/max_members/description/joined_at/op_user_id；Update 为变更后数据、
  Delete 为变更前数据且官方无差分字段）、`ChannelCreateEvent` / `ChannelUpdateEvent` / `ChannelDeleteEvent`
  （CHANNEL_CREATE/UPDATE/DELETE，共享基类 `ChannelEvent`，含 id/guild_id/name/type/sub_type/position/owner_id/op_user_id）、
  `FriendDelEvent`（FRIEND_DEL，含内联 `FriendAuthor.union_openid`，官方示例未出现 author 故按可空处理）、
  `C2CMsgReceiveEvent` / `C2CMsgRejectEvent`（C2C_MSG_RECEIVE/REJECT 单聊主动消息开关，openid+timestamp）。
- core：`GUILD_MEMBER_*`、`MESSAGE_REACTION_*`、`FORUM_*`、`AUDIO_*` 共 17 个事件**继续**以 `GenericEvent` 分发——
  官方 api-v2 仅在「事件订阅 Intents」清单列出事件名，无事件页、无事件体字段规格（`GUILD_MEMBER_*` 连对象模型页都没有），
  按「官方文档唯一依据」原则不凭空建模；用 `client.onRaw(事件名, consumer)` 或 `on(GenericEvent.class, e -> e.getRaw())` 接。
- core：`HttpApiClient` 新增 PATCH 支持（修改入群自动审批策略接口使用）。
- api：`ChannelManageApi`（`client.channelManage()`）对齐官方「服务端接口/频道管理」全部 6 个端点：
  获取频道详情（GET /guilds/{guild_id}）、子频道列表（GET /guilds/{guild_id}/channels）、
  创建子频道（POST .../channels）、子频道详情/修改（GET、PATCH /channels/{channel_id}）、
  删除子频道（DELETE /channels/{channel_id}）；模型 `Guild` / `Channel`（含官方类型常量）与
  请求体 `ChannelCreateRequest`（text/voice/category，分组 position≥2）/ `ChannelUpdateRequest`（rename/move）。
- api：`MenuApi`（`client.menu()`）查询与修改全局自定义菜单（GET、PUT /v2/menu，仅单聊场景、全量覆盖，
  最多 10 项、二级菜单最多 5 项、链接必须 https）；模型 `Menu` / `MenuItem` / `SubMenuItem` / `MenuSwitch`
  / `MenuUpdateRequest` / `MenuResponse` / `VersionResponse`。
- api：`PanelApi`（`client.panel()`）对齐官方指令面板 6 个端点：面板列表（GET /v2/panels?scope&cursor&limit，
  cursor 分页 `Page<PanelRecord>`）、创建（POST /v2/panels，返回 panel_id）、详情（GET /v2/panels/{panel_id}）、
  修改（PUT，覆盖元素与备注、不影响关联对象）、删除（DELETE）、关联对象增删（PUT /v2/panels/{panel_id}/target，
  op=add/del，单次≤20）；模型 `Panel` / `PanelItem` / `PanelRecord` / `PanelCreateResponse` 与请求体
  `PanelCreateRequest` / `PanelUpdateRequest` / `PanelTargetRequest`（含 channel/dm 仅 all 等官方约束本地校验）。
- api：`C2CApi` 补齐官方单聊剩余 2 个端点——流式发送消息（POST /v2/users/{user_openid}/stream_messages，
  50 QPS，`StreamMessageRequest` append/replace + input_state 1/10 + index 递增，`StreamMessageResponse`
  首片返回 stream_msg_id）与获取用户基本信息（GET /v2/users/{user_openid}/info，10 QPS，`UserInfo`）。
- api：`GatewayInfoApi`（`client.gatewayInfo()`）暴露官方 GET /gateway（获取通用 WSS 接入点），
  模型 `GatewayInfo`；SDK 建连时内部已自动调用，业务侧可用于探活或自建长连接。
- core：`HttpApiClient` 新增 `putAsync(path, body, responseType)`（菜单/面板 PUT 异步调用）。
- api：分页/查询串工具抽为 `support.Paging`（cursor、参数拼接、`Page<T>` 解析），群聊管理与指令面板共用。
- api：`GatewayInfoApi.getGatewayBot()` / `getGatewayBotAsync()` 对齐官方 GET /gateway/bot
  （官方页面「获取带分片 WSS 接入点」`api-v2/openapi/wss/shard_url_get`），模型 `GatewayBotInfo`
  返回 `url` + `shards`（平台建议分片数）+ `session_start_limit`（total / remaining / reset_after /
  max_concurrency）。多进程分片部署前先用它确定 `QQBotConfig.shard(shardId, numShards)` 的 numShards。

### 修复

- skill：`references/recipes.md` 与 `SKILL.md` 删除错误的「4004 鉴权失败」关闭码——官方错误码表
  与网关关闭码表中均无 4004；HTTP 认证失败是状态码 401（SDK 自动刷新 token 后重试），网关侧鉴权/
  意图问题表现为 4013/4014，下架与封禁为 4914/4915。
- api：`GroupInfo` / `GroupMember` 三处 Javadoc 字段语义按官方口径修正——`group_finger_memo` 是
  群简介而非 QQ 群号码；`member_role` 取值为 member / admin / owner（原写作 common）；
  `union_group_openid` 为绑定三方应用后的跨应用关联群 openid。
- skill：`references/api-reference.md` 与源码签名对齐——补 `MessageMarkdown.setContent` /
  `setForceVerifyImageResource` 并标注 `setTemplateId` / `setCustomTemplateId` 已废弃、
  `InputNotify.typing` 秒数范围 1~60、`MessageAuditedEvent` 审核结论在事件对象的 `isPass()` 上、
  删除「官方文档没有 /gateway/bot 接口页」的错误结论（见上方新增条目）。

### 变更

- api：全部公开方法 Javadoc 首行统一为「官方：HTTP方法 路径（官方中文名）」，IDE 悬浮即知对应官方接口。
- 单元测试增至 77 项（core 32 + api 41 + starter 4；本版本新增 `GroupManageApiTest` 6 项、
  `ChannelManageApiTest` 4 项、`MenuApiTest` 3 项、`PanelApiTest` 3 项、流式/用户信息/网关路径 2 项、
  群成员/入群申请/订阅授权事件解析 1 项、带分片网关接入点 1 项、
  频道与子频道变更事件解析 + 好友删除与单聊推送开关事件解析 2 项）。
- 文档：新增 `scripts/update-wiki-meta.py`，为 Wiki 每页页首写「最后修改时间」、页尾写「上一章 / 下一章」
  导航（线性串联，首尾回文档中心首页），改完 Wiki 重跑一次即可。
- 文档：确立「只写当前版本的正确用法」——不写从旧版本升级的步骤、不写旧页面地址迁移表、不做兼容别名，
  历史沿革统一放本 CHANGELOG；Home 的旧目录迁移对照表已删除，要求写进 CONTRIBUTING 与
  「8-集成与开发/5-构建与发布」文档同步清单。
- 文档：README 的 Wiki 导航表由旧四目录改为镜像官方章节层级的目录结构，并同步 AI Skill 模板数为
  R1~R16；Gitee Wiki 重构为「1-起步指南 … 10-更新日志」十目录，完整可运行示例集中在「9-附录/1-示例工程」。
- 文档：群聊「必须 @ 机器人才收消息」表述修正为 GROUP_AT_MESSAGE_CREATE（@机器人）+
  GROUP_MESSAGE_CREATE（平台开启「接收所有消息」后全量推送），与官方事件文档一致。
- 文档：Wiki 与 Skill 全面拆分**接口**与**事件**两条线，对齐官方 api-v2 的文档分型（接口=官方
  `autogen/api` / `server-inter` 的「HTTP 方法 + 路径」，事件=官方 `autogen/event` 的「事件名 + intents」）：
  新增「9-附录/3-官方事件覆盖对照表」（49 个事件名逐条对应事件类，含 17 个无事件体规格事件的
  `onRaw` 兜底说明与 READY/RESUMED 生命周期帧），「9-附录/2-官方接口覆盖对照表」收敛为纯接口表；
  消息总览、按钮与互动回执、入群申请与审批、频道消息与私信等功能页改为分小节列两张表；
  Home 覆盖总表拆成「一、服务端接口」「二、事件」两张；`client.gateway()` 与 `client.on(...)`
  从接口表移出。修正两处混用：`MESSAGE_AUDIT` 是 Intent 名（事件名只有 `MESSAGE_AUDIT_PASS` /
  `MESSAGE_AUDIT_REJECT`）、`AT_MESSAGE_CREATE` 属 `PUBLIC_GUILD_MESSAGES`(1<<30) 而非 `GUILD_MESSAGES`(1<<9)。
- 文档：AI Skill 同步两线口径——`SKILL.md` 新增硬约束「接口与事件不要混」、平台规则速记拆成
  「接口侧」「事件侧」两张表，`references/recipes.md` 顶部定义【事件】/【接口】行内标注并在 R1/R2/R14 落地，
  `references/api-reference.md` 事件表补事件名/Intent 区分与 3 个删除事件的建模来源说明。
- 文档：Spring Boot Starter 明确**不支持注解式事件监听**（无自定义注解扫描，SDK 事件不走
  `ApplicationContext.publishEvent`，Spring 原生 `@EventListener` 收不到机器人事件），
  Wiki「8-集成与开发/1-SpringBoot集成」补充三种可用写法与 `@Bean` lambda 写法。
- 文档：Wiki 新增「10-更新日志」目录，每个版本一页（`1-1.1.0`、`2-1.0.0`），对齐官方文档的
  「变更记录」章节；新增版本时按 `N-` 前缀建页并重跑导航脚本。
- 文档：`scripts/update-wiki-meta.py` 的「最后修改时间」精确到秒（`yyyy-MM-dd HH:mm:ss`）；
  README / CHANGELOG / CONTRIBUTING 的手写时间行同步改为秒级，并要求改动后一并更新。

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