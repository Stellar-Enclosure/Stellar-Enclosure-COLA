# Per-Step Gate Checklist

Use this checklist for every Plan Step. Record objective evidence; do not replace failed or missing evidence with a narrative assertion.

## Resume an interrupted execution

1. Reconcile the recorded Plan and effective Spec revisions, authorization, repository, branch and baseline. Reuse authorization for unchanged revisions. Stop on material drift or unclear ownership.
2. From the Step record and Git history, verify each completed Step in order. The recorded commit must be non-empty, reachable from current HEAD, scoped to the declared paths, and tied to passing validation and manual evidence. Inspect later changes to its paths and prerequisites. Only resume past a Step whose completion is supported by that evidence. Reconcile any interrupted post-commit check with the actual commit before making further edits.
3. Resume the first uncommitted Step. If all existing edits on its paths are attributable to that Step, compare them with its recorded baseline, inspect staged and unstaged changes, and refresh the required evidence before committing. Preserve the recorded RED result when it still matches the Step. If required RED evidence is missing, reproduce it on the recorded baseline in a separate scratch checkout when feasible. Never discard changes to reconstruct RED. An unexplained commit, missing evidence that cannot be recovered, or unclear edits on a Step path blocks advancement.

## Step lock

- [ ] Exact Plan path and revision recorded.
- [ ] Step number, title, requirement IDs, dependencies, and proposed commit recorded.
- [ ] Effective Spec sections for this Step reread.
- [ ] `git rev-parse HEAD` recorded as the Step baseline.
- [ ] Current branch, `git status --short`, staged paths, and untracked paths inspected.
- [ ] Every earlier dependency is represented by an already verified commit.
- [ ] Declared Step paths do not overlap unrelated or concurrent edits.
- [ ] Planned files, symbols, consumers, migration sequence, and commands still exist.
- [ ] No material repository, Plan, or Spec drift is present.

## RED gate when the Step changes behavior

- [ ] The focused test is created or modified before production behavior.
- [ ] The exact focused command is run.
- [ ] The test fails for the expected missing behavior.
- [ ] Failure is not caused by compilation, fixture, dependency, environment, or unrelated errors.
- [ ] RED evidence is recorded. If RED is legitimately impossible, the approved reason is recorded.

## Implementation gate

- [ ] Only one Step is `In Progress`.
- [ ] Files are handled in the Plan's declared order.
- [ ] Every operation is limited to declared `CREATE`, `MODIFY`, `DELETE`, `RENAME`, or `GENERATED` paths.
- [ ] Implementation follows real repository APIs and conventions while preserving the Plan's semantics.
- [ ] No undeclared public contract, dependency, migration, schema, permission, page, or architecture change was introduced.
- [ ] No unrelated refactor, formatting sweep, debug output, secret, or generated noise was added.

## User-mandated Literal Rule gate

Read `references/user-mandated-java-rules.md`. Copy all rows into the Step record at Step lock and refresh them against the final diff before commit. Do not merge rows or substitute a general standards statement.

| Literal rule | Applicability | Status | Diff/path/symbol evidence | Test/static evidence | Finding | Required action/exception |
| --- | --- | --- | --- | --- | --- | --- |
| Rule 1 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<type inventory and declarations>` | `<compile/name search>` | `<semantic suffix result>` | `None / action and owner` |
| Rule 2 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<every affected handoff/groups/normalization>` | `<positive/negative/group tests>` | `<layer validation result>` | `None / action and owner` |
| Rule 3 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<value-object record/class Lombok/Converter diff>` | `<compile/mapping tests/generated mapper>` | `<model/conversion result>` | `None / action and owner` |
| Rule 4 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<business classes/Bean names/Qualifier/lombok.config>` | `<compile/wiring/log review>` | `<logging/injection result>` | `None / action and owner` |
| Rule 5 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<imports/manifests/helpers>` | `<dependency/import search>` | `<allowlist result>` | `None / action and owner` |
| Rule 6 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<external contracts/Jackson annotations>` | `<serialization/compatibility tests>` | `<Jackson result>` | `None / action and owner` |
| Rule 7 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<base and every environment profile>` | `<normalized key-parity/config tests>` | `<profile structure result>` | `None / action and owner` |
| Rule 9 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<flow classification/pattern participants/wiring>` | `<pattern behavior/branch search>` | `<Complex pattern or Simple direct result>` | `None / action and owner` |
| Rule 10 | Applicable / Not applicable | PASS / N/A / FAIL / BLOCKED | `<time fields/imports/mappings>` | `<time tests/forbidden import search>` | `<java.time result>` | `None / action and owner` |
| Rule 11 | Applicable | PASS / FAIL / BLOCKED | `<Step paths/current and target tree/verifier>` | `<architecture/static/build gate>` | `<selected structure preserved>` | `None / action and owner` |

Literal Rule verdict rules:

- [ ] Original order is exactly Rule 1, 2, 3, 4, 5, 6, 7, 9, 10, 11; no Rule 8 is invented.
- [ ] Rule 11 is always `Applicable` and `PASS` before a completion commit.
- [ ] Every applicable rule is `PASS` with fresh final-diff evidence.
- [ ] Every `N/A` has positive scope evidence and does not hide a touched violation.
- [ ] Tests/compile/static searches supplement manual inspection and cannot waive a rule.
- [ ] Any missing row/evidence, `FAIL`, `BLOCKED`, `UNKNOWN`, reduced mandatory implementation, or silent exception prohibits `Verified` and commit-as-complete.

## Blocking Manual Check gate

Read `references/java-spring-egon-coding-standards.md`. Copy all stable IDs below into the Step record and evaluate them one by one after implementation and again immediately before commit. `Applicable` permits `PASS`, `FAIL`, or `BLOCKED`; `Not applicable` permits only `N/A`. Every row requires concrete evidence and a finding. A failed/blocked row requires an exact action and owner.

| Check ID | Applicability | Status | Evidence | Finding | Required action/exception |
| --- | --- | --- | --- | --- | --- |
| `MC-ARCH-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<tree/module/dependency evidence>` | `<profile preserved or impact absent>` | `<None or action/owner>` |
| `MC-REUSE-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<search paths/resolved capability>` | `<reuse conclusion>` | `<None or action/owner>` |
| `MC-DEP-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<manifest/diff/gap approval>` | `<dependency conclusion>` | `<None or action/owner>` |
| `MC-NAME-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<touched type list/search>` | `<semantic-name conclusion>` | `<None or action/owner>` |
| `MC-VALID-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<constraints/groups/normalizer/tests>` | `<boundary coverage>` | `<None or action/owner>` |
| `MC-MODEL-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<value-object record/class complex Lombok diff>` | `<construction/conflict conclusion>` | `<None or action/owner>` |
| `MC-CONVERT-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<MapStruct/MapStructPlus + BaseConverter or BaseForwardConverter diff/search>` | `<mapping/no-bypass conclusion>` | `<None or action/owner>` |
| `MC-LOG-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<business-class/log diff>` | `<logging conclusion>` | `<None or action/owner>` |
| `MC-BEAN-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<Bean names/Qualifier/lombok.config>` | `<injection conclusion>` | `<None or action/owner>` |
| `MC-UTIL-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<imports/dependencies/helper search>` | `<utility conclusion>` | `<None or action/owner>` |
| `MC-JSON-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<JSON imports/contract annotations/tests>` | `<Jackson conclusion>` | `<None or action/owner>` |
| `MC-TIME-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<time types/converters/tests>` | `<java.time conclusion>` | `<None or action/owner>` |
| `MC-CONFIG-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<all profile key comparisons/properties>` | `<configuration parity>` | `<None or action/owner>` |
| `MC-PATTERN-001` | `Applicable / Not applicable` | `PASS / N/A / FAIL / BLOCKED` | `<flow classification/pattern participant evidence>` | `<mandatory Complex pattern / Simple direct conclusion>` | `<None or action/owner>` |
| `MC-SCOPE-001` | `Applicable` | `PASS / FAIL / BLOCKED` | `<Step diff/status and untouched paths>` | `<scope conclusion>` | `<None or action/owner>` |
| `MC-TEST-001` | `Applicable` | `PASS / FAIL / BLOCKED` | `<actual commands/output/static review>` | `<standards/behavior proof>` | `<None or action/owner>` |
| `MC-BLOCKER-001` | `Applicable` | `PASS / FAIL / BLOCKED` | `<all rows and Step blockers>` | `<closure conclusion>` | `<None or exact unresolved action/owner>` |

Manual Check verdict rules:

- [ ] Every stable ID appears exactly once; no row is merged into another.
- [ ] Every applicable concern is `PASS` with current Step evidence.
- [ ] Every `N/A` has positive scope evidence and an explicit reason; `N/A` is not used to hide a violation.
- [ ] Static checks/tests supplement, but do not replace, line-by-line manual review.
- [ ] `MC-BLOCKER-001` is `PASS` only when all other rows are `PASS` or valid `N/A`.
- [ ] Any `FAIL`, `BLOCKED`, `UNKNOWN`, missing ID/evidence, or unresolved exception keeps the Step `In Progress`/`Blocked` and prohibits a completion commit.

## Verification gate

- [ ] Commands were classified by actual side effects. Runtime startup, live-system acceptance, and changes to external state were authorized before execution. Required Step gates passed. Acceptance checks assigned to a later phase were recorded separately.
- [ ] The checked files match the Step candidate. If unrelated work can affect results, use a scratch checkout containing the Step baseline plus only the candidate changes, or report that the gate is blocked.
- [ ] Exact focused validation passed with complete output and exit status.
- [ ] Required compile, typecheck, lint/format, XML/schema, module, integration, or regression gates passed.
- [ ] Expected GREEN behavior and relevant error paths are covered.
- [ ] Current Step requirements and effective Spec sections were reread against the implementation.
- [ ] For generated-project business work, every Spec §2.5 EXAMPLE ID is covered by the Plan. Current-Step Delete/Replace paths and registrations/resources/tests are closed; before first-business acceptance, no unused example chain remains anywhere in the target project. Real-business/infrastructure/history retention has evidence, and applied SQL was corrected only by a new version.
- [ ] All Literal Rule rows are `PASS` or evidence-backed `N/A`, with Rule 11 `PASS`; evidence was refreshed after the final diff.
- [ ] All Step Manual Check rows are `PASS` or evidence-backed `N/A`; evidence was refreshed after the final diff.
- [ ] `git diff --check -- <Step paths>` passed.
- [ ] Failures, skips, warnings, timeouts, and unavailable runtime evidence are classified honestly.

## Pre-commit gate

From the repository root, capture status and the unrelated staged diff before staging. Confirm `HEAD` still equals the Step baseline and there are no conflicts. Set the `step_paths` Bash array to the exact changed Step file paths, including both sides of each rename. Review ownership before staging, including untracked files.

Build `stage_paths` from the Step paths that exist in the worktree (including symlinks) or still have an index entry. A path absent from both may be left out of `stage_paths` only if it is a verified staged deletion, such as the source of a rename already staged with `git mv`. Verify `D` for that exact path with `git --literal-pathspecs diff --cached --no-renames --name-status "$step_baseline" -- "$path"`. Investigate any other missing path before proceeding. Retain all Step paths for review and commit.

```bash
if (( ${#stage_paths[@]} > 0 )); then
    git --literal-pathspecs add -A -- "${stage_paths[@]}"
fi
git --literal-pathspecs diff --cached "$step_baseline" -- "${step_paths[@]}"
git --literal-pathspecs diff --cached --check "$step_baseline" -- "${step_paths[@]}"
git --literal-pathspecs diff --cached --name-status "$step_baseline" -- "${step_paths[@]}"
git --literal-pathspecs diff --exit-code -- "${step_paths[@]}"
```

- [ ] The scoped staged diff was reviewed line by line and matches the validated Step candidate. It includes all new, changed and removed files for this Step.
- [ ] The worktree matches the index for the Step paths, and the candidate and HEAD are unchanged since review. Refresh the checks if either changed.
- [ ] The commit message is semantic and matches the Plan proposal or repository convention.
- [ ] The candidate diff is non-empty.
- [ ] The final Literal Rule verdict is PASS; no mandatory rule was reduced or waived because tests passed.
- [ ] The final pre-commit Manual Check verdict is PASS; no blocker was waived in prose.

Commit immediately with `git --literal-pathspecs commit --only -m "$step_message" -- "${step_paths[@]}"`. Path-limited commit uses the worktree versions of the named files. The equality check above makes those the versions you reviewed in the index. Unrelated staged entries stay outside the commit.

## Post-commit gate

- [ ] Commit succeeded and its hash was recorded.
- [ ] `git show --stat --oneline <hash>` was inspected.
- [ ] The parent is the recorded baseline. The committed diff and paths match the reviewed candidate.
- [ ] Validation commands and observed results are recorded against the hash.
- [ ] Deviations are recorded as `None`, approved clarification, or corrective-commit explanation.
- [ ] Unrelated staged diff and status match the snapshot taken before staging. No unrelated unstaged or untracked work was changed.
- [ ] The Step is marked `Committed` before any later Step begins.
- [ ] New `HEAD` hash and committed paths were recorded before the first edit of the next Step.

If any required box cannot be checked, keep the Step `In Progress` or mark it `Blocked`; do not advance.

## Java / CQE contract review

Read `references/egon-java-cqe-contract.md`. Record each applicable concern separately: POJO/value-object/enum; components and MP starter; annotation-driven validation; business columns + deleted_at uniqueness and NULL behavior; Command/Query/Event with actual outbox or MQ delivery; source-grounded DDD or preserved three-layer architecture. Use its existing MC ID mapping and record evidence, gaps and validation limits.

For affected time boundaries, read `references/time-semantics.md` and compare effective Spec TIME decisions, Plan owning Steps and MC-TIME-001 results. Verify actual UTC persistence/wire conversion, pure-date stability, business-zone ranges/grouping, frontend display/submission, applicable DST/precision and legacy compatibility. Runtime evidence required by this Step must pass before commit. Track any runtime verification assigned to final or post-delivery acceptance separately and run it only with authorization.
