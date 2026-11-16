# Per-Step Gate Checklist

Use this checklist for every Plan Step. Record objective evidence; do not replace failed or missing evidence with a narrative assertion.

## Resume an interrupted execution

1. Reconcile the authorized Plan and Spec baseline, every recorded revision, repository, branch and baseline commit. Reuse the authority for revisions covered by the skill. Diagnose drift and unclear ownership using Git history, Step evidence and safe isolation where needed.
2. From the Step record and Git history, verify each completed Step in order. A Step commit must be non-empty, reachable from current HEAD, scoped to the effective paths, and tied to passing validation and manual evidence. A Step marked `Satisfied at baseline` needs reachable baseline commit evidence and refreshed checks of its complete required outcome. Inspect later changes to relevant paths and prerequisites and verify any interrupted post-commit check against Git.
3. Resume the earliest runnable Step in the effective schedule. Attribute existing edits by comparing staged and unstaged changes with the recorded baseline, then refresh the required evidence. Reuse matching RED evidence or reproduce it in a scratch checkout when feasible. Preserve worktree changes. Recheck prerequisites before returning to a parked Step.

## Step preflight

Apply this before locking or resuming each Step. If a check exposes a discrepancy, diagnose it, choose an authorized correction that preserves intent, and recheck what changed. Use the blocker rule only when a required condition cannot be met.

- [ ] Reread the effective Spec requirements and compare Plan paths, symbols, consumers, migration sequence and validation commands with the current repository.
- [ ] Inspect branch, HEAD, staged and unstaged changes, untracked paths, and file ownership. Isolate unrelated edits or baseline failures when needed to obtain valid Step evidence and preserve those edits.
- [ ] Verify prerequisites with commit or baseline completion evidence. If a required earlier Step needs repair, execute it or its corrective commit workflow first, then refresh the dependent Step's baseline.
- [ ] Confirm the validation setup can produce the required evidence. Restore an available local tool or fixture, or choose a documented equivalent command with the same coverage. Recheck affected preflight items.
- [ ] For Java, inspect relevant architecture and reuse evidence and identify all applicable Literal Rules and Manual Checks, including any omitted in the Plan. Record baseline problems that the current Step can fix against its effective scope and verify the repairs on the final candidate.

## Lock the effective Step

- [ ] Exact Plan path and revision recorded.
- [ ] Step number, title, requirement IDs, verified prerequisites, effective paths, RED/GREEN intent, validation commands, rollback point and proposed commit recorded.
- [ ] `git rev-parse HEAD` recorded as the Step baseline.
- [ ] If an independent Step proceeds while another is blocked, record the dependency and order check, and isolate their edits and evidence.
- [ ] Record preflight findings, fixes, and recheck results. Identify any remaining fixes owned by this Step in its effective paths and checks.

## Record an intent-preserving revision when needed

- [ ] Cite the source requirement or decision and current repository evidence. Check the impact on consumers and Step dependencies against the accepted intent.
- [ ] Update the smallest relevant Plan or Spec passage and dependent references. Record the before/after sections or revisions, affected Steps and paths, and why the change preserves intent. Keep the evidence in the Step record when a frozen document can still be implemented as written.
- [ ] Revalidate the structure of a revised Plan in strict mode. For a proven parser false positive on a frozen Plan, record the original result, the equivalent normalization in an isolated copy, and that copy's strict result. Compare old and new coverage when a check is replaced.
- [ ] Before completion, record actual validation and check results for affected behavior, compatibility, and commit scope. Choose a qualifying approach among viable alternatives and keep a recoverable failure `In Progress`. Apply the skill's blocker rule only when no authorized path remains.

## RED gate when the Step changes behavior

- [ ] The focused test is created or modified before production behavior.
- [ ] The effective focused command is run.
- [ ] The test fails for the expected missing behavior.
- [ ] Failure is not caused by compilation, fixture, dependency, environment, or unrelated errors.
- [ ] RED evidence is recorded. If the planned RED mechanism is unavailable, select and document an equivalent authorized before/after check when it can prove the same gap and coverage. If mandatory RED evidence cannot be recovered or equivalently established, apply the blocker rule.

## Implementation gate

- [ ] Only one Step is `In Progress`.
- [ ] Files are handled in the effective Plan order, including documented local revisions.
- [ ] Every operation is limited to the Step's effective `CREATE`, `MODIFY`, `DELETE`, `RENAME`, or `GENERATED` paths.
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

- [ ] Commands were classified by actual side effects. Run bounded, isolated local fixtures under the task's validation scope. Check applicable authorization for shared/live systems and external writes. Required Step gates passed. Acceptance checks assigned to a later phase were recorded separately.
- [ ] The checked files match the Step candidate. If unrelated work can affect results, isolate the Step baseline and candidate when possible and classify any remaining impediment under the blocker criteria.
- [ ] Effective focused validation passed with complete output and exit status. Any documented replacement proves the same required outcome.
- [ ] Required compile, typecheck, lint/format, XML/schema, module, integration, or regression gates passed.
- [ ] Expected GREEN behavior and relevant error paths are covered.
- [ ] Current Step requirements and effective Spec sections were reread against the implementation.
- [ ] For generated-project business work, every Spec §2.5 EXAMPLE ID is covered by the Plan. Current-Step Delete/Replace paths and registrations/resources/tests are closed; before first-business acceptance, no unused example chain remains anywhere in the target project. Real-business/infrastructure/history retention has evidence, and applied SQL was corrected only by a new version.
- [ ] All Literal Rule rows are `PASS` or evidence-backed `N/A`, with Rule 11 `PASS`; evidence was refreshed after the final diff.
- [ ] All Step Manual Check rows are `PASS` or evidence-backed `N/A`; evidence was refreshed after the final diff.
- [ ] `git diff --check -- <Step paths>` passed.
- [ ] Failures, skips, warnings, timeouts, and unavailable runtime evidence are classified honestly.

## Pre-commit gate

Use this gate when the Step changes repository files. For `Satisfied at baseline`, record reachable committed evidence for the complete outcome and the refreshed verification before advancing. A no-diff result alone is insufficient.

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
- [ ] Deviations are recorded as `None`, recorded intent-preserving revision, separately approved change, or attributed corrective commit.
- [ ] Unrelated staged diff and status match the snapshot taken before staging. No unrelated unstaged or untracked work was changed.
- [ ] The Step is marked `Committed` before a dependent Step begins.
- [ ] New `HEAD` hash and committed paths were recorded before editing another Step.

Resolve open checks autonomously where possible. Once the blocker rule applies, retain the affected Step's evidence and advance any eligible independent Step under rule 16. Advance dependent Steps only with verified completion evidence.

## Java / CQE contract review

Read `references/egon-java-cqe-contract.md`. Record each applicable concern separately: POJO/value-object/enum; components and MP starter; annotation-driven validation; business columns + deleted_at uniqueness and NULL behavior; Command/Query/Event with actual outbox or MQ delivery; source-grounded DDD or preserved three-layer architecture. Use its existing MC ID mapping and record evidence, gaps and validation limits.

For affected time boundaries, read `references/time-semantics.md` and compare effective Spec TIME decisions, Plan owning Steps and MC-TIME-001 results. Verify actual UTC persistence/wire conversion, pure-date stability, business-zone ranges/grouping, frontend display/submission, applicable DST/precision and legacy compatibility. Runtime evidence required by this Step must pass before commit. Track any runtime verification assigned to final or post-delivery acceptance separately and run it only with authorization.
