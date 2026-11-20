# Stellar Enclosure React Admin

React administration project using the shared Stellar Enclosure layout, HTTP client, identity and authorization SDKs.

Requires Node 24 and npm 11. When generating the project, use a lowercase npm-compatible artifact ID and an npm semantic version such as `0.1.0-SNAPSHOT`. Configure `.env.local` from `.env.example` and set the real business and application codes in `permission-manifest.config.json`. Provision a tenant PUBLIC client, a Resource audience, and the application API URL served by the Cilium Gateway. Set the console's HTTPS origin and the issuer's exact authorization, token and JWKS endpoints in the public environment file. Register the exact console callbacks at `<web-origin><base-path>oauth/callback` and `<web-origin><base-path>oauth/popup-callback`. Serve the SPA for both paths and permit its exact origin on the identity and Resource endpoints where cross-origin requests are required.

The shared SDK uses authorization code with PKCE and holds short-lived access tokens in memory. API calls go directly to the configured Resource URL with an audience-bound Bearer token and omitted cookies. The corresponding permission snapshot must match the signed identity and the configured namespace and environment. An embedded instance can authorize through an application-owned popup when the browser opener shares the configured console origin. Cross-origin embeds and embedded step-up actions link to the standalone console.

The source project includes a public home resource and a codegen-owned `src/generated/permissions.ts`. Generated feature routes are discovered from `src/features/**/*.routes.tsx`; their statically declared resources are collected in that owned file. Do not manually edit the generated registry. Its codegen state records the template baseline in a project produced by the archetype.

## Build and permissions

The package lock pins the reviewed OAuth-capable SDK 0.1.0 artifacts. Use the byte-identical registry packages once published. For isolated validation before promotion, resolve the two reviewed local tarballs in a disposable copy of the lock and retain their recorded integrity values.

Set `PAC_SDK_DIR` to the reviewed web-sdk checkout, `PAC_TOOLCHAIN_LOCK` to the included lock, and `PAC_CONTRACT_ARTIFACT` to the exact contract JAR named by that lock. `npm run build` invokes the shared permission build and emits a FULL manifest plus artifact evidence. The included lock pins a real SDK commit and contract digest; the commit must be available to the build runner. A `LOCAL_UNCOMMITTED` lock can instead be supplied for local checks only. Production delivery requires the clean pinned SDK checkout, a digest-verified contract and a configured `permission-targets.json`.

```sh
npm ci
npm run typecheck
npm test
npm run build
```

The included GitHub workflow performs offline validation on ordinary pushes and pull requests. The separate protected `permission-delivery` workflow invokes the shared SDK adapter for candidate registration or activation. Enable it only after committing a real `PINNED_COMMIT` lock and configuring the target, protected environments, credentials and a trusted deployment workflow. Registration does not grant roles; activation requires trusted deployment evidence and the complete producer set. See the SDK permissions README for the required target, credential, revision and operation ID. Never place credentials in this repository.

The template itself does not establish a live login, deployed authorization service or remote registration. Generated files must be reviewed with the backend API and business rules.
