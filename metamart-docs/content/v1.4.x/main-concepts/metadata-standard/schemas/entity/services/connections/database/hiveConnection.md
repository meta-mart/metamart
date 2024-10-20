---
title: hiveConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/database/hiveconnection
---

# HiveConnection

*Hive SQL Connection Config*

## Properties

- **`type`**: Service Type. Refer to *#/definitions/hiveType*. Default: `Hive`.
- **`scheme`**: SQLAlchemy driver scheme options. Refer to *#/definitions/hiveScheme*. Default: `hive`.
- **`username`** *(string)*: Username to connect to Hive. This user should have privileges to read all the metadata in Hive.
- **`password`** *(string)*: Password to connect to Hive.
- **`hostPort`** *(string)*: Host and port of the Hive service.
- **`auth`** *(string)*: Authentication mode to connect to hive. Must be one of: `['NONE', 'LDAP', 'KERBEROS', 'CUSTOM', 'NOSASL', 'BASIC', 'GSSAPI', 'JWT', 'PLAIN']`. Default: `NONE`.
- **`kerberosServiceName`** *(string)*: If authenticating with Kerberos specify the Kerberos service name.
- **`databaseSchema`** *(string)*: Database Schema of the data source. This is optional parameter, if you would like to restrict the metadata reading to a single schema. When left blank, MetaMart Ingestion attempts to scan all the schemas.
- **`databaseName`** *(string)*: Optional name to give to the database in MetaMart. If left blank, we will use default as the database name.
- **`authOptions`** *(string)*: Authentication options to pass to Hive connector. These options are based on SQLAlchemy.
- **`metastoreConnection`**: Hive Metastore Connection Details.
- **`connectionOptions`**: Refer to *../connectionBasicType.json#/definitions/connectionOptions*.
- **`connectionArguments`**: Refer to *../connectionBasicType.json#/definitions/connectionArguments*.
- **`supportsMetadataExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsMetadataExtraction*.
- **`supportsDBTExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsDBTExtraction*.
- **`supportsProfiler`**: Refer to *../connectionBasicType.json#/definitions/supportsProfiler*.
## Definitions

- **`hiveType`** *(string)*: Service type. Must be one of: `['Hive']`. Default: `Hive`.
- **`hiveScheme`** *(string)*: SQLAlchemy driver scheme options. Must be one of: `['hive', 'hive+http', 'hive+https']`. Default: `hive`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
