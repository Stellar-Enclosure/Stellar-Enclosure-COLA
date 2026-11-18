# RFC-style governance for EGON Coding Specs

## Filenames and identity

- File: `docs/egon/spec/YYYY-MM-DD-HH-MM-<lowercase-kebab-abstract>.md`.
- The timestamp is the local creation minute and is part of the document identity.
- The abstract is concise, stable, and specific enough to avoid collisions.
- If a new document would collide with an existing path, choose a more specific abstract.
- Header `Document`, `Created`, and `Updated` values must agree with the file and actual history.
- New Specs use `Template Version: 9` plus `Complexity`, `Complexity Drivers`, `Change Surface`, `Affected Chapters`, `Archetype Cleanup`/§2.5, time-semantics contract §6.3, the literal-rule compliance matrix, the complete blocking Manual Check catalog, and the API governance/gates when an external API is affected. Version 2 through Version 8 Specs remain valid under their original contracts; older historical Specs remain usable when their design is authoritative. Do not rewrite a predecessor only to upgrade its template. Existing-business work with archetype remnants still needs the effective cleanup decision under the current skill, even when the authoritative predecessor uses an older template.

## Status lifecycle

```text
Draft -> Review -> Accepted -> Implemented
  |        |          |
  +------> Rejected   +-> Superseded
```

- `Draft`: incomplete or blocked by a major decision.
- `Review`: internally complete and awaiting user/decision-owner review.
- `Accepted`: explicitly approved; never infer acceptance from silence.
- `Implemented`: the accepted design was delivered and verified by implementation evidence.
- `Superseded`: a later Spec completely replaces it for a named scope.
- `Rejected`: deliberately not adopted.

## Choose the file before drafting

- Identify the Spec from the user's reference or current review context. Check that it covers the same requirement. When searching the repository, match the requirement and scope as well as the filename.
- Confirmation means explicit user or decision-owner approval of the Spec itself, recorded in its lifecycle (`Accepted` or `Implemented`) or evidenced by the approval decision. `PASS — Ready for user review` and approval of individual design decisions leave the Spec pending review.
- For feedback, corrections, or additions to a Spec still awaiting confirmation (`Draft` or `Review`), edit that file in place. Preserve its path, `Document`, `Created`, and template version. Update `Updated`, set the current review status, and keep applicable RFC links. An in-place revision creates no `Amends` or `Supersedes` relationship to its own earlier text.
- For a change to confirmed design, continue in any existing unapproved follow-up for that change. Otherwise create a timestamped follow-up with `Amends` or `Supersedes` as applicable. A separate requirement with no existing Spec can start its own document.

## Relationship semantics

- `Amends`: changes or fills only named sections of an earlier Spec. Both documents form the effective design.
- `Supersedes`: completely replaces an earlier Spec for an explicitly named scope.
- `Depends On`: the current design cannot be understood or implemented correctly without the referenced sections.
- `Related Specs`: useful context without normative dependency.
- `Related Plans`: implementation plans derived from this Spec.

Relationships use relative Markdown links and exact section references. Targets may be older design documents outside `docs/egon/spec` if they remain authoritative.

A follow-up to confirmed design must identify the exact rules it changes and their replacements. Predecessor sections outside that scope remain effective. Keep approved normative content intact. Update relationship or lifecycle metadata only where repository policy allows.

## Effective-design resolution

When several documents govern a task:

1. Start with the base Spec or authoritative legacy design.
2. Exclude content superseded for the relevant scope.
3. Apply accepted amendments in timestamp order.
4. Include normative dependencies.
5. Treat unresolved conflicts as a major ambiguity and ask the user.

Cite exact locations such as `docs/legacy/identity-design.md §15.1`, using the actual existing repository-relative path, not “the old spec.”

## Backlinks and immutability

When creating a Plan, add it to the Spec's `Related Plans` metadata if repository policy permits metadata maintenance. When creating an amendment/supersession, add a metadata-only backlink to the predecessor if safe. Do not make backlink updates when the predecessor is immutable by repository rule; the forward link remains authoritative.

For Version 9, verify §6.3 time applicability and every TIME row against affected fields, §14 cases and MC-TIME-001. Read `references/time-semantics.md`; UTC instant storage/wire, pure-date stability, explicit business/display zones, DST/precision and legacy compatibility require evidence. Older template versions keep their original structural validation; current affected work still needs effective time decisions.
