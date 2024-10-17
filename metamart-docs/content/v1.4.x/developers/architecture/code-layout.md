---
title: Understand Code Layout
slug: /developers/architecture/code-layout
---

# Understand Code Layout
Use this document as a quick start guide to begin developing in MetaMart. Below, we address the following topics:

1. Schema (Metadata Models)
2. APIs
3. System and Components

## Schema (Metadata Models)
MetaMart takes a schema-first approach to model metadata. We define entities, types, API requests, and relationships between entities. We define the MetaMart schema using the [JSON Schema](https://json-schema.org/) vocabulary.

We convert models defined using JSON Schema to [Plain Old Java Objects (POJOs)](https://www.jsonschema2pojo.org/) using the `jsonschema2pojo-maven-plugin` plugin as defined in [`pom.xml`](https://github.com/meta-mart/MetaMart/blob/main/metamart-service/pom.xml#L517). You can find the generated POJOs under `MetaMart/metamart-service/target/generated-sources/jsonschema2pojo`.

### Entities
You can locate defined entities in the directory [`MetaMart/metamart-spec/src/main/resources/json/schema/entity`](https://github.com/meta-mart/MetaMart/tree/main/metamart-spec/src/main/resources/json/schema/entity). Currently, MetaMart supports the following entities:

- data
- feed
- policies
- services
- tags
- teams

### Types
All MetaMart supported types are defined under [`MetaMart/metamart-spec/src/main/resources/json/schema/type`](https://github.com/meta-mart/MetaMart/tree/main/metamart-spec/src/main/resources/json/schema/type). 
### API request objects
The API request objects are defined under [`MetaMart/metamart-spec/src/main/resources/json/schema/api`](https://github.com/meta-mart/MetaMart/tree/main/metamart-spec/src/main/resources/json/schema/api).

## API
MetaMart uses the [Dropwizard](https://www.dropwizard.io/) Java framework to build REST APIs. You can locate defined APIs in the directory [`MetaMart/metamart-service/src/main/java/org/metamart/service/resources`](https://github.com/meta-mart/MetaMart/tree/main/metamart-service/src/main/java/org/metamart/service/resources). MetaMart uses [Swagger](https://swagger.io/) to generate API documentation following OpenAPI standards.

## System and Components

{% image src="/images/v1.4/developers/architecture/architecture.png" alt="System and Components" caption="Overview of the MetaMart components and high-level interactions." /%}

### Events
MetaMart captures changes to entities as `events` and stores them in the MetaMart server database. MetaMart also indexes change events in Elasticsearch to make them searchable.

The event handlers are defined under [`MetaMart/metamart-service/src/main/java/org/metamart/service/events`](https://github.com/meta-mart/MetaMart/tree/main/metamart-service/src/main/java/org/metamart/service/events) and are applied globally to any outgoing response using the `ContainerResponseFilter`.

### Database
MetaMart uses MySQL or Postgres for the metadata catalog. The catalog code is located in the directory [`MetaMart/metamart-service/src/main/java/org/metamart/service/jdbi3`](https://github.com/meta-mart/MetaMart/tree/main/metamart-service/src/main/java/org/metamart/service/jdbi3).

The database entity tables are created with the script [`MetaMart/bootstrap/metamart-ops.sh`](https://github.com/meta-mart/MetaMart/blob/main/bootstrap/metamart-ops.sh). [Flyway](https://flywaydb.org/) is used for managing the database table versions.

### Elasticsearch
MetaMart uses Elasticsearch to store the Entity change events and makes it searchable by search index. The [`MetaMart/metamart-service/src/main/java/org/metamart/service/elasticsearch/ElasticSearchEventPublisher.java`](https://github.com/meta-mart/MetaMart/blob/main/metamart-service/src/main/java/org/metamart/service/elasticsearch/ElasticSearchEventPublisher.java) is responsible for capturing the change events and updating Elasticsearch.

Elasticsearch indices are created when the [`MetaMart/ingestion/pipelines/metadata_to_es.json`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/pipelines/metadata_to_es.json) ingestion connector is run.

### Authentication/Authorization
MetaMart uses Google OAuth for authentication. All incoming requests are filtered by validating the JWT token using the Google OAuth provider. Access control is provided by [`Authorizer`](https://github.com/meta-mart/MetaMart/blob/main/metamart-service/src/main/java/org/metamart/service/security/Authorizer.java).

See the configuration file `MetaMart` [`/conf/metamart.yaml`](https://github.com/meta-mart/MetaMart/blob/main/conf/metamart.yaml) for the authentication and authorization configurations.

### Ingestion
Ingestion is a simple Python framework to ingest metadata from external sources into MetaMart.

**Connectors**

MetaMart defines and uses a set of components called `Connectors` for metadata ingestion. Each data service requires its own connector. See the documentation on how to [build a connector]() for details on developing connectors for new services.

1. Workflow [`MetaMart/ingestion/src/metadata/ingestion/api/workflow.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/workflow.py)
2. Source [`MetaMart/ingestion/src/metadata/ingestion/api/source.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/source.py)
3. Processor [`MetaMart/ingestion/src/metadata/ingestion/api/processor.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/processor.py)
4. Sink [`MetaMart/ingestion/src/metadata/ingestion/api/sink.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/sink.py)
5. Stage [`MetaMart/ingestion/src/metadata/ingestion/api/stage.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/stage.py)
6. BulkSink [`MetaMart/ingestion/src/metadata/ingestion/api/bulk_sink.py`](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/api/bulk_sink.py)

Workflow is a simple orchestration job that runs `Source`, `Processor`, `Sink`, `Stage` and `BulkSink` based on the configurations present under [`MetaMart/ingestion/examples/workflows`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/examples/workflows).

There are some popular connectors already developed and can be found under:

1. Source → [`MetaMart/ingestion/src/metadata/ingestion/source`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/ingestion/source)
2. Processor → [`MetaMart/ingestion/src/metadata/ingestion/processor`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/ingestion/processor)
3. Sink → [`MetaMart/ingestion/src/metadata/ingestion/sink`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/ingestion/sink)
4. Stage → [`MetaMart/ingestion/src/metadata/ingestion/stage`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/ingestion/stage)
5. BulkSink → [`MetaMart/ingestion/src/metadata/ingestion/bulksink`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/src/metadata/ingestion/bulksink)

**Airflow**

For simplicity, MetaMart ingests metadata from external sources using a pull-based model. MetaMart uses Apache Airflow to orchestrate ingestion workflows.

See the directory [`MetaMart/ingestion/examples/airflow/dags`](https://github.com/meta-mart/MetaMart/tree/main/ingestion/examples/airflow/dags) for reference DAG definitions.

**JsonSchema python typings**

You can generate Python types for MetaMart models defined using Json Schema using the make generate command of the [`Makefile`](https://github.com/meta-mart/MetaMart/blob/main/Makefile). Generated files are located in the directory `MetaMart/ingestion/src/metadata/generated`
