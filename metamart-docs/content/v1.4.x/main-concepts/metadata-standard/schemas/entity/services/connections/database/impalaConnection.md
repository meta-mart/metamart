---
title: impalaConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/database/impalaconnection
---

# ImpalaConnection

*Impala SQL Connection Config*

## Properties

- **`type`**: Service Type. Refer to *#/definitions/impalaType*. Default: `Impala`.
- **`scheme`**: SQLAlchemy driver scheme options. Refer to *#/definitions/impalaScheme*. Default: `impala`.
- **`username`** *(string)*: Username to connect to Impala. This user should have privileges to read all the metadata in Impala.
- **`password`** *(string)*: Password to connect to Impala.
- **`hostPort`** *(string)*: Host and port of the Impala service.
- **`authMechanism`** *(string)*: Authentication mode to connect to Impala. Must be one of: `['NOSASL', 'PLAIN', 'GSSAPI', 'LDAP', 'JWT']`. Default: `PLAIN`.
- **`kerberosServiceName`** *(string)*: If authenticating with Kerberos specify the Kerberos service name.
- **`databaseSchema`** *(string)*: Database Schema of the data source. This is optional parameter, if you would like to restrict the metadata reading to a single schema. When left blank, MetaMart Ingestion attempts to scan all the schemas.
- **`databaseName`** *(string)*: Optional name to give to the database in MetaMart. If left blank, we will use default as the database name.
- **`useSSL`** *(boolean)*: Establish secure connection with Impala.
- **`authOptions`** *(string)*: Authentication options to pass to Impala connector. These options are based on SQLAlchemy.
- **`connectionOptions`**: Refer to *../connectionBasicType.json#/definitions/connectionOptions*.
- **`connectionArguments`**: Refer to *../connectionBasicType.json#/definitions/connectionArguments*.
- **`supportsMetadataExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsMetadataExtraction*.
- **`supportsDBTExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsDBTExtraction*.
- **`supportsProfiler`**: Refer to *../connectionBasicType.json#/definitions/supportsProfiler*.
## Definitions

- **`impalaType`** *(string)*: Service type. Must be one of: `['Impala']`. Default: `Impala`.
- **`impalaScheme`** *(string)*: SQLAlchemy driver scheme options. Must be one of: `['impala', 'impala4']`. Default: `impala`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
