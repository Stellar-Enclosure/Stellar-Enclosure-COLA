# Stellar-Enclosure-COLA

Application architecture templates, Maven archetypes and coding workflow skills for Stellar Enclosure. All first-party modules start at **0.0.1**.

## 功能概览

| 能力 | 当前实现与入口 |
|---|---|
| 轻量业务工程 | `light`、`light-open` 为单 Maven 模块，按业务域组织包，适合不需要拆分部署模块的应用。 |
| 服务工程 | `service`、`service-open` 提供七模块工程，包含独立 Facade 合同和 RPC/MQ 适配；不承担业务 HTTP Controller。 |
| Web 工程 | `web`、`web-open` 提供七模块工程，包含 HTTP/GraphQL/RPC/MQ 入口与跨服务调用示例。 |
| Agent 工程 | `agent` 提供六模块工程，包含 Deep Research SSE、Knowledge API、Agent Flow、RAG 和 Outbox 集成。 |
| Archetype 生产 | 从正常源工程与打包定义生成七族 archetype，支持确定性检查、边界检查与生成工程的 Groovy verifier。 |
| 编码工作流 | `.agents/skills/stellar-enclosure-coding-*` 提供建模块、需求规格、实施计划和执行计划四个技能；Spec/Plan/Execute 内置离线 CRUD codegen 启动脚本。 |

Native 与 Open 是已有集成边界不同的工程族，具体协议和依赖以所选源工程为准。模板与组件库分别维护：本仓库拥有源工程、打包规则和技能，实际通用运行时能力来自 [Components](https://github.com/Stellar-Enclosure/Stellar-Enclosure-Components) 及各平台 Starter。生成成功只证明模板打包与检查结果；现有实现与目标编码规范的差异见[架构总览](docs/architecture-overview.md)和[编码规范](docs/coding-standard.md)。

## Build

Requires Java 21 and the included Maven Wrapper. Once the 0.0.1 dependencies are published:

```bash
./mvnw -B -ntp test-compile
```

Before the first Central publication, clone the eight Java repositories next to one another and bootstrap their artifact graph explicitly:

```bash
bash ../Stellar-Enclosure-Parent/scripts/bootstrap-workspace.sh \
  --workspace "$(cd .. && pwd)" \
  --local-repository "$HOME/.m2/repository"

# Resolve subsequent Maven commands and generator subprocesses from this same cache.
export MAVEN_ARGS="${MAVEN_ARGS:-} -Dmaven.repo.local=$HOME/.m2/repository"
```

The bootstrap installs parent/BOM models before building components and business starters in Maven reactor order. It compiles test sources and installs packages with runtime tests disabled. It never runs applications, databases, containers, browsers, or deploy.

## Skills and archetypes

The four skills are under `.agents/skills/stellar-enclosure-coding-*`. Their resources and original normative rule blocks remain included. `.agents/skills/scripts/link-skills.sh` supports explicit links to other checkouts; its regression test is beside it.

Seven authoritative archetype families live under `source-projects` and `definitions`. Never edit or commit `.generated`. After bootstrap:

```bash
./scripts/generate_archetypes.sh generate
./scripts/generate_archetypes.sh check
```

The Spec, Plan and Execute skills each bundle `scripts/stellar-enclosure-codegen.sh` inside their own skill directory. Invoke it from the resolved skill root so it also works in linked or copied skills. Its implementation is the Components artifact `top.egon:stellar-enclosure-component-code-generator:0.0.1`; the existing explicit `EGON_CODEGEN_CLASSPATH` contract is preserved.

本仓库维护七个 Maven Archetype 产品：可直接编译、测试的正常源工程经两段式生成，打包成发布到 Maven Central 的 archetype。根 `pom.xml` 是 `stellar-enclosure-archetypes-parent`，直接继承 `stellar-enclosure-parent`。`source-projects/`、`definitions/`、`docs/` 和 `.generated/` 均位于仓库根目录。

## 目录结构

| 路径 | 内容 | 是否手改 |
|---|---|---|
| `source-projects/stellar-enclosure-source-<family>/` | 七个正常源工程：Java、资源、测试、模块 POM，以及随生成工程分发的 README | 是，这里是事实来源 |
| `definitions/stellar-enclosure-archetype-<family>/` | archetype 打包描述、`verify.groovy` 集成测试、随生成工程分发的策展文档 | 是 |
| `.generated/` | 由 `scripts/generate_archetypes.sh generate` 派生的打包输入，已被 gitignore | 否，永远不要手改 |
| `docs/` | 维护者文档（见下文），不随生成工程分发 | 是 |

七个 family 为 `light`、`light-open`、`service`、`service-open`、`web`、`web-open`、`agent`。各产品的形态、模块与直接依赖见[架构总览](docs/architecture-overview.md)。

## 维护者文档

| 文档 | 语言 | 内容 |
|---|---|---|
| [docs/architecture-overview.md](docs/architecture-overview.md) | English | 七个产品的形态选择、实际直接模块依赖、层职责、持久化与验证边界 |
| [docs/architecture-diagrams.md](docs/architecture-diagrams.md) | 中文 | 业务层依赖、持久化调用、CQE 事务事件、数据库生命周期四张 Mermaid 图 |
| [docs/coding-standard.md](docs/coding-standard.md) | 中文 | Java、组件复用、校验、软删除唯一键、分布式 DDL 与 CQE 的目标编码规范，以及当前实现与规范的已知差异 |
| [docs/open-archetype-code-style.md](docs/open-archetype-code-style.md) | English | 三个 Open 产品在通用编码规范之外的补充约定 |
| [docs/generated-readme-contract.md](docs/generated-readme-contract.md) | 中文 | 生成工程 README 的统一章节骨架，以及被 `verify.groovy` 断言冻结、改文档时必须保留的子串 |

## 会离开本目录的文档

下列文件不只给本仓库维护者看，修改时要按读者和去向来写：

| 来源 | 去向 | 读者 |
|---|---|---|
| `source-projects/*/README.md`、`README.zh-CN.md` | 生成业务工程的根目录 README，`${rootArtifactId}` 等变量被替换 | 业务工程开发者 |
| `source-projects/*/deploy/container/README.md` | 生成业务工程的容器交付说明（不做 Velocity 转义） | 业务工程开发者 |
| `source-projects/*/**/db/manual/postgresql/README.md` | 生成业务工程的归档 SQL 说明（仅 Open 族） | 业务工程开发者 |
| `definitions/*/src/main/javadoc/README.md` | 打进 archetype 发布制品的 javadoc classifier | archetype 使用者 |
| `definitions/*/architecture-docs/architecture.md` | 复制到 `.generated/` 中 archetype 目录旁，供 `verify.groovy` 读取；**不进入**生成业务工程，也不在发布制品里 | 维护者、集成测试 |

修改前三类文件前先读 [docs/generated-readme-contract.md](docs/generated-readme-contract.md)：README 有统一的章节骨架，且大量子串被集成测试逐字断言，改错一个字就会让 archetype IT 失败。`architecture.md` 同样有被断言的关键字，见该文档最后一节。

## 修改与验证

改源工程或策展文档后，按顺序运行：

```bash
./scripts/generate_archetypes.sh generate
```

```bash
./scripts/generate_archetypes.sh check
```

```bash
python3 scripts/checks/check-archetype-family-boundaries.py
```

```bash
python3 scripts/checks/check-native-archetype-boundaries.py
```

```bash
./mvnw -o -f pom.xml -Pgenerated-archetypes integration-test -DskipTests=false
```

最后一条会为七族各生成一个业务工程并运行其 `verify.groovy`，耗时较长；只改动单个 family 时可加 `-pl :stellar-enclosure-archetype-<family>` 限定范围。生成器和静态检查通过不代表应用、数据库或容器运行验证通过。

`scripts/` 只保留 archetype 维护工具：`generate_archetypes.sh` 生成并检查打包资源，`checks/` 校验 Native/Open/Agent 边界，`regression/` 验证生成器安全性及 Groovy verifier。生成器回归使用 `bash scripts/regression/test-generate-archetypes.sh unit`；两个 verifier 回归脚本需要显式传入已构建的生成工程和 Groovy classpath，可用 `--help` 查看参数。

## Publish to Maven Central

This repository publishes its root archetype parent, seven generated archetypes and four peer facade contracts needed by generated projects. The generated reactor wrapper and all other source-project examples are excluded. Dependencies from Parent, Components and the platform Starters must already be published.

Configure Maven server `central` with your Central Portal token and a local GPG key, as described in the [release guide](https://github.com/Stellar-Enclosure/Stellar-Enclosure-Parent/blob/main/docs/first-release.md). Run from this repository:

```bash
(
set -e
./scripts/generate_archetypes.sh generate
./scripts/generate_archetypes.sh check

# Publish the parent once. It must retain its dependency/plugin management.
./mvnw -B -ntp -N -f pom.xml -Prelease clean deploy

# Only the four facade contracts use flattened consumer POMs.
for family in service web service-open web-open; do
  ./mvnw -B -ntp \
    -f "source-projects/stellar-enclosure-source-${family}/stellar-enclosure-source-${family}-facade/pom.xml" \
    -Prelease,publish-resolved-facade clean deploy
done

# Select the seven archetypes without publishing the parent/facades again.
./mvnw -B -ntp -f pom.xml \
  -Prelease,generated-archetypes \
  -pl :stellar-enclosure-archetype-agent,:stellar-enclosure-archetype-light,:stellar-enclosure-archetype-light-open,:stellar-enclosure-archetype-service,:stellar-enclosure-archetype-service-open,:stellar-enclosure-archetype-web,:stellar-enclosure-archetype-web-open \
  -Darchetype.test.skip=true clean deploy
)
```

For a local release rehearsal, run the same Maven commands with `verify` in place of `deploy` and add `-Dgpg.skip=true`; use the Build section's bootstrap if dependencies are not yet published. Keep `publish-resolved-facade` limited to the facade commands: flattening the parent would remove management required by its consumers. Stop on any failed publication, check which GAVs reached Central, and publish only the remaining modules or use a new version. Generated output stays ignored by Git. Pushes do not publish.

## Compatibility and publication

Java packages/classes, Bean names, configuration keys, OAuth/RPC identities, APIs, and database ownership are retained. Existing SQL and DDL history are copied byte for byte. Artifact/build versions and necessary build paths change. A successful build does not imply application startup or live infrastructure acceptance.

Git pushes run validation only. Central and npm publication are separate user actions. CI defaults to initial source bootstrap; after publication set `STELLAR_USE_SOURCE_BOOTSTRAP=false` to consume published artifacts directly. Private organizations may supply the Actions secret `STELLAR_ENCLOSURE_READ_TOKEN` with repository read access.

Original MIT/LGPL licenses and author attribution are preserved.
