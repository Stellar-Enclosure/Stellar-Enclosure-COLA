# Stellar Enclosure React Admin

React administration project using the shared Stellar Enclosure layout, HTTP client, identity and authorization SDKs.

Requires Node 24 and npm 11. When generating the project, use a lowercase npm-compatible artifact ID and an npm semantic version such as `0.1.0-SNAPSHOT`. Configure `.env.local` from `.env.example` and set the real business and application codes in `permission-manifest.config.json`. The Edge application alias, identity client/resource, gateway routes, namespace and environment must be provisioned separately. The application uses the gateway's HttpOnly session; it does not store access tokens in the browser.

The source project includes a public home resource and a codegen-owned `src/generated/permissions.ts`. Generated feature routes are discovered from `src/features/**/*.routes.tsx`; their statically declared resources are collected in that owned file. Do not manually edit the generated registry. Its codegen state records the template baseline in a project produced by the archetype.

## Build and permissions

Install the SDK packages from the reviewed npm artifacts or the published registry. Before the first publication, use the workspace's `web-sdk/scripts/verify-consumers.mjs` preparation or the reviewed local SDK tarballs; do not fabricate registry checksums.

Set `PAC_SDK_DIR` to the reviewed web-sdk checkout, `PAC_TOOLCHAIN_LOCK` to the included lock, and `PAC_CONTRACT_ARTIFACT` to the exact contract JAR named by that lock. `npm run build` invokes the shared permission build and emits a FULL manifest plus artifact evidence. The included lock pins a real SDK commit and contract digest; the commit must be available to the build runner. A `LOCAL_UNCOMMITTED` lock can instead be supplied for local checks only. Production delivery requires the clean pinned SDK checkout, a digest-verified contract and a configured `permission-targets.json`.

```sh
npm ci
npm run typecheck
npm test
npm run build
```

The included GitHub workflow performs offline validation on ordinary pushes and pull requests. The separate protected `permission-delivery` workflow invokes the shared SDK adapter for candidate registration or activation. Enable it only after committing a real `PINNED_COMMIT` lock and configuring the target, protected environments, credentials and a trusted deployment workflow. Registration does not grant roles; activation requires trusted deployment evidence and the complete producer set. See the SDK permissions README for the required target, credential, revision and operation ID. Never place credentials in this repository.

The template itself does not establish a live login, deployed authorization service or remote registration. Generated files must be reviewed with the backend API and business rules.
