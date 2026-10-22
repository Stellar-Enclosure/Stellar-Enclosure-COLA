# Stellar-Enclosure-COLA

Stellar Enclosure Java modules. All first-party modules start at **0.0.1**.

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

The four skills are under `.agents/skills/stellar-enclosure-coding-*`. Their resources and original normative rule blocks remain included. `scripts/link-skills.sh` supports explicit links to other checkouts.

Seven authoritative archetype families live under `stellar-enclosure-archetypes/source-projects` and `definitions`. Never edit or commit `.generated`. After bootstrap:

```bash
./scripts/generate_archetypes.sh generate
./scripts/generate_archetypes.sh check
```

The CLI launcher is `scripts/stellar-enclosure-codegen.sh`. Its implementation is the Components artifact `top.egon:stellar-enclosure-component-code-generator:0.0.1`; the existing explicit `EGON_CODEGEN_CLASSPATH` contract is preserved.

## Publish to Maven Central

This repository publishes its archetype parent, seven generated archetypes and four peer facade contracts needed by generated projects. The COLA root aggregator, generated reactor wrapper and all other source-project examples are excluded. Dependencies from Parent, Components and the platform Starters must already be published.

Configure Maven server `central` with your Central Portal token and a local GPG key, as described in the [release guide](https://github.com/Stellar-Enclosure/Stellar-Enclosure-Parent/blob/main/docs/first-release.md). Run from this repository:

```bash
(
set -e
./scripts/generate_archetypes.sh generate
./scripts/generate_archetypes.sh check

# Publish the parent once. It must retain its dependency/plugin management.
./mvnw -B -ntp -N -f stellar-enclosure-archetypes/pom.xml -Prelease clean deploy

# Only the four facade contracts use flattened consumer POMs.
for family in service web service-open web-open; do
  ./mvnw -B -ntp \
    -f "stellar-enclosure-archetypes/source-projects/stellar-enclosure-source-${family}/stellar-enclosure-source-${family}-facade/pom.xml" \
    -Prelease,publish-resolved-facade clean deploy
done

# Select the seven archetypes without publishing the parent/facades again.
./mvnw -B -ntp -f stellar-enclosure-archetypes/pom.xml \
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
