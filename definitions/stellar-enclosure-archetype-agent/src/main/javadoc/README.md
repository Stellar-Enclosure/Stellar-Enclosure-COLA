# Stellar-Enclosure Agent Archetype

This classifier documents the `stellar-enclosure-archetype-agent` Maven Archetype distribution.
It generates a Java 25, Spring Boot six-module Agent project with two business domains. `research` runs a fixed Agent Flow behind one authenticated Server-Sent Events command and keeps its sessions process-local. `knowledge` is durable: it stores documents in PostgreSQL, embeds them into a pgvector store, and answers questions from the retrieved chunks through twelve additional authenticated operations. Model, MCP, embedding and database credentials are supplied by the consuming deployment; the archetype commits none.

## Dependency and runtime ownership

Generated projects inherit the released `top.egon:stellar-enclosure-archetypes-parent` at a concrete version with an empty `relativePath`. The parent imports the Components BOM, manages Common dependencies and ShardingSphere 5.5.3, and keeps Commons Lang at 3.20.0. Consumer modules inherit their own project root. Install the matching parent/BOM and required artifacts locally before validating an unpublished release; a local install does not publish artifacts.

Agent retains its six-module application with Spring AI 2.0.2, Agent Flow and Google ADK. Its runtime libraries include the Agent Flow, RAG, transactional-outbox and two-level cache starters, `spring-ai-pgvector-store`, the Common MyBatis-Plus ShardingSphere-JDBC extension starter (which brings ShardingSphere transitively, and is the only permitted route to it), Flyway, and the PostgreSQL driver. What Agent does not gain is Egon RPC/Tianshu, Nacos, Dubbo, or the organization/evaluation facades; the generated project runs as a single HTTP service. Google ADK may independently bring gRPC/Protobuf; BOM management alone is not a runtime dependency.

## CQERS execution

Generated projects retain the Manage entry point and place one `CmdExe`, `QueryExe`, or `EventExe`
per Application Command, Query, or consumed business Event under the corresponding domain's
`executor` package. Query executors are read-only. Event executors handle inbound business events;
Domain ports and Infrastructure own outbound MQ/Outbox publication. Existing transactions and
wire contracts remain in place. See the generated project README for the exact paths and boundaries.
