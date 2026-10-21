#!/usr/bin/env bash
set -Eeuo pipefail
script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd -P)"
root="$(cd "$script_dir/.." && pwd -P)"
"$script_dir/generate_archetypes.sh" check
"$root/mvnw" -B -ntp -f "$root/stellar-enclosure-archetypes/pom.xml" \
  -Pgenerated-archetypes -DskipTests -DskipITs -Darchetype.test.skip=true verify
