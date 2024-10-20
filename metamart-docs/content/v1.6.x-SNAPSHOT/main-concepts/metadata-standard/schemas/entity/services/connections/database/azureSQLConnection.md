---
title: azureSQLConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/database/azuresqlconnection
---

# AzureSQLConnection

*Azure SQL Connection Config*

## Properties

- **`type`**: Service Type. Refer to *#/definitions/azureSQLType*. Default: `AzureSQL`.
- **`scheme`**: SQLAlchemy driver scheme options. Refer to *#/definitions/azureSQLScheme*. Default: `mssql+pyodbc`.
- **`username`** *(string)*: Username to connect to AzureSQL. This user should have privileges to read the metadata.
- **`password`** *(string)*: Password to connect to AzureSQL.
- **`hostPort`** *(string)*: Host and port of the AzureSQL service.
- **`database`** *(string)*: Database of the data source. This is optional parameter, if you would like to restrict the metadata reading to a single database. When left blank, MetaMart Ingestion attempts to scan all the databases.
- **`driver`** *(string)*: SQLAlchemy driver for AzureSQL. Default: `ODBC Driver 18 for SQL Server`.
- **`ingestAllDatabases`** *(boolean)*: Ingest data from all databases in Azuresql. You can use databaseFilterPattern on top of this. Default: `False`.
- **`connectionOptions`**: Refer to *../connectionBasicType.json#/definitions/connectionOptions*.
- **`connectionArguments`**: Refer to *../connectionBasicType.json#/definitions/connectionArguments*.
- **`supportsMetadataExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsMetadataExtraction*.
- **`supportsUsageExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsUsageExtraction*.
- **`supportsLineageExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsLineageExtraction*.
- **`supportsDBTExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsDBTExtraction*.
- **`supportsProfiler`**: Refer to *../connectionBasicType.json#/definitions/supportsProfiler*.
- **`supportsDatabase`**: Refer to *../connectionBasicType.json#/definitions/supportsDatabase*.
## Definitions

- **`azureSQLType`** *(string)*: Service type. Must be one of: `['AzureSQL']`. Default: `AzureSQL`.
- **`azureSQLScheme`** *(string)*: SQLAlchemy driver scheme options. Must be one of: `['mssql+pyodbc']`. Default: `mssql+pyodbc`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
