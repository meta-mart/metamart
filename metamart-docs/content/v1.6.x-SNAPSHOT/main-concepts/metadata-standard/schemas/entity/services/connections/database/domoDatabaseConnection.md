---
title: domoDatabaseConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/database/domodatabaseconnection
---

# DomoDatabaseConnection

*Domo Database Connection Config*

## Properties

- **`type`**: Service Type. Refer to *#/definitions/domoDatabaseType*. Default: `DomoDatabase`.
- **`clientId`** *(string)*: Client ID for DOMO.
- **`secretToken`** *(string)*: Secret Token to connect DOMO.
- **`accessToken`** *(string)*: Access token to connect to DOMO.
- **`apiHost`** *(string)*: API Host to connect to DOMO instance. Default: `api.domo.com`.
- **`instanceDomain`** *(string)*: URL of your Domo instance, e.g., https://metamart.domo.com.
- **`databaseName`** *(string)*: Optional name to give to the database in MetaMart. If left blank, we will use default as the database name.
- **`supportsMetadataExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsMetadataExtraction*.
## Definitions

- **`domoDatabaseType`** *(string)*:  service type. Must be one of: `['DomoDatabase']`. Default: `DomoDatabase`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
