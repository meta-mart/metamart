---
title: workflow
slug: /main-concepts/metadata-standard/schemas/metadataingestion/workflow
---

# MetadataWorkflow

*MetaMart Ingestion Framework definition.*

## Properties

- **`id`**: Unique identifier that identifies this pipeline. Refer to *../type/basic.json#/definitions/uuid*.
- **`name`**: Name that identifies this pipeline instance uniquely. Refer to *../type/basic.json#/definitions/entityName*.
- **`metaMartWorkflowConfig`** *(object)*: MetaMart Ingestion Workflow Config. Cannot contain additional properties.
  - **`source`**: Refer to *#/definitions/source*.
  - **`processor`**: Refer to *#/definitions/processor*.
  - **`sink`**: Refer to *#/definitions/sink*.
  - **`stage`**: Refer to *#/definitions/stage*.
  - **`bulkSink`**: Refer to *#/definitions/bulkSink*.
  - **`workflowConfig`**: Refer to *#/definitions/workflowConfig*.
  - **`ingestionPipelineFQN`** *(string)*: Fully qualified name of ingestion pipeline, used to identify the current ingestion pipeline.
  - **`pipelineRunId`**: Unique identifier of pipeline run, used to identify the current pipeline run. Refer to *../type/basic.json#/definitions/uuid*.
## Definitions

- **`sourceConfig`** *(object)*: Additional connection configuration. Cannot contain additional properties.
  - **`config`**
- **`source`** *(object)*: Configuration for Source component in MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of the source connector ex: mysql, snowflake, tableau etc..
  - **`serviceName`** *(string)*: Type of the source connector ex: mysql, snowflake, tableau etc..
  - **`serviceConnection`**: Connection configuration for the source. ex: mysql , tableau connection. Refer to *../entity/services/connections/serviceConnection.json#/definitions/serviceConnection*.
  - **`sourceConfig`**: Refer to *#/definitions/sourceConfig*.
- **`processor`** *(object)*: Configuration for Processor Component in the MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of processor component ex: pii-processor.
  - **`config`**: Refer to *../type/basic.json#/definitions/componentConfig*.
- **`stage`** *(object)*: Configuration for Stage Component in the MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of stage component ex: table-usage.
  - **`config`**: Refer to *../type/basic.json#/definitions/componentConfig*.
- **`sink`** *(object)*: Configuration for Sink Component in the MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of sink component ex: metadata.
  - **`config`**: Refer to *../type/basic.json#/definitions/componentConfig*.
- **`bulkSink`** *(object)*: Configuration for BulkSink Component in the MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of BulkSink component ex: metadata-usage.
  - **`config`**: Refer to *../type/basic.json#/definitions/componentConfig*.
- **`logLevels`** *(string)*: Supported logging levels. Must be one of: `['DEBUG', 'INFO', 'WARN', 'ERROR']`. Default: `INFO`.
- **`workflowConfig`** *(object)*: Configuration for the entire Ingestion Workflow. Cannot contain additional properties.
  - **`loggerLevel`**: Refer to *#/definitions/logLevels*. Default: `INFO`.
  - **`metaMartServerConfig`**: Refer to *../entity/services/connections/metadata/metaMartConnection.json*.
  - **`config`**: Refer to *../type/basic.json#/definitions/componentConfig*.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
