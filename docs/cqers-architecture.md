# CQERS 架构与执行规范

CQERS 是本项目对 **Command–Query–Event Responsibility Segregation** 的命名：在 CQRS 的读写责任分离之外，明确本服务收到的业务事件由独立执行器处理。这是项目约定，不假设存在行业统一的 CQERS 标准，也不要求事件溯源、独立读写数据库或新的消息中间件。

## 分层与命名

| 输入或角色 | 应用层位置 | 执行责任 |
|---|---|---|
| `XxxCommand` | `application/<业务域>/pojo/command/` | 表达一次写入或其他有副作用的用例。 |
| `XxxCmdExe` | `application/<业务域>/executor/command/` | 执行对应 Command，如 `CreateUserCommand` 对应 `CreateUserCmdExe`。 |
| `XxxQuery` | `application/<业务域>/pojo/query/` | 表达一次查询，包括分页、过滤和必要的租户范围。 |
| `XxxQueryExe` | `application/<业务域>/executor/query/` | 执行对应 Query，如 `UserDetailQuery` 对应 `UserDetailQueryExe`；不写入业务状态或发布业务事件。 |
| 接收侧 `XxxEvent` | `application/<业务域>/pojo/event/` | 保存已收到的业务事实，协议 DTO 与应用事件按边界转换。 |
| `XxxEventExe` | `application/<业务域>/executor/event/` | 处理对应的接收侧 Event，如 `UserImportedEvent` 对应 `UserImportedEventExe`。 |
| `XxxEventHandler` | `adapter/<业务域>/event/` | 显式配置事件时由 code generator 生成的传输无关入口，委托对应 EventExe；具体消费者需要自行接线。 |
| `XxxManage` / `XxxManageImpl` | `application/<业务域>/manage/` / `manage/impl/` | 保留稳定用例入口并委托 Exe；保持原有接口和必要的事务入口。 |

具体执行器由 Spring 显式命名并注入，直接组合已有 Domain 端口和组件；不增加统一的反射注册表、消息总线或只转发回 Manage 的执行器。多个用例确实共享的复杂规则可以放入现有 Application service，例如 Agent 的 `KnowledgeRetrievalService`。已有直接构造 Manage 实现的调用仍可使用原构造参数；正常 Spring 注入使用对应 Exe。

Light 系列是单 Maven 模块，路径为该模块的包；Service/Web 和 Agent 使用各自的 Application 模块。原来以标量参数表达的 Agent 读写入口在内部转换成对应 Query/Command，再交给 Exe，原 Manage 方法签名保持不变。Agent 现有的 `RetrieveKnowledgeCommand` 名称保留以兼容调用方，但其执行器仍是只读检索。

## 事件方向和事务

- **接收：** Adapter 或消息接收边界负责协议反序列化与校验，将本服务需要处理的业务事实交给 EventExe。Light 的 `UserImportedConsumer → UserImportedEventExe → CreateUserCmdExe` 是示例；消费者可通过 Manage 调用已有 Command 用例。消费的幂等键、重试和死信按既有消息契约设计。
- **发布：** Command 用例通过 Domain 业务端口请求发布；Infrastructure 负责 MQ 或 Outbox 的具体实现。EventExe 不负责发布出站事件。只发布、不在本服务消费的 Domain Event 无需空处理器。
- **事务：** 不把慢速文件、模型或网络调用机械地放进数据库事务。已有 Manage 的事务入口保留；有事务要求的 CommandExe 在可被代理调用时也明确事务。Agent 的 `KnowledgeIngestQueueService` 通过 `KnowledgeIngestPublisher` 在同一调用线程内请求 Infrastructure 的 Outbox 实现；文档行和 Outbox 行需要使用兼容的数据源和事务管理器。
- **投递保证：** Outbox 在同一事务内记录投递意图，不代表下游业务 exactly-once。直接 MQ 须说明双写窗口与恢复；Web 现有 after-commit 发布不是原子 Outbox。
- **事件分类：** Agent 的研究/问答 SSE 是流输出，不因类名包含 Event 就作为消息消费；出站 Event、传输 envelope 和消费者收到的业务事件也不能混为一谈。Agent 现有 `rag-ingest` 是由 Infrastructure `KnowledgeIngestDeliveryHandler` 处理的摄取工作项，不作为接收侧业务 Event 建立空的 EventExe；若后续将其作为独立应用 Command 建模，应补充相应 CmdExe 并保留现有重试、租户与审计语义。

## 兼容与验证

保持 HTTP、GraphQL、RPC、MQ 的现有契约、Bean 名、租户及审计上下文。执行器拆分需要覆盖原用例行为、异常、幂等、事务回滚和事件边界；生成验证还应检查每个应用 Command/Query/接收侧 Event 对应的 Exe 是否存在并实际接入。源工程测试与 H2 不等于真实 PostgreSQL、MQ、Outbox 投递或集群运行验收。

独立的 Components CRUD code generator 支持 native Light、Web 和 Service 的 CQERS Command/Query 执行器，并可根据显式事件定义生成 Event、转换器、EventExe 和不绑定消息中间件的 Adapter Handler。事件消费仍需接入已有的事务幂等 Bean 和具体传输；幂等 Bean 需要同步执行操作，按可信租户、消费方、事件类型、事件 ID 与内容指纹处理重复，并与业务写入使用兼容的事务。Agent 与 Open 不在生成器支持范围内。旧版平铺路径或被移除的已生成事件会触发冲突：审阅并自行保留或迁移源码后，可使用 `check` 输出的 `retirementReview` 和 `retire-legacy` 解除相应归属记录并重新规划；命令会备份状态，不移动或删除源码。不能删除状态或重置哈希来绕过保护。
