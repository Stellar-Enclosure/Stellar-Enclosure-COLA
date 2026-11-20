# stellar-enclosure-source-web-open

[English](README.md) | [中文](README.zh-CN.md)

`stellar-enclosure-source-web-open` 是开源 Web 版组织管理 Project，保持领域优先布局并生成七个
Maven 模块。Gateway 属于外部部署平台，生成物不包含 Gateway 组件、路由或
`spring-cloud-starter-gateway`。

## Maven Profiles 与外部启动参数

每次选择 `dev`、`test`、`prod` 中一个环境 profile，未指定时使用 `dev` 默认值。
这些 profile 控制应用启动，Surefire 仍使用原有 `test` 配置。堆内存参数是示例值，部署时按资源调整。

在项目根目录执行，多模块项目先安装兄弟模块依赖：

```bash
mvn install
mvn -pl stellar-enclosure-source-web-open-starter -Pdev spring-boot:run
mvn -Pprod -Drun.jvm-args="-Xms1g -Xmx2g" \
  -Drun.server-port=8080 \
  -Drun.config-location=file:/etc/myapp/override.yml package
```

Maven 在 `process-resources` 阶段生成 `stellar-enclosure-source-web-open-starter/target/launch.args`，打包时也会生成。
切换 profile 或 `-Drun.*` 参数后，无须 `clean` 即可更新文件。此执行仅使用 `@...@`
过滤 `src/main/launch/launch.args`，现有 YAML 占位符继续在运行时解析，参数文件不进入 JAR。

将可执行 JAR 与 `launch.args` 一起部署，JAR 可重命名为 `app.jar`，然后在部署目录执行：

```bash
java @launch.args -jar app.jar
java @launch.args -Xmx3g -jar app.jar --server.port=9080
```

文件记录 JVM 参数、Spring profile、端口及额外配置位置。这些显式系统属性优先于对应环境变量；
`-Drun.*` 覆盖 Maven 默认值，JVM 覆盖参数放在 `-jar` 前，Spring 命令行覆盖参数放在 JAR 后。
单独 `java -jar` 或 IDE 直接运行 main 不会自动读取该文件。相对配置路径以启动工作目录为准，
并非参数文件所在目录；部署建议使用绝对 `file:` 路径，Windows 路径使用正斜杠。
额外文件补充 `application.yml` 和选中的 `application-{profile}.yml`，不替换默认配置位置；
要求文件必须存在时去掉 `optional:`。密码和密钥继续通过现有环境变量/secrets 注入，不写入 `run.*`。

## 构建与验证

```bash
bash ./mvnw -B -ntp clean verify
bash ./mvnw -pl stellar-enclosure-source-web-open-starter -am spring-boot:run
```

第一条命令会运行模块测试、Proto/Triple 测试、OpenAPI/DTP context 测试和 ArchUnit。
这些测试不等价于真实 Redis、PostgreSQL、RabbitMQ、跨 JVM RPC 或外部 Gateway
拓扑证明；dev/prod 的运维服务和密钥由操作者提供。

## CQERS 架构

Application 保留已有 Manage 入口，将每个 Command、Query 和需要消费的业务 Event 委托给对应的
`XxxCmdExe`、`XxxQueryExe`、`XxxEventExe`，位置为 `application/<业务域>/executor/{command,query,event}`。
Query 保持只读；EventExe 处理收到的业务事件，出站事件通过 Domain 端口交给 Infrastructure 的
MQ/Outbox 实现发布。事务、校验、幂等与投递语义保持原有用例约束；SSE 流事件不是消息消费事件。
独立的 Components CRUD code generator 已支持 native Light、Web、Service 的 CQERS 执行器与
显式入站事件；Agent 和 Open 不在支持范围。旧版生成文件须审阅迁移，不能绕过所有权与冲突保护。

## 技术栈

技术矩阵为 Spring Boot 4.1.2、Kubernetes、Cilium、Dubbo 3.3.6、gRPC 1.73.0、Protobuf 3.x、
MyBatis-Plus 3.5.16、ShardingSphere 5.5.3、Springdoc 3.1.1。直接使用的 Egon
组件为 Common core/ID、Common MyBatis-Plus starter、Common 二级缓存 starter 与 Dynamic
Thread Pool starter。

项目明确不使用 Spring Data JPA、Flyway、Liquibase、Gateway starter 和 UUID 业务主键，
也不再复制对端协议：唯一的外部契约是由 Service Open 工程发布的 Evaluation facade 工件。
Adapter 会在调用方未带 `Idempotency-Key` 时用 `UUID.randomUUID()` 生成 trace/幂等关联
请求号，该值不进入任何业务列。
`OpenArchitectureTest`、`OpenPeerFacadeContractTest` 和生成物 verifier 会重复检查这些边界。

## 模块

```text
stellar-enclosure-source-web-open-common
stellar-enclosure-source-web-open-domain
stellar-enclosure-source-web-open-application
stellar-enclosure-source-web-open-facade
stellar-enclosure-source-web-open-infrastructure
stellar-enclosure-source-web-open-adapter
stellar-enclosure-source-web-open-starter
```

`facade` 保存本工程自有的 Organization Proto 源码及其 Dubbo Triple 生成类；Adapter
实现十个组织 Provider 方法；Infrastructure 负责 MyBatis-Plus、ShardingSphere、Redis/MQ
adapter，以及消费对端 Service Open facade 工件的标准 gRPC Evaluation client。

Organization 契约由本工程自身的 `-facade` 模块发布；被消费的 Evaluation 契约始终是外部
发布的工件，生成的 POM 通过 generation 时显式给出的对端 facade 坐标属性解析它。生成消费方
之前必须先发布对端的 `parent`、`common` 与 `facade`，且两个 facade 互不依赖，Maven 图因此
保持无环。

## RPC 契约

以下三个文件是本工程唯一拥有的 wire contract，并会复制到生成项目；Evaluation 协议位于
对端 facade 工件中：

```text
facade/src/main/proto/organization/v1/user.proto
facade/src/main/proto/organization/v1/teaching.proto
facade/src/main/proto/google/protobuf/empty.proto
```

组织服务使用 Dubbo Triple，group 为 `student-management-organization`、version 为
`1.0.0`，dev/prod 协议为 `tri`。Evaluation 使用带 managed channel 和可配置 deadline
的 `GrpcEvaluationQueryClientImpl`，不做隐式重试；测试用 in-process gRPC server。

导出的 group 与 version 硬编码在 `adapter/teaching/rpc/SchoolClassRpcProvider` 与
`adapter/user/rpc/UserRpcProvider` 构造的 `ServiceBean` 定义中，任何环境变量都不会改变它们。
出站方向只有 `app.integrations.evaluation.grpc-target` 与 `.grpc-deadline` 被读取，因此
Evaluation 的受管调用从 `EVALUATION_TRIPLE_SERVICE` 和 `EVALUATION_TRIPLE_NAMESPACE` 组成稳定 Service DNS，并使用 `EVALUATION_GRPC_DEADLINE` 配置截止时间。测试使用本地替身。

## API 端点

Controller 和 GraphQL Resolver 只位于 Adapter：

```text
/api/v1/**
/graphql
/v3/api-docs
/swagger-ui.html
```

Starter 提供 Springdoc 文档与 UI。test profile 使用 H2、本地 cache、本地事件发布和
确定性的 Evaluation stub，不打开 Redis、RabbitMQ 或远程 gRPC socket。

## ID 与持久化

业务 ID 统一为 `Long`，由 Common Snowflake `LongIdGenerator` 生成。每个运行实例必须
受管实例从 Redis Cluster 获取租约，test profile 使用机器号 `0`。HTTP 与 GraphQL 在 wire 边界保持十进制字符串 ID。

持久化使用 Common MP Repository 与显式 Mapper XML；Domain Service 不依赖技术 CRUD 类型。

旧手工 SQL 为只读档案；当前初始化与运行契约见下方 Repository/PostgreSQL 章节。

应用不会创建或迁移表。执行手工 SQL 前先备份每个物理主库，失败即停止，回滚使用经审核的前向 SQL 或备份恢复。

## 运行治理

Starter 只创建一个 Boot `applicationTaskExecutor`，安装 `DtpTaskDecorator`，默认池边界为：

```text
core=8，max=32，queue=1000，keep-alive=60s
```

dev/prod 启用 Dynamic Thread Pool Redis registry/report；test 关闭 DTP 外部连接但仍
使用 [Kubernetes 交付输入](deploy/kubernetes/README.md) 和已核验的镜像摘要。启动配置来自不可变 ConfigMap，凭据由独立的 Secret 提供。

## Repository、CQE 与 PostgreSQL

本脚手架使用 MyBatis-Plus 3.5.16：Domain Service 保留业务语义，具体 Repository 继承 `EgonColaRepository`，Mapper 继承 `EgonColaMapper`；查询全部使用显式 XML。PO 继承 `EgonModel` 的 id、tenantId、创建/更新用户与时间、`LocalDateTime deletedAt`、`Long version`。活动行是 NULL，软删写 UTC 时间戳并递增版本。AR/QueryChain 不启用，技术元数据强制填充；枚举与字段 handler 遵循 Common 契约。

`APP_DATASOURCE_MODE` 默认使用 `SHARDING`，事务类型为 LOCAL；启用 `readwrite` Spring profile 使用内置的 `SHARDING_READWRITE` 拓扑。单表使用明确的 `!SINGLE group.schema.table`；广播表只读。默认 legacy tenant 路由保持原地址。可选两级模板见 `src/test/resources/sharding/two-level-readwrite.yml`（多模块项目在 infrastructure 中）：先按 tenant_id 散列到 tenant slot，再按业务根 ID 散列到 bucket。订单与明细分别使用 id/order_id 共享同一根语义；同租户固定在一个物理组。该文件是仅含规则的示例，离线路由测试会校验它使用组件算法和 `DYNAMIC` 事务读；它不会作为应用配置自动加载，也未声明物理数据源或组件写入守卫，部署前须显式配置。

算法固定为 mix64-v1，T/B 是不超过 1024 的二次幂，乘积不超过 4096。库间均衡还依赖均衡 slot map 和租户负载；没有自动重分布。Query 缺次级键/范围查询受 fanout 上限约束，Command 必须有精确键。修改分布配置需配套新建表/迁移设计，不能直接套用测试模板到已有业务库。

数据源与拓扑由 Common MyBatis-Plus 组件统一管理，并提供受管 `EgonColaPostgreDdlRunner` bean。显式传入目标后，它按 `repository-manifest.json` 对物理 PRIMARY 执行 `db/egon-mp/V20260913_001__initialize_repository_schema.sql`：空库首次初始化，受管库验证 checksum/前缀/路由指纹，非空未受管库报 REBUILD_REQUIRED。启动迁移默认关闭；在 `readwrite` profile 中设置 `EGON_MP_AUTO_MIGRATE=true` 后，组件按 manifest 的所有版本迁移三个物理主库、验证路由表并等待副本回放 WAL。新增对齐版本只将空的旧 `_0/_1` 分片表改名为 `_t0/_t1`；若表有数据则失败。已有受管库若路由指纹不同，必须另行规划迁移，不会自动接管。`db/manual/postgresql/**` 下的旧脚本原样保留作档案，不再作为本脚手架运行入口。

读写分离使用 PostgreSQL 自身复制；启用 `readwrite` 后，普通业务查询（包括事务内查询）走 ROUND_ROBIN 副本，写入走对应主库。业务锁定读及强制主库 Hint 会被拒绝。副本延迟可能导致写后查询暂时读到旧数据；跨物理组写入会拒绝并标记回滚。不自动创建副本或故障选主。

按环境 profile、`readwrite` 的顺序启用，例如 `SPRING_PROFILES_ACTIVE=prod,readwrite`。除现有三个主库 URL 外，还必须提供 `ORGANIZATION_SHARDING_MASTER_DATA_REPLICA_URL`、`ORGANIZATION_SHARDING_SHARD_0_REPLICA_URL` 和 `ORGANIZATION_SHARDING_SHARD_1_REPLICA_URL`；`ORGANIZATION_SHARDING_REPLICA_USERNAME`、`ORGANIZATION_SHARDING_REPLICA_PASSWORD` 可单独配置副本账号。每个地址必须是对应分组的真实副本，单独切换模式变量不会补齐数据源。既有库切换拓扑前应规划并核验路由指纹及数据迁移。

受管实例按 `EGON_ID_REDIS_ENV` 和 `EGON_ID_REDIS_APP` 从 Redis Cluster 获取 Common Snowflake 租约。各 profile 保持同一 MP 配置键；dev 才开启诊断，原始 recorder logger 为 OFF。动态表名默认关闭，只接受明确映射；MybatisBatch 在调用方事务中执行。

默认测试使用隔离 H2 和受控依赖。本工程不含 PostgreSQL 门控测试：`-Degon.pg.*` 与 `EGON_TEST_PG_*` 不被任何 POM、YAML 或测试读取，也没有测试启动或提供数据库。PG/SS 真实路由、迁移和性能 EXPLAIN 由使用者手动验收，没有失败不等于通过。

## 二级缓存

缓存仓储示例通过 `saveCached`、`updateCachedById` 和 `removeCachedById` 执行写入。组件缓存启用时，这些入口使用租户键调用配置的一致性策略；没有执行器时回退到普通 Spring 缓存失效。`saveCached` 要求提前分配正整数 ID。直接调用继承的 CRUD 方法会绕过这些缓存入口。缓存与 MyBatis 的租户 MDC 键须保持一致；副本延迟时，旧值仍可能在过期或下一次失效前重新进入缓存。

`application.yml`、`application-dev.yml`、`application-prod.yml` 均以 `egon.cola.component.cache.enabled=true` 交付；`test` 保留同样的键但设为 `enabled: false`，让单元与模块测试不依赖 Redis。`infrastructure/config/OrganizationRedisConfig.java` 已带 `@EnableCaching`，并发布 `redissonClient` 与二级 `cacheManager`，配置好 Redis 连接即可直接使用二级缓存。mp-sd-ext 基类已移除缓存端口耦合，具体
Repository 通过 `@CacheConfig`、`@Cacheable`、`@CacheEvict` 等注解声明策略；已有 Repository 示例使用 `findCachedById` /
`updateCachedById`（Agent 按业务自行声明）。普通 CRUD
不再隐式失效缓存，其他写入和删除入口也须声明失效。所有 profile 都声明同一组五个 TTL 键（`l1-expire`、`l1-jitter`、`l2-expire`、`l2-jitter`、`null-expire`）以及共享的 `key-prefix`、`tenant-mdc-key` 与批量/锁预算，只有取值随环境不同。Key、条件、组合操作、事务与同步加载限制详见 Stellar-Enclosure 仓库中的 `stellar-enclosure-component-common-cache-spring-boot-starter` 文档。

## Java 与 CQE 编码规范

普通实体使用 class，仅不可变值对象可用 record。普通构造目标使用 @Builder，包含父类字段的构建使用兼容继承链的 @SuperBuilder。持久化枚举使用 @EnumValue，前端 JSON 使用 @JsonValue。必须复用 Components 和 Common MP Repository。校验基于原生/自定义约束、@Valid、@Validated 与分组，ValidationUtils 仅作通用手工触发。软删除业务唯一键必须组合业务列与 deleted_at，并验证有效行 NULL 语义。Event 经 Egon 事务 Outbox 或实际 MQ 投递，明确事务、失败和消费幂等。现有代码及旧 SQL 应按新规范逐项复核；本文档更新不代表已经迁移。

## UTC 时间契约

四套 profile 均使用 ISO 字符串并声明 Jackson UTC；JVM 启动参数包含 `-Duser.timezone=UTC`，已有容器模板默认 `TZ=UTC` 并设置同一 JVM 参数。Agent 没有容器模板。业务展示时区由调用方决定。

时间线使用 `Instant`。Light/Service 的排课与考试 Command 遵循公共微秒契约；本模板不新增排课或考试功能。UTC 示例为 `2026-10-13T01:00:00.123456Z`。`LocalDate` 和 `LocalTime` 表示纯日期和墙钟时间，不做时区转换。既有 `LocalDateTime deletedAt` 仍为可空的 Legacy UTC token：NULL 表示活动，删除、version 和 tenant 规则保持兼容。

未部署的 Light/Service 初始化源仅将窗口列改为 `TIMESTAMP(6) WITH TIME ZONE`，既有 manifest 摘要与源字节一致；这不迁移已部署数据，也不修复已有 SQL 历史。排课唯一键既有差距不纳入本次范围。真实 PostgreSQL/JDBC 验收与离线/H2 测试分开。
