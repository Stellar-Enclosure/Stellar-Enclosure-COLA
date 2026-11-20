# Kubernetes delivery

Use this workload example as input to the platform release assembler. Set the approved namespace, image digest, ServiceAccount identity, and resource names in the release inventory. The example hostname under `example.invalid` reserves the image field for that digest.

- Provision the versioned settings ConfigMap with the app's database and integration coordinates, approved Redis Cluster nodes, `EGON_ID_REDIS_ENV` and `EGON_ID_REDIS_APP`. Use a consistent lease scope across stable and canary releases that can write into the same data space.
- Provide storage, OAuth and integration credentials through the separately managed Secret. Use the scope and token registration agreed with Tianquan, including the dedicated Tianshu config SERVICE delivery registration.
- Use the immutable ConfigMap produced for this workload binding for `baseline.json`. Supply the cluster ID, owner Deployment UID as `WORKLOAD_UID`, binding ID and revision, and baseline validity through the authorized release binding. The Downward API supplies the namespace and Pod identity. The projected token has the Tianshu config audience and rotates at the kubelet. Mount the directory to retain rotation and use the approved internal Tianshu HTTP origin.
- Protect the probe port `9090` with the platform management policy. The application HTTP backend uses `8080` with network admission and resource authorization aligned to the approved workload. Yuheng publishes the approved URL through Cilium Gateway API from its route namespace. Public TLS terminates at the platform Gateway.

The platform owns Cilium policy, release admission, image verification and promotion, and production traffic tests. The workload runs with a read-only root and uses temporary Pod volumes for logs, JVM temporary files and Tianshu local state. A replacement Pod starts from the approved immutable baseline.

For east-west gRPC use the stable `stellar-enclosure-source-light-rpc` Service on port 50051. Its Service-attached GRPCRoute sends the explicitly configured methods to the stable backend. Direct backend access and the reserved 19099 fallback require the platform deny and source admission policies. Add a separate canary Deployment and backend Service through the release assembler before adjusting weights. Keep retries and quotas governed by the caller and access-guard contracts.
