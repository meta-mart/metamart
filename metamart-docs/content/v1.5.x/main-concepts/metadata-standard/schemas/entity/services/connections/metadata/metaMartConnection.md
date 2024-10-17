---
title: metaMartConnection
slug: /main-concepts/metadata-standard/schemas/entity/services/connections/metadata/metamartconnection
---

# MetaMartConnection

*MetaMart Connection Config*

## Properties

- **`clusterName`** *(string)*: Cluster name to differentiate MetaMart Server instance. Default: `metamart`.
- **`type`**: Service Type. Refer to *#/definitions/metamartType*. Default: `MetaMart`.
- **`hostPort`** *(string)*: MetaMart Server Config. Must include API end point ex: http://localhost:8585/api. Default: `http://localhost:8585/api`.
- **`authProvider`**: Refer to *#/definitions/authProvider*.
- **`verifySSL`**: Refer to *../../../../security/ssl/verifySSLConfig.json#/definitions/verifySSL*. Default: `no-ssl`.
- **`sslConfig`**: Refer to *../../../../security/ssl/verifySSLConfig.json#/definitions/sslConfig*.
- **`securityConfig`**: MetaMart Client security configuration.
- **`secretsManagerProvider`**: Refer to *./../../../../security/secrets/secretsManagerProvider.json*. Default: `noop`.
- **`secretsManagerLoader`**: Refer to *./../../../../security/secrets/secretsManagerClientLoader.json*. Default: `noop`.
- **`apiVersion`** *(string)*: MetaMart server API version to use. Default: `v1`.
- **`includeTopics`** *(boolean)*: Include Topics for Indexing. Default: `True`.
- **`includeTables`** *(boolean)*: Include Tables for Indexing. Default: `True`.
- **`includeDashboards`** *(boolean)*: Include Dashboards for Indexing. Default: `True`.
- **`includePipelines`** *(boolean)*: Include Pipelines for Indexing. Default: `True`.
- **`includeMlModels`** *(boolean)*: Include MlModels for Indexing. Default: `True`.
- **`includeUsers`** *(boolean)*: Include Users for Indexing. Default: `True`.
- **`includeTeams`** *(boolean)*: Include Teams for Indexing. Default: `True`.
- **`includeGlossaryTerms`** *(boolean)*: Include Glossary Terms for Indexing. Default: `True`.
- **`includeTags`** *(boolean)*: Include Tags for Indexing. Default: `True`.
- **`includePolicy`** *(boolean)*: Include Tags for Policy. Default: `True`.
- **`includeMessagingServices`** *(boolean)*: Include Messaging Services for Indexing. Default: `True`.
- **`enableVersionValidation`** *(boolean)*: Validate Metamart Server & Client Version. Default: `True`.
- **`includeDatabaseServices`** *(boolean)*: Include Database Services for Indexing. Default: `True`.
- **`includePipelineServices`** *(boolean)*: Include Pipeline Services for Indexing. Default: `True`.
- **`limitRecords`** *(integer)*: Limit the number of records for Indexing. Default: `1000`.
- **`forceEntityOverwriting`** *(boolean)*: Force the overwriting of any entity during the ingestion. Default: `False`.
- **`storeServiceConnection`** *(boolean)*: If set to true, when creating a service during the ingestion we will store its Service Connection. Otherwise, the ingestion will create a bare service without connection details. Default: `True`.
- **`elasticsSearch`** *(object)*: Configuration for Sink Component in the MetaMart Ingestion Framework. Cannot contain additional properties.
  - **`type`** *(string)*: Type of sink component ex: metadata.
  - **`config`**: Refer to *../../../../type/basic.json#/definitions/componentConfig*.
- **`supportsDataInsightExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsDataInsightExtraction*.
- **`supportsElasticSearchReindexingExtraction`**: Refer to *../connectionBasicType.json#/definitions/supportsElasticSearchReindexingExtraction*.
- **`extraHeaders`**: Refer to *#/definitions/extraHeaders*.
## Definitions

- **`metamartType`** *(string)*: MetaMart service type. Must be one of: `['MetaMart']`. Default: `MetaMart`.
- **`extraHeaders`** *(object)*: Additional headers to be sent to the API endpoint. Can contain additional properties.
  - **Additional Properties** *(string)*
- **`authProvider`** *(string)*: MetaMart Server Authentication Provider. Make sure configure same auth providers as the one configured on MetaMart server. Must be one of: `['no-auth', 'basic', 'azure', 'google', 'okta', 'auth0', 'aws-cognito', 'custom-oidc', 'ldap', 'saml', 'metamart']`. Default: `basic`.


Documentation file automatically generated at 2023-10-27 13:55:46.343512.
