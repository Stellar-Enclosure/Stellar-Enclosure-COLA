# OCI image delivery

The portable Dockerfile builds the executable from source and runs it as a numeric non-root user. Docker, Podman and nerdctl can build the same image from the repository root.

```bash
docker build --build-arg CONTAINER_ENGINE=docker -f deploy/container/Dockerfile -t application:local .
podman build --build-arg CONTAINER_ENGINE=podman -f deploy/container/Dockerfile -t application:local .
nerdctl build --build-arg CONTAINER_ENGINE=nerdctl -f deploy/container/Dockerfile -t application:local .
```

Jenkins can build and publish the image using the selected engine and registry credentials. Deploy its verified digest through the [Kubernetes delivery contract](../kubernetes/README.md). Kubernetes sets runtime inputs, mounts the configuration and Pod credentials, and probes the management listener. The platform provisions persistent data stores and manages public routes.
