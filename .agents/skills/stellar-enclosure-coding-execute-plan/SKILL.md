---
name: stellar-enclosure-coding-execute-plan
description: Autonomously execute an authorized coding Plan with intent-preserving revisions, verified Step commits, Java/Spring/Egon-COLA checks, and a final Spec audit. Ask only when no authorized path can satisfy a required outcome.
---

# Stellar Enclosure Coding Plan Execution

## Autonomous execution

When an authorized approach satisfies the accepted intent and required checks, choose it, implement it, verify it, and continue. Prefer existing conventions, the smallest reversible change, and practical evidence. Resolve uncertainty with source inspection or a focused experiment. If several approaches qualify, choose one and record consequential assumptions without seeking confirmation. Apply this rule when a bundled reference describes an approval or stop, while honoring explicit user and repository restrictions.

## Dependency approval and backend template generation

After resource preflight, read `references/backend-code-generation.md` for Java dependency decisions or DDL/CRUD scaffolding. Choose a compliant local solution or existing component/build path when available. Apply the dependency introduction gate only when the remaining compliant path requires an unauthorized dependency change. Reuse applicable approvals, including dependency changes covered by the authorized baseline. For supported templates use the verified generator at scripts/stellar-enclosure-codegen.sh. On native light, web, and service, it writes initial CREATE-based scaffolding or explicitly configured standalone inbound events. Implement later structural changes such as ALTER under the effective Plan. Create a new business project through stellar-enclosure-coding-create-new-module using a non-open archetype. After each such apply, or after a plan that is the last authorized generator action, append the classpath SQL journal required by references/backend-code-generation.md.

For native CRUD, `backend-crud` uses the full Repository → Domain Service → CQERS Exe → Manage → Controller chain where that profile supports HTTP. Check `EgonModel` fields in DDL and keep them inherited in PO. A v1 or stale plan and unmanaged files block apply; use the v2 generator plan and review the exact file list.

For generated-project business work, reconcile the effective Spec §2.5 inventory with the Plan before editing. Execute each `EXAMPLE-*` Delete/Replace in its owning Step, including unused domains outside the first feature; before first-business acceptance and final audit, prove no unused executable example or incoming reference remains across target modules. Missing cleanup scope requires a Spec/Plan amendment, not improvised deletion. Retain only evidenced real business, required infrastructure and immutable applied history; preserve user work and correct applied schemas only with a new DDL version.

## Java / CQE effective contract (2026-09-22)

Read `references/egon-java-cqe-contract.md` after resource preflight for every Java task. Apply its POJO, enum, component/MP reuse, validation, soft-delete uniqueness, CQE delivery and source-grounded DDD checks in design, planning and final review. This is the latest user-directed contract; ordinary three-layer structure stays unchanged.

## Transactional Outbox and Spring Statemachine (2026-09-27)

After resource preflight, read `references/outbox-statemachine-integration.md` when the task adopts Outbox, consumes business Events through Spring Statemachine, or changes technical delivery/migration. The existing single Starter owns the mandatory technical lifecycle; its business adapter is optional. Ordinary queries and unrelated CRUD do not require it, and direct MQ or existing handlers remain valid CQE choices.

Use section 2 at Step validation and both sections during final Spec review. Verify actual transaction/receipt ordering, matching envelope headers, tenant restoration, fresh Machine execution, MP wiring and failure tests. Inspect current Mapper and timeout enforcement; a configured property, skipped PG test or old README is not runtime evidence.

## Purpose

Execute one authorized coding Plan, with one active Step at a time and a verified commit for each completed implementation change. Continue independent Steps when a blocker can be isolated. Audit the delivered repository against the effective Specs and report unmet, partial, or runtime-unverified requirements.

The effective Specs define the accepted outcomes and constraints. The Plan defines the implementation sequence and evidence needed to deliver them. Use the following authority throughout execution, including preflight, resume, and final audit.

## Intent-preserving Spec and Plan revisions

Authorization to execute the baseline extends to small revisions supported by current repository evidence. Apply the smallest compliant correction and continue without another approval when it preserves the accepted requirements, non-goals, observable contracts, architecture, security and permission boundaries, data and migration guarantees, compatibility, and acceptance coverage. Judge scope by impact on consumers and dependencies.

- **Plan:** Choose or adjust internal implementation details, including design patterns when the accepted Spec leaves the choice open. Correct stale paths, symbols, file order, and validation commands. Add a necessary internal file, wiring point, fixture, or test when it serves an existing requirement and respects Step dependencies and ownership. Update affected file lists, commit paths, commands, and downstream references before relying on them.
- **Spec:** Clarify or correct the smallest affected passage when its intended meaning follows from the accepted requirements, effective contracts or acceptance criteria, and repository evidence. Preserve the binding meaning. When the change only concerns implementation, update the Plan alone.
- Record the original requirement or decision, repository evidence, old and new document revisions or sections, affected Steps and paths, why the change preserves intent, and validation results. Update affected references and revision metadata. Respect explicitly frozen documents and use the Step evidence record for clarifications that can be applied under the existing text. Include document paths in a Step commit only when they belong to its authorized repository and scope.
- Recheck dependencies and affected requirements, rerun strict Plan validation when the Plan changes, and refresh affected behavioral, compatibility, and scope checks. A replacement validation must prove the same requirement with equivalent coverage and evidence. Correct proven parser false positives with equivalent wording that preserves the Plan template and references. For a frozen Plan, validate that normalization on an isolated copy. Record the original diagnostic, the exact normalization, and both results.
- Existing authorization boundaries still apply, including dependency introduction, live-system changes, and running DDL. Escalate only under Failure and blocker handling. An explicit restriction on revisions from the user or repository governs the affected document.

## Time semantics and UTC policy

When time semantics or boundaries are affected, read `references/time-semantics.md`. Distinguish occurred instants (UTC storage and ISO 8601 `Z` transmission), pure dates, local times, zoned schedules and durations. Use explicit business zones for computation and explicit display zones on the frontend; enforce `MC-TIME-001` with concrete boundary/compatibility evidence. This policy does not expand the authorized code/data/migration scope.

## Resource-integrity preflight

Resolve the directory containing this `SKILL.md` as `<skill-root>`. Before reading a bundled reference or editing code, run:

```bash
python3 <skill-root>/scripts/validate_skill_resources.py
```

Replace `<skill-root>` with the resolved absolute directory. Bundled resources must use skill-root-relative `references/` or `scripts/` paths. If preflight reports a missing, escaping, ambiguous, or broken resource, diagnose and restore verified resources before using them. If that is unavailable, report the exact diagnostic and block dependent work.

## User-mandated Java rules — effective normative source

The following rules are preserved exactly and are mandatory at coding, Step, commit, and final-audit gates. Read `references/user-mandated-java-rules.md` for the concrete inspection contract.

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

Do not translate, renumber, correct, shorten, summarize, or weaken this block. Literal spellings are mapped to exact code symbols by `references/user-mandated-java-rules.md` without changing the source.

## Entry conditions and execution authorization

Before modifying code:

1. Identify one exact Plan path and read it completely.
2. Resolve and read the primary `Implements Spec` plus every `Effective Specs` document and governing amendment, supersession, or dependency.
3. Read all applicable `AGENTS.md` files and repository instructions.
4. Confirm the Plan revision and repository baseline. Reinspect every current path, symbol, migration sequence, consumer, and validation command used by the next Step.
5. Establish the authorized Plan and effective Spec baseline and reconcile recorded revisions. An instruction to execute an identifiable Plan authorizes its effective baseline, including on resume and when its header says `Review`. Record the authorization source and honor any express restriction on when execution may begin.
6. Run the structural validator supplied by the installed `stellar-enclosure-coding-writing-plan` skill against the exact Plan with strict mode enabled.
7. Inspect `git status`, the current branch/HEAD, staged changes, and untracked files. Preserve all unrelated work.
8. For Java work, read `references/user-mandated-java-rules.md` and `references/java-spring-egon-coding-standards.md` completely. Confirm the actual project tree is either the established traditional layered profile or the exact selected Egon-COLA Archetype profile. Revalidate the Plan's Spring/Starter/Egon/module capability reuse ledger before adding code or dependencies.

Resolve entry discrepancies against the authorized baseline and current evidence. Apply the revision procedure to correct local drift or structural errors before starting implementation. Use the blocker criteria if the target, authorization, or a binding decision cannot be established, or required structural validation cannot be satisfied.

Inspect actual validation side effects. Run repository-standard local checks, including bounded tests with disposable local services, browser sessions or test databases, within the execution authorization when the environment is isolated and no explicit restriction applies. This is the execution rule for local tests mentioned in the shared references. Verify the target, limit the run and clean up its resources. Shared or live environments, external writes, and other restricted operations require the applicable authorization. Seek equivalent authorized evidence when practical. Track runtime acceptance assigned to a later phase separately.

## Step state machine

Treat each Plan Step as a strict state transition:

```text
Pending -> In Progress -> Verified -> Committed
              |              |
              +-> Blocked <--+
              |
              +-> Satisfied at baseline
```

- `Pending`: no Step-owned edits have begun.
- `In Progress`: work is limited to the current Step and its documented, intent-preserving revisions.
- `Verified`: every required gate for this Step passed against the candidate change, with fresh evidence and a passing diff review.
- `Committed`: one path-limited semantic commit exists and its hash/scope were verified.
- `Satisfied at baseline`: the no-change path from `In Progress`. Reachable committed source and fresh checks prove the Step's complete outcome under rule 12.
- `Blocked`: after diagnosis, no authorized path can implement, verify, or deliver a required result while preserving accepted intent.

**Begin Step implementation only after its prerequisites are `Committed` or `Satisfied at baseline`.** Preflight can investigate and resolve unmet prerequisites. Follow the effective schedule. Complete a verified changed Step immediately by committing it and recording its `HEAD` and paths. Isolate any unfinished blocked candidate under rule 16 before editing an independent Step.

## Non-negotiable execution rules

1. Execute one Step at a time in effective Plan order, with a separate candidate and commit scope for each Step.
2. Keep at most one Step `In Progress`.
3. Follow the effective Step's file order, operations, required outcomes, and validation gates. Adapt implementation details under Intent-preserving Spec and Plan revisions.
4. Use test-first execution for every behavior covered by a planned RED/GREEN cycle: write the focused test, observe the expected failure, implement the minimum behavior, then refactor/wire while tests remain green.
5. Keep changes within the current Step's effective paths and generated outputs. Record a permitted local revision before editing newly needed paths. Escalate a required material change under Failure and blocker handling.
6. Preserve unrelated dirty or staged work. Stage and commit only explicit Step file paths. Never stage the whole repository or a directory.
7. Preserve unrelated edits, including overlaps. When needed, isolate the candidate in a scratch checkout or worktree. Verify that the delivery branch can contain the Step commit while preserving those edits. Escalate an overlap only if it still prevents safe delivery.
8. Run the exact focused validation required by the Step, plus the smallest relevant compile/static/regression gate needed to prove the Step is internally complete.
9. Read the full command output and exit status. A lost process handle, partial log, timeout, skipped test, or warning treated as failure by repository policy is not a pass.
10. Review the Step diff against its requirements and the effective Spec before committing. Remove accidental files, debug output, secrets, generated noise, and unrelated refactors.
11. Commit each verified implementation change immediately using a path-limited semantic Step commit. Prefer the Plan's proposed message when it matches the final diff and repository convention.
12. Reconcile any apparent no-op with the intended outcome and Git history. Reuse a completed Step commit when the resume checks establish its provenance and evidence. If the committed baseline already satisfies the complete outcome, verify its applicable gates and record the baseline commits and passing evidence as `Satisfied at baseline` in the Plan or, for a frozen Plan, in the Step evidence record. Record that the behavior was already present. Reuse or recover historical RED evidence if the Plan specifically requires it.
13. After committing, verify the commit hash, file list, diff summary, validation evidence, and remaining worktree state. Record the commit against the Step before advancing.
14. Preserve committed history. When a later Step exposes a defect in earlier work, apply the revision criteria, isolate the smallest compliant corrective change attributed to the originating Step, verify it, and record a dedicated corrective commit before continuing. Escalate when the blocker criteria are met.
15. Use MP-SDJ distributed managed DDL. Implement the planned new SQL version and manifest update, preserve applied history, and verify per-target retry semantics. Test it in an isolated disposable fixture within the authorized validation scope. Shared or live DDL execution requires operational authorization. The generator reads SQL as input and writes files. Use it for approved initial CREATE scaffolding or explicitly configured standalone inbound events, with the required SQL journal. Implement later structural DDL changes under the Plan. Diagnose generator conflicts and missing classpaths before treating generation as unavailable.
16. Adjust the remaining schedule autonomously when prerequisites, binding order constraints, validation, rollback, and separate commits are preserved. A Step number alone does not create a dependency. Record the effective order and reason in the Plan or Step record for a frozen Plan. Park a blocked Step while independent Steps with satisfied prerequisites proceed when the candidates and evidence can be isolated.
17. Every coding Step has a blocking Manual Check. At Step lock, enumerate all applicable stable `MC-*` IDs from `references/java-spring-egon-coding-standards.md`; before commit, evaluate each row manually with concrete diff/path/symbol/command evidence. `MC-SCOPE-001` and `MC-TEST-001` always apply.
18. A Step cannot become `Verified` or be committed as complete while any applicable Manual Check is `FAIL`, `BLOCKED`, `UNKNOWN`, missing, lacks evidence, or has an unresolved exception. Evidence-backed `N/A` is allowed only when the concern is truly outside the Step.
19. For touched Java code, enforce semantic type suffixes, Jakarta/Spring Validation and groups at every affected handoff, approved normalization, the exact value-object record/ordinary class Lombok classification, MapStruct/MapStructPlus plus the appropriate Egon converter base, `@Slf4j`, explicit Bean names, qualified Lombok constructor injection, approved utilities, Jackson, `java.time`, configuration-profile parity, and an appropriate design pattern for every Complex business flow. Do not perform unrelated broad cleanup.
20. Apply `references/user-mandated-java-rules.md` literally. At Step lock and before commit, execute separate Rule 1, 2, 3, 4, 5, 6, 7, 9, 10, and 11 rows. Rule 11 always applies. No rule may be collapsed into a generic Manual Check assertion, changed to preference language, or waived because tests pass.
21. Ordinary POJOs retain the required Lombok baseline. Every new affected Converter must use MapStruct/MapStructPlus plus Egon `BaseConverter` for bidirectional mappings or `BaseForwardConverter` for irreversible projections. Diagnose constructor, framework, and converter conflicts and use a compliant resolution when available. Validate every affected layer handoff and implement the effective Plan's pattern for every Complex business flow.

## Per-Step execution workflow

Use `references/step-gate-checklist.md` for preflight, Step lock and verification. On resume, reconcile prior commits and any interrupted Step before editing.

### 1. Preflight and repair

Before starting or resuming each Step, inspect the current baseline, prerequisites, file ownership, real paths and symbols, consumers, migration sequence, and validation setup. For Java, check the relevant architecture and reuse evidence, dependencies, `lombok.config`, converters, validators, profiles, and nearby conventions.

Resolve any nonconformance autonomously when an authorized path preserves accepted intent. Fix stale Plan/Spec details or recover the local validation setup, then recheck the affected preflight items. Add code repairs that belong to the current Step to its effective scope and execute them through that Step's gates. When a prerequisite Step needs correction, resolve it first using the applicable Step or corrective commit workflow, then refresh the baseline. Isolate unrelated baseline failures where practical and record their impact on validation. Apply the blocker rule only if a required prerequisite or check remains unattainable.

### 2. Lock the effective Step

- Record the Step number/title, requirements, verified prerequisites, current `HEAD`, effective paths, checks, rollback point, and proposed commit. Include preflight findings, repairs, and outstanding fixes owned by this Step.
- Record the applicable `MC-*` IDs and all ten Literal Rule rows with current evidence. Rule 11 is always `Applicable`. Baseline issues that this Step can repair are implementation work. Passing results are required for the final candidate before commit.

### 3. Execute the Step in file order

- Apply each declared `CREATE`, `MODIFY`, `DELETE`, `RENAME`, or `GENERATED` action in order.
- For behavior changes, run the focused test at the RED point and confirm the failure is caused by missing behavior rather than syntax, fixture, dependency, or environment errors.
- Implement only the minimum Spec-compliant behavior needed for GREEN.
- Preserve current project style, comments/annotations, module boundaries, public compatibility, and unrelated behavior.

### 4. Verify the Step

- Run the Step's exact focused command and confirm the objective expected result.
- Run applicable compile, lint/format, mapper/XML/schema, module, or cross-module checks required by the Step and repository.
- Run `git diff --check` on the Step paths.
- Re-read the Step requirements and relevant Spec sections. Confirm every stated behavior, error path, field, state, permission, migration, UI state, and test obligation represented by this Step is implemented.
- Execute each applicable Manual Check row in `references/step-gate-checklist.md`. Record independent evidence, fix recoverable failures, and rerun affected checks.
- Re-execute every Literal Rule Gate row against the final diff. Tests/static searches support but never replace the manual per-rule inspection.

Any failed gate keeps the Step `In Progress` or `Blocked`; it cannot be committed as complete.

### 5. Review and commit the Step

Follow the checklist's path-limited commit procedure. Review the full Step candidate against its baseline, including staged changes and new files. Commit only verified Step paths and confirm the resulting parent, hash, and paths. Confirm unrelated index and worktree changes are preserved.

Mark a changed Step `Committed` after this check. For a no-change Step, record `Satisfied at baseline` and its evidence before advancing.

## Failure and blocker handling

Keep work `In Progress` while an implementation, repair, equivalent validation, or safe isolation can meet the requirement. Diagnose the cause and try the smallest qualifying alternatives. Mark only the affected work `Blocked` when evidence shows that no authorized path can achieve a required result, verification, or delivery. This includes a conflict in binding requirements or a missing product decision that puts every viable path outside accepted intent.

Continue independent authorized work under rule 16. Report the affected Step and requirement, evidence, alternatives considered, and exact missing prerequisite. Ask one focused question only if a specific answer or authorization from the user would remove that blocker.

## Commit contract

Every changed Step needs a verified non-empty semantic commit to complete. Park unfinished blocked changes separately under rule 16. For `Satisfied at baseline`, record the existing reachable commit evidence and refreshed verification.

For each Step record:

| Evidence | Required value |
| --- | --- |
| Step | Number and exact Plan title |
| Requirements | Source requirement IDs |
| Baseline | Commit before Step edits |
| Commit | Resulting full or short hash |
| Paths | Exact committed file list |
| Validation | Commands and observed results |
| Manual Check | All stable IDs with `PASS` or evidence-backed `N/A`; no unresolved row |
| Literal Rules | Rules 1, 2, 3, 4, 5, 6, 7, 9, 10, 11 each `PASS` or evidence-backed `N/A`; Rule 11 `PASS` |
| Deviations | `None`, recorded intent-preserving revision, separately approved change, or attributed corrective commit |

Use path-limited staging/commits. Never include unrelated work merely because it was already staged. Do not push, open a PR, merge, or release unless the user separately authorizes it.

## Final Spec conformance audit

Read `references/final-spec-audit.md` and audit the accepted baseline, recorded revisions, and delivery commits. Audit completed independent scope while Steps are parked, then refresh the audit as additional work completes. Final PASS requires every Step to have verified completion evidence and every requirement to be satisfied.

1. Re-resolve the final effective Spec set and exact revisions.
2. Extract every effective requirement, acceptance criterion, non-goal, interface, model/schema rule, UI behavior, test obligation, non-functional constraint, migration, compatibility, rollout, and rollback requirement.
3. Map each requirement to concrete implementation evidence: commit(s), paths/symbols, and validation/test output.
4. Run the safe, authorized final validations on the delivery commits. Use a clean checkout when unrelated work could affect results. For live acceptance, record the authorized run's evidence or the missing runtime proof.
5. Assign one status to every requirement:
   - `Satisfied`: implementation and all required evidence, including runtime evidence where required, prove it.
   - `Partial`: only part of the requirement is implemented or proven.
   - `Not satisfied`: implementation conflicts with or omits the requirement.
   - `Runtime unverified`: source/module evidence exists and required live-system proof is the only outstanding evidence.
6. Check non-goals and scope boundaries for accidental behavior, dependency, migration, or refactor expansion.
7. Check every Plan Step has verified commit or baseline evidence and every required file and validation gate was accounted for.
8. Dispose of every scratch verification harness created during execution. Each one is promoted into a durable check suite, moved into the subsystem it actually guards, or deleted; report the chosen outcome per harness. An undetermined script left in repository `scripts/work/` is a finding, and never a silent pass.
9. Re-execute all ten effective Literal Rules and all 17 Manual Checks against the final tree and delivery commits. Every applicable row must be `PASS`; every `N/A` needs evidence and reason; Rule 11 and `MC-BLOCKER-001` must pass and reconcile all findings.
10. Report every `Partial`, `Not satisfied`, `Runtime unverified`, failed/blocked Literal Rule or Manual Check, and silent-exception attempt with evidence, impact, and recommended next action.

If the audit finds a gap that can be corrected within the accepted intent, record the finding and minimal corrective Step in the Plan or the audit's execution record for a frozen Plan. Execute it through the Step gates and make a dedicated corrective commit for any repository change. Refresh the audit baseline and rerun affected requirement checks and final gates against the corrected delivery. Use Failure and blocker handling for unresolved findings.

## Final report contract

The completion report must contain:

- Plan path/revision and effective Spec paths/revisions;
- a Step table with status, commit hash, committed paths, and validation evidence;
- final validation commands and actual results;
- a Spec conformance matrix with every requirement status;
- a final Manual Check matrix containing every stable `MC-*` ID, applicability, status, evidence, finding, and required action/exception;
- a final Literal Rule matrix containing Rules 1, 2, 3, 4, 5, 6, 7, 9, 10, and 11 separately with applicability, status, diff evidence, validation evidence, finding, and action;
- explicit unmet, partial, and runtime-unverified requirements;
- intent-preserving revisions with rationale, document changes and evidence, plus any separately approved deviations and corrective commits;
- the disposition of every scratch verification harness created during execution, and confirmation that repository `scripts/work/` holds no undetermined script;
- remaining worktree state and confirmation that unrelated work was preserved;
- whether runtime, database, browser, deployment, push, PR, or release actions were not performed;
- one final verdict:
  - `PASS — Implementation conforms to the effective Specs`
  - `PARTIAL — Spec requirements are unmet or unverified`
  - `BLOCKED — Final verification could not be completed`

Never claim full completion when any effective requirement is `Partial`, `Not satisfied`, required runtime evidence is missing, or any Literal Rule/Manual Check is missing, non-passing, weakened, or unsupported. Final PASS requires every Spec requirement satisfied, every applicable literal rule passed, Rule 11 passed, and every applicable Manual Check passed.

## Common failures

| Failure | Required correction |
| --- | --- |
| Mixing unfinished edits from several Steps | Isolate each Step's candidate and evidence, then complete one runnable Step at a time |
| Starting a dependent Step after tests but before commit | Verify and commit the prerequisite Step first |
| Committing unrelated staged files | Use path-limited commit and verify the committed file list |
| A Step produces no semantic diff | Verify whether the committed baseline already satisfies the Step and record the evidence. Apply the blocker criteria to any remaining gap. |
| Treating Plan completion as Spec compliance | Run the independent final Spec audit |
| The audit finds a locally repairable gap | Record and verify the correction through the Step gates, then refresh the final audit |
| Claiming runtime acceptance from unit/module tests | Mark the requirement `Runtime unverified` |
| Rewriting earlier Step commits after later work | Preserve history; use an attributed corrective commit and report it |
| Committing a Step with a failed/missing/unknown Manual Check | Keep it `In Progress`/`Blocked`; close each row with evidence before commit |
| Adding a dependency/helper without current Spring/Egon/module reuse proof | Recheck existing capabilities and choose a compliant local resolution when available. Apply the dependency gate to an unavoidable unauthorized introduction. |
| Creating a hybrid package tree during execution | Implement within the existing traditional or exact Archetype profile and revise the Plan locally where supported |
| Passing final audit by summarizing Manual Checks in one sentence | Re-run and record all stable IDs individually against the final commits/tree |
| Replacing the exact numbered rules with a general coding-standard summary | Restore the verbatim source and re-run every effective Rule row independently |
| Tests pass while an ordinary class lacks the required Lombok baseline or a Converter bypasses its required Egon base | Repair Rule 3 compliance and rerun the checks before completing the Step |
| Only Controller input is validated | Add and verify Validation/groups at every affected layer handoff before commit |
| Complex business logic remains `if/else` or `switch` | Implement the effective design-pattern participants and tests before commit |
| A required test starts a service | Check its effects and run a bounded isolated fixture when covered by the task. Apply the blocker criteria if essential evidence depends on an unavailable external environment. |
| Sending ALTER to the CREATE-only generator, or hand-finishing a CREATE generator conflict | Follow the agent-owned ALTER Plan. For initial CREATE, diagnose `CONFLICT` or `BLOCKED_TOOLING` and recover the verified generator path when possible. |

## Skill maintenance

When changing this skill, run `scripts/test_user_mandated_java_rules.py`, `scripts/test_validate_skill_resources.py`, and `scripts/validate_skill_resources.py`, review `references/acceptance-scenarios.md`, and confirm the verbatim source, English operational file, Chinese review mirror, standards, checklists, and metadata still express the same execution contract.


## Constructor injection amendment (2026-10-13)

For Spring dependency-injection constructors, explicitly declare `@Autowired` even when there is only one
constructor. Keep final dependency fields and use `@RequiredArgsConstructor(onConstructor_ = @Autowired)`;
retain stable Bean names and every `@Qualifier`, including its propagation to generated constructor parameters.
With multiple constructors, annotate only the intended DI constructor. Field injection remains forbidden.
This rule does not apply to DTO/PO/exception/Domain value construction or `@ConfigurationProperties` constructor
binding; `@Autowired` on a binding constructor changes its semantics. Verify the emitted constructor annotation
and parameter Qualifiers by compilation/reflection. Source-projects and CRUD templates are the producer truth;
do not patch generated Java. This amendment supplements Rule 4 without changing the historical quoted text.
