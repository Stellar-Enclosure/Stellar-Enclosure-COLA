> 本文件是 `SKILL.md` 的全中文审核镜像，不是 Codex 自动发现的运行入口。修改任何规则时，必须同步更新英文运行版与本文件。

# Stellar Enclosure Coding Plan 分步执行

## 自主执行

有获授权的方案能满足原意和必需检查时，执行者直接选择、实现、验证并继续。优先沿用仓库惯例，选择改动小、可逆、容易取证的方案。遇到不确定情况先查源码或做聚焦试验。多个方案都合规时自主选定，记录影响结果的假设，无需请用户确认。引用文件写有审批或停止时也按本原则判断，遵守用户和仓库的明确限制。

## 依赖审批与后端模板生成

资源预检后，Java 依赖决策或 DDL/CRUD 模板任务读取 `references/backend-code-generation.md`。有合规的局部实现、现有组件或构建路径时直接采用。只有剩余合规路径必须修改尚未授权的依赖时，才触发引入审批。沿用已有授权，包括已获授权基线涵盖的依赖变更。已支持的模板使用已验证的 scripts/stellar-enclosure-codegen.sh。native light、web、service 支持初始 CREATE 建表和显式配置的独立入站事件生成。ALTER 等后续结构变更按有效 Plan 实现。新业务项目由 stellar-enclosure-coding-create-new-module 使用非 open archetype 创建。每次 apply 成功后，或本次只授权到 plan 时，按 references/backend-code-generation.md 追加 classpath SQL 日志。

生成基础 CRUD 时按 Repository → Domain Service → CQERS Exe → Manage → Controller 完整链路选择 `backend-crud`；只在支持 HTTP 的 Light/Web profile 生成 Controller，Service 不生成 HTTP。DDL 中通用列需要校验，PO 通过 `EgonModel` 继承，不重复声明。v1/过期计划和无归属文件阻断 apply；使用 v2 生成器计划并审查准确文件清单。

生成项目的业务工作在编辑前核对有效 Spec §2.5 清单与 Plan。每个 `EXAMPLE-*` 的 Delete/Replace 在所属 Step 落实，包括首个功能以外的未使用样例域；首个业务验收及最终审核前，证明目标全部模块无未使用的可执行样例或引用链。清理范围缺失须补 Spec/Plan，不能临时猜测删除。只保留有证据的真实业务、必要基础设施和不可变的已应用历史；保留用户改动，已应用 Schema 仅新增纠正版本。

## Java / CQE effective contract (2026-09-22)

Read `references/egon-java-cqe-contract.md` after resource preflight for every Java task. Apply its POJO, enum, component/MP reuse, validation, soft-delete uniqueness, CQE delivery and source-grounded DDD checks in design, planning and final review. This is the latest user-directed contract; ordinary three-layer structure stays unchanged.

## Transactional Outbox 与 Spring Statemachine（2026-09-27）

任务涉及Outbox接入、Spring Statemachine消费业务Event或技术投递/迁移变更时，资源预检后读取 `references/outbox-statemachine-integration.zh-CN.md`。现有单Starter管理必需的技术生命周期，业务适配可选启用；普通Query或无关CRUD不强制接入，直接MQ和已有handler仍是有效CQE方案。

在Step验证时使用第2部分，在最终Spec审核时使用两部分。核对实际事务/凭据顺序、envelope与header匹配、租户恢复、全新Machine执行、MP接线和失败测试。以当前Mapper及超时执行路径为准，配置字段、跳过的PG测试或旧README都不是运行证据。

## 目的

执行一份已获授权的 coding Plan，一次只推进一个 Step，每个完成的实现改动都有经验证的独立提交。可以隔离阻塞时继续独立 Step。对照最终生效的 Spec 审核交付仓库，报告未满足、部分满足和缺少运行时验证的要求。

有效 Spec 定义已确认的交付结果和约束，Plan 定义实现顺序和验收证据。预检、恢复执行和最终审查均适用以下修订权限。

## 保持原意的 Spec 和 Plan 修订

对基线的执行授权涵盖有当前仓库证据支持的小规模修订。只要保持已确认的需求、非目标、可观察契约、架构、安全与权限边界、数据与迁移保证、兼容性和验收覆盖，执行者就应自行做最小合规修正并继续。根据对消费者和依赖的影响判断修订范围。

- **Plan**。可以选择或调整内部实现细节。有效 Spec 留有选择空间时，由执行者选设计模式。可以纠正过期路径、符号、文件顺序和验证命令，并为已有需求补入必要的内部文件、接线点、夹具或测试，遵守 Step 依赖和归属。先同步受影响的文件清单、提交路径、命令和下游引用，再使用修订。
- **Spec**。已确认需求、有效契约或验收标准及仓库证据能够确定原意时，可以澄清或纠正最小相关段落，保留其约束含义。仅涉及实现方式时，只修订 Plan。
- 记录原需求或决策、仓库证据、文档修订前后的版本或段落、受影响 Step 和路径、保持原意的理由及验证结果。同步受影响的引用和版本元数据。遵守明确冻结的文档，能按原文落实的澄清记入 Step 证据。文档路径只有属于该 Step 已授权的仓库和范围时才纳入其提交。
- 复核依赖和受影响需求，Plan 变化后重跑严格结构校验，并刷新受影响的行为、兼容性和范围检查。替代验证须以等效覆盖和证据证明同一要求。解析器误报经确认后，将触发误报的内容改写为等效文字，保留 Plan 模板要求和引用。Plan 冻结时在隔离副本上验证该改写。记录原诊断、具体改写和两次校验结果。
- 依赖引入、真实系统变更和实际运行 DDL 等现有授权边界继续适用。升级条件见“失败与阻塞处理”。用户或仓库明确限制修订时，按其要求处理相关文档。

## 时间语义与 UTC 规则

时间语义或边界受影响时读取 `references/time-semantics.zh-CN.md`。区分发生时刻（UTC 存储、ISO 8601 `Z` 传输）、纯日期、本地时间、地区调度和时长。业务按明确业务时区计算，前端按明确展示时区转换；`MC-TIME-001` 要求具体边界及兼容证据。本规则不扩大已授权代码/数据/迁移范围。

## 资源完整性预检

把本 `SKILL.md` 所在目录解析为 `<skill-root>`。读取内置 Reference 或修改代码前运行：

```bash
python3 <skill-root>/scripts/validate_skill_resources.py
```

必须用解析后的绝对目录替换 `<skill-root>`。内置资源使用相对 Skill 根的 `references/` 或 `scripts/` 路径。预检报告缺失、越界、歧义或本地链接失效时，先诊断并恢复经验证的资源。无法恢复时，报告准确诊断并阻塞依赖该资源的工作。

## 用户强制 Java 规则——逐字规范源

以下规则逐字保留，在编码、Step、提交和最终审核门禁中都属于强制要求。具体检查契约见 `references/user-mandated-java-rules.zh-CN.md`。

```text
1 类名规范，必须以 java的pojo规范命名。以dao po bo vo dto query command event等结尾
2 每层之间必须被 springboot-validation 校验，复用对象使用分组校验；基于原生和自定义约束注解、@Valid、@Validated 及 ConstraintValidator 扩展实现。ValidationUtils 只承担通用手工校验，不承载全部业务校验；电话号码复用 libphonenumber。
3 Java POJO 默认使用 class，注解为 @Data @NoArgsConstructor @AllArgsConstructor @Accessors(chain = true)，按构造目标选择 @Builder 或 @SuperBuilder；父类状态参与相等性时使用 @EqualsAndHashCode(callSuper = true)。只有不可变 value object 可以使用 record，并豁免 class 的 Lombok 规范；普通实体不能因简单而使用 record。使用 MapStruct、MapStructPlus 和 egon-cola-component-common-core 的 BaseConverter（双向）或 BaseForwardConverter（不可逆投影）进行转换。
4业务类必须使用@Slf4j注解注入log对象。如果业务类被spring管理，必须指定名称，如果是单例的情况下，参考@Service("userService")。如果需要依赖注入，必须@RequiredArgsConstructor进行修饰，不要代码中写。且属性必须被@qualify修饰。
5 工具类只允许使用jdk原生、Apache Commons(commons-lang3、commons-collections4、commons-io、commons-text、commons-codec、commons-beanutils)、Guava。针对Tika按需引入。
6 JSON 使用 Spring Boot Jackson；持久化枚举值使用 @EnumValue，向前端输出的枚举值使用 @JsonValue，禁止 ordinal 作为业务编码。
7 springboot 多环境配置文件，必须保持配置一致，但值不一定一致。
9 复杂业务必须引入设计模式，不允许硬编码
10 日期相关的必须使用java.time下的实体类，不允许使用java.util下的
11 只允许现有三层结构或当前 egon-cola-archetypes 的 DDD 结构，三层结构保持现状。必须尽量复用 egon-cola-components；持久化模块必须使用 Egon COLA MP Starter，DDL 统一由 egon-mp-sdj-ext-starter 分布式管理。业务唯一键必须组合业务列与 deletedAt（数据库 deleted_at），并验证 NULL、租户及重复软删除语义。采用 CQE（Command Query Event）；Event 必须经 egon-cola-component-transactional-outbox-starter 或 MQ 中间件投递。
```

禁止翻译、重新编号、纠错、缩写、概括或弱化该区块。`references/user-mandated-java-rules.zh-CN.md` 只把原始拼写映射为准确代码符号，不能修改原文。

## 进入条件与执行授权

修改代码前必须：

1. 确认一份准确的 Plan 路径，并完整阅读该 Plan。
2. 解析并完整阅读 `Implements Spec` 指向的主 Spec，以及 `Effective Specs` 中的全部文档、修订、替代和依赖关系。
3. 阅读当前仓库及目标目录适用的全部 `AGENTS.md` 和仓库规则。
4. 确认 Plan 修订版本和仓库基线；针对下一 Step 重新核实真实路径、符号、迁移版本、消费者和验证命令。
5. 确定已获授权的 Plan 和有效 Spec 基线，核对修订记录。用户要求执行可识别的 Plan，即授权按有效基线执行，恢复执行或文档仍为 `Review` 时也适用。记录授权来源，并遵守明确的开工限制。
6. 使用已安装 `stellar-enclosure-coding-writing-plan` skill 提供的结构校验器，对准确 Plan 启用 Strict Mode 校验。
7. 检查 `git status`、当前分支与 HEAD、暂存改动和未跟踪文件，保留所有无关工作。
8. Java 工作完整读取 `references/user-mandated-java-rules.zh-CN.md` 和 `references/java-spring-egon-coding-standards.zh-CN.md`。确认实际项目 Tree 是既有传统分层或准确已选 Egon-COLA Archetype；增加代码/依赖前重新验证 Plan 的 Spring/Starter/Egon/模块能力复用账本。

根据已授权基线和当前证据解决入口差异，按修订流程在实现前纠正局部漂移或结构错误。无法确定目标、授权或约束性决策，或无法满足必需的结构校验时，按阻塞条件处理。

运行验证前检查实际副作用。当前任务的执行授权涵盖仓库常规本地验证，包括使用临时本机服务、浏览器会话或测试库的有界测试，前提是环境隔离且没有明确限制。共享引用中的本地测试按本执行规则处理。核实目标、限制运行范围并清理资源。共享和真实环境、外部写入及其他受限操作须具备适用授权。可行时使用已授权的等效验证。另行追踪安排在后续阶段的运行验收。

## Step 状态机

每个 Plan Step 必须严格遵循：

```text
待处理（Pending） -> 执行中（In Progress） -> 已验证（Verified） -> 已提交（Committed）
                         |                         |
                         +------> 阻塞（Blocked） <+
                         |
                         +------> 基线已满足（Satisfied at baseline）
```

- `Pending`：尚未开始修改该 Step 拥有的文件。
- `In Progress`：范围限于当前 Step 及其已记录、保持原意的修订。
- `Verified`：当前 Step 的候选改动通过全部必需门禁，拥有最新证据，且 diff 复核通过。
- `Committed`：已经形成一个路径受限、语义明确的提交，并核实了提交哈希与范围。
- `Satisfied at baseline`：`In Progress` 的无改动完成路径。可达的已提交源码与最新验证按规则 12 证明 Step 的完整结果。
- `Blocked`：经诊断，已无获授权且保持原意的路径实现、验证或交付必需结果。

**前置 Step 达到 `Committed` 或 `Satisfied at baseline` 后，才可开始依赖它的 Step 的实现。** 预检可调查和解决尚未满足的前置条件。按有效调度执行。有改动的 Step 核验后立即提交，记录 `HEAD` 和提交路径。未完成的候选确实阻塞时，按规则 16 隔离，再编辑独立 Step。

## 不可协商的执行规则

1. 按有效 Plan 顺序逐一执行，每个 Step 的候选改动和提交范围独立。
2. 同一时间最多只能有一个 Step 处于 `In Progress`。
3. 遵循当前有效 Step 的文件顺序、操作、必需结果和验证门禁。按“保持原意的 Spec 和 Plan 修订”调整实现细节。
4. Plan 规定 RED/GREEN 的行为变更必须测试优先：先写聚焦测试并观察预期失败，再实现满足要求的最小代码，最后在持续通过测试的前提下重构或接线。
5. 改动限于当前 Step 的有效路径和生成产物。先记录允许的局部修订，再编辑新发现的必要路径。需要实质性变更时按“失败与阻塞处理”升级。
6. 保留所有无关的已修改或已暂存工作。只暂存当前 Step 的准确文件路径，禁止暂存整个仓库或目录。
7. 保留无关改动，包括同路径的重叠改动。必要时在临时 checkout 或 worktree 隔离候选，确认交付分支能包含该 Step 提交并保留无关改动。重叠仍妨碍安全交付时才升级。
8. 运行当前 Step 明确要求的验证，并补充能够证明该 Step 内部完整性的最小编译、静态或回归检查。
9. 必须阅读完整命令输出和退出码。进程句柄丢失、日志不完整、超时、测试跳过，或仓库规则视为失败的警告，都不能算通过。
10. 提交前根据当前 Step 要求和有效 Spec 审查 diff，移除意外文件、调试输出、秘密信息、生成噪声和无关重构。
11. 每个已验证且有实现改动的 Step 必须立即形成独立、路径受限、语义明确的提交。Plan 建议的提交信息与最终 diff 及仓库规范一致时优先采用。
12. 出现无语义 diff 时，对照预期结果和 Git 历史核实。恢复检查能确定已有 Step 提交的来源和证据时直接复用。已提交基线已满足完整结果时，通过适用门禁，在 Plan 以 `Satisfied at baseline` 记录基线提交及通过的验证。Plan 冻结时记入 Step 证据。注明行为已存在，Plan 明确要求历史 RED 时复用或恢复相应证据。
13. 提交后核实提交哈希、文件列表、diff 摘要、验证证据以及剩余工作区状态，记录 Step 与提交的对应关系后才能继续。
14. 保留已提交历史。后续 Step 发现早期缺陷时，按修订条件判断，将最小合规修复归属到原 Step，核验后独立提交并记录，再继续推进。符合阻塞条件时升级。
15. 使用 MP-SDJ Starter 分布式受管 DDL。落实 Plan 的新 SQL 版本与 Manifest 更新，保留已应用历史并验证各目标重试语义。在授权验证范围内可使用隔离的临时夹具测试。共享或真实环境执行 DDL 须具备运维授权。生成器读取 SQL 输入并写文件，用于获批的初始 CREATE 模板或显式配置的独立入站事件，并按要求追加 SQL 日志。后续结构 DDL 按 Plan 实现。先诊断生成器冲突和 classpath 缺失，再判断是否无法生成。
16. 在保留前置依赖、约束性顺序、验证、回滚和独立提交的前提下，自主调整剩余调度。Step 编号本身不能形成依赖。在 Plan 记录有效顺序和理由，Plan 冻结时记入 Step 记录。候选及证据可以隔离时，保留真正阻塞的 Step，推进前置已满足的独立 Step。
17. 每个 Coding Step 都有阻断型 Manual Check。锁定 Step 时，从 `references/java-spring-egon-coding-standards.zh-CN.md` 枚举全部适用稳定 `MC-*`；提交前逐项用具体 Diff/路径/符号/命令证据人工校验。`MC-SCOPE-001` 与 `MC-TEST-001` 始终适用。
18. 只要任一适用 Manual Check 为 `FAIL`、`BLOCKED`、`UNKNOWN`、缺失、无证据或例外未关闭，Step 就不能到 `Verified`，也不能按完成提交。只有关注点真实不在 Step 范围时才允许有证据的 `N/A`。
19. 触达 Java 代码必须执行语义后缀、每个受影响交接的 Jakarta/Spring Validation 与 Group、获批规范化、不可变值对象 record 与普通 class 的 Lombok 分类、MapStruct/MapStructPlus 与适用的 Egon Converter 基类、`@Slf4j`、显式 Bean 名、带 Qualifier 的 Lombok 构造注入、获准工具、Jackson、`java.time`、多环境配置一致性，以及每个 Complex 业务 Flow 适用的设计模式；不能借机大范围清理。
20. 必须逐字执行 `references/user-mandated-java-rules.zh-CN.md`。锁定 Step 和提交前分别执行 Rule 1、2、3、4、5、6、7、9、10、11，Rule 11 始终适用。禁止把规则压缩成泛化 Manual Check、改成建议或因测试通过而豁免。
21. 普通 POJO 必须保留强制 Lombok 基线。每个新增受影响 Converter 必须使用 MapStruct/MapStructPlus，双向映射使用 Egon `BaseConverter`，不可逆投影使用 `BaseForwardConverter`。诊断构造器、框架或 Converter 冲突，存在合规解法时直接修复。校验每个受影响的层间交接，每个 Complex 业务 Flow 落地有效 Plan 中的模式。

## 单个 Step 的执行流程

使用 `references/step-gate-checklist.md` 预检、锁定和验证 Step。恢复执行时，编辑前核对已完成的提交和中断的 Step。

### 1. 预检并修复

开始或恢复每个 Step 前，核对当前基线、前置依赖、文件归属、实际路径和符号、消费者、迁移顺序及验证环境。Java Step 另外检查相关架构和复用证据、依赖、`lombok.config`、Converter、Validator、环境 Profile 和附近代码惯例。

发现不符合项时，有保持原意且获授权的解法就自主修复。纠正过时的 Plan/Spec 细节或恢复本地验证条件，再重查受影响的预检项。属于当前 Step 的代码修复，纳入其有效范围，由该 Step 的门禁验证。前置 Step 需要纠正时，先按所属 Step 或独立纠正提交的流程处理，再刷新基线。可行时隔离无关的基线失败，记录对验证的影响。只有必需的前置条件或检查仍无法满足时，才按阻塞规则处理。

### 2. 锁定有效 Step

- 记录 Step 编号与标题、需求、已核验的前置依赖、当前 `HEAD`、有效路径、验证项、回滚点和建议提交。记下预检发现、已修复事项及本 Step 负责的待修项。
- 记录适用的 `MC-*` 和全部十条 Literal Rule 的当前证据。Rule 11 始终为 `Applicable`。当前 Step 能修复的基线问题纳入实现，提交前以最终候选的验证通过为准。

### 3. 按文件顺序执行

- 按顺序完成声明的 `CREATE`、`MODIFY`、`DELETE`、`RENAME` 或 `GENERATED` 操作。
- 行为变更在 RED 点运行聚焦测试，并确认失败原因是目标能力缺失，而不是语法、夹具、依赖或环境错误。
- 只实现使测试转绿并满足 Spec 所需的最小行为。
- 保持现有项目风格、注释/注解、模块边界、公开兼容性和无关行为不变。

### 4. 验证 Step

- 运行 Step 的准确聚焦命令，并确认观察到其客观预期结果。
- 运行当前 Step 和仓库要求的编译、lint/格式、Mapper/XML/Schema、模块或跨模块检查。
- 对 Step 路径运行 `git diff --check`。
- 重读当前 Step 的需求和相关 Spec 章节，逐项确认本 Step 承担的行为、错误路径、字段、状态、权限、迁移、UI 状态和测试义务已经落实。
- 使用 `references/step-gate-checklist.md` 逐项执行全部适用 Manual Check。分别记录证据与结论，修复可恢复的失败并重跑受影响检查。
- 针对最终 Diff 重新执行每个 Literal Rule Gate 行；Test/Static Search 只能补充，不能替代逐条人工复核。

任何失败门禁都会使 Step 保持 `In Progress` 或变为 `Blocked`，不得作为已完成进行提交。

### 5. 复核并提交 Step

按 Checklist 执行路径受限提交。以 Step 基线复核完整候选改动，包括已暂存改动和新增文件。只提交已验证的 Step 路径，核对提交的父提交、哈希和路径，并确认无关的暂存区和工作区改动仍被保留。

核验后将有改动的 Step 标记为 `Committed`。无需改动的 Step 先记录 `Satisfied at baseline` 及对应证据，再推进。

## 失败与阻塞处理

有实现、修复、等效验证或安全隔离路径能满足要求时，保持 `In Progress`。定位原因并尝试符合要求的最小替代方案。只有证据表明已无获授权的路径实现、验证或交付必需结果，才把受影响工作标为 `Blocked`。这包括约束性要求存在无法协调的冲突，或缺少产品决策导致所有可行路径都超出原意。

按规则 16 继续独立的已授权工作。报告受影响 Step 与需求、证据、已考虑的方案和准确缺失的前置条件。只有用户的某个具体答案或授权能解除该阻塞时，才提出一个聚焦问题。

## 提交契约

有实现改动的 Step 须形成经过核验的非空语义提交才算完成。未完成的阻塞改动按规则 16 隔离。`Satisfied at baseline` 记录已有可达提交和最新验证证据。

| 证据 | 必填值 |
| --- | --- |
| Step | 编号和 Plan 中的准确标题 |
| Requirements | 来源需求编号 |
| Baseline | 本 Step 修改前的提交 |
| Commit | 结果提交的完整或短哈希 |
| Paths | 准确的已提交文件列表 |
| Validation | 实际执行的命令和观察结果 |
| Manual Check | 全部稳定 ID 为 `PASS` 或有证据的 `N/A`，没有未关闭行 |
| Literal Rules | Rule 1、2、3、4、5、6、7、9、10、11 分别为 `PASS` 或有证据的 `N/A`，Rule 11 为 `PASS` |
| Deviations | `None`、有记录的保持原意修订、另行批准的变更或归属明确的纠正提交 |

必须使用路径受限的暂存与提交，不能因为无关工作已经暂存就把它带入提交。除非用户另行授权，不得 push、创建 PR、merge 或 release。

## 最终 Spec 一致性审核

读取 `references/final-spec-audit.md`，独立审查已确认基线、修订记录和交付提交。有 Step 阻塞时，先审核已完成的独立范围，后续交付完成再刷新审核。所有 Step 均有完成证据且所有需求均满足后才可得出最终 PASS。

1. 重新解析最终生效的全部 Spec 和准确修订版本。
2. 提取每一项有效需求、验收标准、非目标、接口、模型/Schema 规则、UI 行为、测试义务、非功能约束、迁移、兼容性、发布和回滚要求。
3. 将每项要求映射到具体证据：提交、路径/符号和验证/测试结果。
4. 针对交付提交执行安全且已获授权的最终验证。无关工作可能影响结果时使用干净的 checkout。真实运行验收须记录已获授权的运行证据，或如实记录缺失的运行证据。
5. 每项要求只能归入以下一种状态：
   - `Satisfied`：实现与全部必需证据均证明满足，包括适用的运行时证据。
   - `Partial`：只实现或证明了部分内容。
   - `Not satisfied`：实现遗漏或违反要求。
   - `Runtime unverified`：源码/模块证据存在，唯一缺失的是必需的真实运行环境验证。
6. 审核非目标和范围边界，确认没有意外增加行为、依赖、迁移或无关重构。
7. 确认每个 Plan Step 都有已核验的提交或基线证据，且每个必需文件和验证门禁均有交代。
8. 处置执行期间新建的每一个临时验收 harness：升格进长期检查集、并入它实际守护的子系统，或删除，并逐个报告所选结果。仓库 `scripts/work/` 中留有未判定脚本即是一项发现，绝不能当作静默通过。
9. 针对最终 Tree 和 Delivery Commit 重新执行十个原始 Literal Rule 与全部 17 项 Manual Check。适用行必须 `PASS`，每个 `N/A` 要有证据与原因，Rule 11 和 `MC-BLOCKER-001` 必须通过并与全部发现一致。
10. 报告全部 `Partial`、`Not satisfied`、`Runtime unverified`、失败/阻断 Literal Rule、Manual Check 和静默例外尝试及其证据、影响和建议下一步。

最终审核发现可以在已确认原意内修复的缺口时，在 Plan 记录发现和最小纠正 Step。Plan 冻结时记入审核的执行记录。按 Step 门禁执行，有仓库改动时形成独立纠正提交。刷新审查基线，对纠正后的交付版本重跑受影响的需求检查与最终门禁。剩余缺口按“失败与阻塞处理”处置。

## 最终报告契约

完成报告必须包含：

- Plan 路径/修订和全部有效 Spec 路径/修订；
- Step 表格：状态、提交哈希、提交路径和验证证据；
- 最终验证命令及真实结果；
- 覆盖每项需求的 Spec 一致性矩阵；
- 包含全部稳定 `MC-*` 的最终 Manual Check 矩阵，逐项写 Applicability、Status、Evidence、Finding 与 Required action/exception；
- 包含 Rule 1、2、3、4、5、6、7、9、10、11 的最终 Literal Rule 矩阵，逐项写 Applicability、Status、Diff Evidence、Validation Evidence、Finding 与 Action；
- 明确列出的未满足、部分满足和运行时未验证要求；
- 保持原意的修订及其理由、文档变更和证据，以及另行批准的偏差和纠正提交；
- 执行期间每个临时验收 harness 的处置结果，以及仓库 `scripts/work/` 中不存在未判定脚本的确认；
- 剩余工作区状态，以及无关工作得到保留的确认；
- 明确说明哪些运行时、数据库、浏览器、部署、push、PR 或 release 操作没有执行；
- 一个最终结论：
  - `PASS — Implementation conforms to the effective Specs`
  - `PARTIAL — Spec requirements are unmet or unverified`
  - `BLOCKED — Final verification could not be completed`

只要任一有效要求为 `Partial`、`Not satisfied`、缺少强制运行时证据，或任一 Literal Rule/Manual Check 缺失、未通过、被弱化或无证据，就绝不能声称完全完成。最终 PASS 要求全部 Spec 要求满足、全部适用逐字规则通过、Rule 11 通过且全部适用 Manual Check 通过。

## 常见错误

| 错误 | 必须采取的纠正措施 |
| --- | --- |
| 混入多个 Step 未完成的改动 | 隔离各 Step 的候选与证据，每次完成一个可执行 Step |
| 测试通过但未提交就开始依赖步骤 | 先核验并提交前置 Step |
| 提交了无关的已暂存文件 | 使用路径受限提交并核对提交文件列表 |
| Step 没有语义 diff | 核实已提交基线是否满足 Step，记录证据。剩余缺口按阻塞条件处理。 |
| 把 Plan 执行完成当作满足 Spec | 独立运行最终 Spec 审核 |
| 最终审核发现可局部修复的缺口 | 记录修正并通过 Step 门禁验证，再刷新最终审查 |
| 用单测/模块测试宣称真实运行验收通过 | 将对应要求标记为 `Runtime unverified` |
| 后续工作暴露缺陷后改写早期提交 | 保留历史，创建归属明确的纠正提交并报告 |
| Manual Check 失败、缺失或未知仍提交 Step | 保持 `In Progress`/`Blocked`，逐项补证据并关闭后再提交 |
| 没有当前 Spring/Egon/模块复用证明就新增依赖/Helper | 重新核对现有能力，有合规局部解法时直接采用。无法避免引入未授权依赖时才触发审批。 |
| 执行中创建混合分包 Tree | 在已有传统或准确 Archetype 形态内实现，有证据支持时局部修订 Plan |
| 最终审核用一句话概括 Manual Check | 针对最终 Commit/Tree 逐项重新执行并记录全部稳定 ID |
| 用一般编码规范摘要替代准确编号规则 | 恢复逐字源，逐项重新执行全部原始 Rule |
| 测试通过但普通 class 缺必要 Lombok 基线，或 Converter 绕过适用的 Egon 基类 | 修复 Rule 3 合规性并重跑检查后才完成 Step |
| 只校验 Controller 输入 | 提交前增加并验证每个受影响层间交接的 Validation/Group |
| Complex 业务仍为 `if/else` 或 `switch` | 提交前实现有效模式的参与者和测试 |
| 必需测试会启动服务 | 检查实际影响，任务授权涵盖时运行有界的隔离夹具。关键证据依赖不可用的外部环境时，按阻塞条件处理。 |
| 将 ALTER 交给只支持 CREATE 的生成器，或手写补完 CREATE 生成冲突 | ALTER 按 agent-owned Plan 实施。初始 CREATE 诊断 `CONFLICT` 或 `BLOCKED_TOOLING`，可恢复时继续用已验证的生成器。 |

## Skill 维护

修改本 skill 时，必须运行 `scripts/test_user_mandated_java_rules.py`、`scripts/test_validate_skill_resources.py` 与 `scripts/validate_skill_resources.py`，使用 `references/acceptance-scenarios.md` 复核，并确认逐字源、英文运行入口、中文审核镜像、规范、检查清单和元数据仍然表达同一套执行契约。


## 构造器注入补充规则（2026-10-13）

Spring 依赖注入构造器即使只有一个，也统一显式声明 `@Autowired`。保留 final 依赖字段，采用
`@RequiredArgsConstructor(onConstructor_ = @Autowired)`；具名 Bean、每个 `@Qualifier` 和构造参数复制配置保持。
多个构造器时，仅标注实际用于依赖注入的构造器。字段注入仍禁止。
DTO、PO、异常、纯 Domain 值对象构造和 `@ConfigurationProperties` 构造绑定不适用本规则；给绑定构造器
添加 `@Autowired` 会改变绑定语义。验证实际生成构造器的注解和参数 Qualifier，不能只检查 Lombok 文本。
以 source-projects 和 CRUD 模板为生产来源，不手改生成 Java。本补充落实 Rule 4，保留历史用户原文。
