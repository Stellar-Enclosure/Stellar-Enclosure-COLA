# Kubernetes delivery

Supply the verified image digest, namespace and workload identity through the platform release inventory. The example registry name reserves the image field for that digest. The application runs as a numeric non-root user with a read-only root and an independent management listener on port 9090.

- Provide versioned, immutable settings and boot ConfigMaps. Supply database, messaging and storage coordinates, including approved Redis Cluster nodes and the `EGON_ID_REDIS_ENV` and `EGON_ID_REDIS_APP` lease scope.
- Store connection credentials and optional protected Config Data in the separately provisioned Secret. Boot loads `/etc/stellar/runtime/application.yml` and the existing `/run/secrets/` Config Data. The application uses an explicit ServiceAccount with automatic API token mounting disabled.
- Permit only the relevant sources and ports in the approved Cilium policy. Kubernetes owns the protected probes. The release inventory owns the identities and addresses of the allowed backends. Public HTTPS, if authorized, is published through Yuheng and Cilium Gateway API.

The provider's internal front Service uses port 50051 and the existing Triple wire contract. Configure only the approved peer using `EVALUATION_TRIPLE_SERVICE` and `EVALUATION_TRIPLE_NAMESPACE`. The producer GRPCRoute publishes the protobuf method set and selects the stable release. The fallback port 19099 is closed and explicitly denied. Add a verified canary backend before changing weights. The local pinned Triple test exercises the wire protocol. The platform release verifies the real Cilium datapath.

Web Open exposes its HTTP and GraphQL backend through a separate ClusterIP on port 8080.
