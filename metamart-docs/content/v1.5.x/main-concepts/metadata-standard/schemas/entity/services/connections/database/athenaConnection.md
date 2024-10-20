---
title: athenaConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/database/athenaconnection
---

# AthenaConnection

*AWS Athena Connection Config*

## Properties

- **`type`**: Service Type. Refer to *#/definitions/athenaType*. Default: `Athena`.
- **`scheme`**: SQLAlchemy driver scheme options. Refer to *#/definitions/athenaScheme*. Default: `awsathena+rest`.
- **`awsConfig`**: Refer to *../../../../security/credentials/awsCredentials.json*.
- **`s3StagingDir`** *(string)*: S3 Staging Directory. Example: s3://postgres/input/.
- **`workgroup`** *(string)*: Athena workgroup.
- **`databaseName`** *(string)*: Optional name to give to the database in MetaMart. If left blank, we will use default as the database name.
- **`connectionOptions`**: Refer to *../connectionBasicType.json#/definitions/connectionOptions*.
- **`connectionArguments`**: Refer to *../connectionBasicType.json#/definitions/connectionArguments*.
- **`supportsMetadataExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsMetadataExtraction*.
- **`supportsDBTExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsDBTExtraction*.
- **`supportsProfiler`**: Refer to *../connectionBasicType.json#/definitions/supportsProfiler*.
- **`supportsQueryComment`**: Refer to *../connectionBasicType.json#/definitions/supportsQueryComment*.
- **`supportsUsageExtraction`** *(boolean)*: Supports Usage Extraction. Default: `True`.
- **`supportsLineageExtraction`** *(boolean)*: Supports Lineage Extraction. Default: `True`.
## Definitions

- **`athenaType`** *(string)*: Service type. Must be one of: `['Athena']`. Default: `Athena`.
- **`athenaScheme`** *(string)*: SQLAlchemy driver scheme options. Must be one of: `['awsathena+rest']`. Default: `awsathena+rest`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
