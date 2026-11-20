# Kubernetes delivery

Supply the verified image digest, namespace and workload identity through the platform release inventory. The example registry name reserves the image field for that digest. The application runs as a numeric non-root user with a read-only root and an independent management listener on port 9090.

- Provide versioned, immutable settings and boot ConfigMaps. Supply database, messaging and storage coordinates, including approved Redis Cluster nodes and the `EGON_ID_REDIS_ENV` and `EGON_ID_REDIS_APP` lease scope.
- Store connection credentials and optional protected Config Data in the separately provisioned Secret. Boot loads `/etc/stellar/runtime/application.yml` and the existing `/run/secrets/` Config Data. The application uses an explicit ServiceAccount with automatic API token mounting disabled.
- Permit only the relevant sources and ports in the approved Cilium policy. Kubernetes owns the protected probes. The release inventory owns the identities and addresses of the allowed backends. Public HTTPS, if authorized, is published through Yuheng and Cilium Gateway API.

Light Open serves HTTP and GraphQL through its ClusterIP on port 8080. For the optional external HTTP clients, supply the approved `USER_SERVICE_BASE_URL` and `TEACHING_SERVICE_BASE_URL` on the internal Service addresses.
