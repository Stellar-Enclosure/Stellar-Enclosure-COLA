# React Admin

The project uses the shared EnterpriseLayout and PageTemplate, the shared PUBLIC OAuth client with PKCE and a direct Cilium Gateway Resource URL, and the Jianshen authorization SDK. Backend and frontend permission manifests remain separate producers. Permissions bind to the verified OIDC identity, request epoch and deployment scope. Generated feature permissions are collected through the codegen-owned static registry.
