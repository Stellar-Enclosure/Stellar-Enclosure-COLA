#!/usr/bin/env bash
# Offline maintenance checks; no Maven generation, install, application or service startup.
set -Eeuo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
SKILLS_ROOT="$(cd "$SCRIPT_DIR/.." && pwd -P)"
readonly SCRIPT_DIR SKILLS_ROOT
readonly SPEC_ROOT="$SKILLS_ROOT/stellar-enclosure-coding-writing-spec"

for entry in "$SKILLS_ROOT"/stellar-enclosure-coding-*/SKILL.md; do
    skill="${entry%/SKILL.md}"
    printf '\nChecking %s\n' "${skill##*/}"
    # The same resource check also covers the small scaffold-only skill,
    # without requiring another copy of the validator in that package.
    validator="$skill/scripts/validate_skill_resources.py"
    [[ -f "$validator" ]] || validator="$SPEC_ROOT/scripts/validate_skill_resources.py"
    python3 -B "$validator" --root "$skill"

    if [[ -d "$skill/scripts" ]]; then
        python3 -B -m unittest discover -s "$skill/scripts" -p 'test_*.py'
        for helper in "$skill/scripts/"*.sh; do
            [[ -f "$helper" ]] && bash -n "$helper"
        done
    fi
done

# These resources intentionally ship in each standalone skill. Phase-specific
# standards/checklists are not identical and must not be folded into this list.
for suffix in writing-plan execute-plan; do
    peer="$SKILLS_ROOT/stellar-enclosure-coding-$suffix"
    for resource in \
        references/backend-code-generation.md \
        references/egon-java-cqe-contract.md \
        references/outbox-statemachine-integration.md \
        references/outbox-statemachine-integration.zh-CN.md \
        references/time-semantics.md \
        references/time-semantics.zh-CN.md \
        scripts/stellar-enclosure-codegen.sh; do
        if ! cmp -s "$SPEC_ROOT/$resource" "$peer/$resource"; then
            printf 'FAIL: shared resource differs: %s (%s)\n' "$resource" "${peer##*/}" >&2
            exit 1
        fi
    done
done

for helper in "$SCRIPT_DIR/"*.sh; do
    bash -n "$helper"
done
bash "$SCRIPT_DIR/test-link-skills.sh"
printf '\nPASS: all skill maintenance checks passed\n'
