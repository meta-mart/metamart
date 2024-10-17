---
title: Kubernetes Helm Values
slug: /deployment/kubernetes/helm-values
digitrans: false
---

# Kubernetes Helm Values

This page list all the supported helm values for MetaMart Helm Charts.

## Metamart Config Chart Values 

{%table%}

| Key | Type | Default | Environment Variable from metamart.yaml | 
|-----|------|---------| ---------------------- |
| metamart.config.authentication.enabled | bool | `true` | |
| metamart.config.authentication.clientType | string | `public` | AUTHENTICATION_CLIENT_TYPE |
| metamart.config.authentication.provider | string | `basic` | AUTHENTICATION_PROVIDER |
| metamart.config.authentication.publicKeys | list | `[http://metamart:8585/api/v1/system/config/jwks]` | AUTHENTICATION_PUBLIC_KEYS |
| metamart.config.authentication.authority | string | `https://accounts.google.com` | AUTHENTICATION_AUTHORITY |
| metamart.config.authentication.clientId | string | `Empty String` | AUTHENTICATION_CLIENT_ID |
| metamart.config.authentication.callbackUrl | string | `Empty String` | AUTHENTICATION_CALLBACK_URL |
| metamart.config.authentication.enableSelfSignup | bool | `true` | AUTHENTICATION_ENABLE_SELF_SIGNUP |
| metamart.config.authentication.jwtPrincipalClaims | list | `[email,preferred_username,sub]` | AUTHENTICATION_JWT_PRINCIPAL_CLAIMS |
| metamart.config.authentication.ldapConfiguration.host | string | `localhost` | AUTHENTICATION_LDAP_HOST |
| metamart.config.authentication.ldapConfiguration.port |int | 10636 | AUTHENTICATION_LDAP_PORT |
| metamart.config.authentication.ldapConfiguration.dnAdminPrincipal | string | `cn=admin,dc=example,dc=com` | AUTHENTICATION_LOOKUP_ADMIN_DN |
| metamart.config.authentication.ldapConfiguration.dnAdminPassword.secretRef | string | `ldap-secret` | AUTHENTICATION_LOOKUP_ADMIN_PWD |
| metamart.config.authentication.ldapConfiguration.dnAdminPassword.secretKey | string | `metamart-ldap-secret` | AUTHENTICATION_LOOKUP_ADMIN_PWD |
| metamart.config.authentication.ldapConfiguration.userBaseDN | string | `ou=people,dc=example,dc=com` | AUTHENTICATION_USER_LOOKUP_BASEDN |
| metamart.config.authentication.ldapConfiguration.groupBaseDN | string | `Empty String` | AUTHENTICATION_GROUP_LOOKUP_BASEDN |
| metamart.config.authentication.ldapConfiguration.roleAdminName | string | `Empty String` | AUTHENTICATION_USER_ROLE_ADMIN_NAME |
| metamart.config.authentication.ldapConfiguration.allAttributeName | string | `Empty String` | AUTHENTICATION_USER_ALL_ATTR |
| metamart.config.authentication.ldapConfiguration.usernameAttributeName | string | `Empty String` | AUTHENTICATION_USER_NAME_ATTR |
| metamart.config.authentication.ldapConfiguration.groupAttributeName | string | `Empty String` | AUTHENTICATION_USER_GROUP_ATTR |
| metamart.config.authentication.ldapConfiguration.groupAttributeValue | string | `Empty String` | AUTHENTICATION_USER_GROUP_ATTR_VALUE |
| metamart.config.authentication.ldapConfiguration.groupMemberAttributeName | string | `Empty String` | AUTHENTICATION_USER_GROUP_MEMBER_ATTR |
| metamart.config.authentication.ldapConfiguration.authRolesMapping | string | `Empty String` | AUTH_ROLES_MAPPING |
| metamart.config.authentication.ldapConfiguration.authReassignRoles | string | `Empty String` | AUTH_REASSIGN_ROLES |
| metamart.config.authentication.ldapConfiguration.mailAttributeName | string | `email` | AUTHENTICATION_USER_MAIL_ATTR |
| metamart.config.authentication.ldapConfiguration.maxPoolSize | int | 3 | AUTHENTICATION_LDAP_POOL_SIZE |
| metamart.config.authentication.ldapConfiguration.sslEnabled | bool | `true` | AUTHENTICATION_LDAP_SSL_ENABLED |
| metamart.config.authentication.ldapConfiguration.truststoreConfigType | string | `TrustAll` | AUTHENTICATION_LDAP_TRUSTSTORE_TYPE |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFilePath | string | `Empty String` | AUTHENTICATION_LDAP_TRUSTSTORE_PATH |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFilePassword.secretRef | string | `Empty String` | AUTHENTICATION_LDAP_KEYSTORE_PASSWORD |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFilePassword.secretKey | string | `Empty String` | AUTHENTICATION_LDAP_KEYSTORE_PASSWORD |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.trustStoreFileFormat | string | `Empty String` | AUTHENTICATION_LDAP_SSL_KEY_FORMAT |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.verifyHostname | string | `Empty String` | AUTHENTICATION_LDAP_SSL_VERIFY_CERT_HOST |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.customTrustManagerConfig.examineValidityDate | bool | `true` | AUTHENTICATION_LDAP_EXAMINE_VALIDITY_DATES |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.hostNameConfig.allowWildCards | bool | `false` | AUTHENTICATION_LDAP_ALLOW_WILDCARDS |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.hostNameConfig.acceptableHostNames | string | `[Empty String]` | AUTHENTICATION_LDAP_ALLOWED_HOSTNAMES |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.jvmDefaultConfig.verifyHostname | string | `Empty String` | AUTHENTICATION_LDAP_SSL_VERIFY_CERT_HOST |
| metamart.config.authentication.ldapConfiguration.trustStoreConfig.trustAllConfig.examineValidityDates | bool | `true` | AUTHENTICATION_LDAP_EXAMINE_VALIDITY_DATES |
| metamart.config.authentication.oidcConfiguration.callbackUrl | string | `http://metamart:8585/callback` | OIDC_CALLBACK |
| metamart.config.authentication.oidcConfiguration.clientAuthenticationMethod | string | `client_secret_post` | OIDC_CLIENT_AUTH_METHOD |
| metamart.config.authentication.oidcConfiguration.clientId.secretKey | string | `metamart-oidc-client-id` | OIDC_CLIENT_ID |
| metamart.config.authentication.oidcConfiguration.clientId.secretRef | string | `oidc-secrets` | OIDC_CLIENT_ID |
| metamart.config.authentication.oidcConfiguration.clientSecret.secretKey | string | `metamart-oidc-client-secret` | OIDC_CLIENT_SECRET |
| metamart.config.authentication.oidcConfiguration.clientSecret.secretRef | string | `oidc-secrets` | OIDC_CLIENT_SECRET |
| metamart.config.authentication.oidcConfiguration.customParams | string | `Empty` | OIDC_CUSTOM_PARAMS |
| metamart.config.authentication.oidcConfiguration.disablePkce | bool | true | OIDC_DISABLE_PKCE |
| metamart.config.authentication.oidcConfiguration.discoveryUri | string | `Empty` | OIDC_DISCOVERY_URI |
| metamart.config.authentication.oidcConfiguration.enabled | bool | false | |
| metamart.config.authentication.oidcConfiguration.maxClockSkew | string | `Empty` | OIDC_MAX_CLOCK_SKEW |
| metamart.config.authentication.oidcConfiguration.oidcType | string | `Empty` | OIDC_TYPE |
| metamart.config.authentication.oidcConfiguration.preferredJwsAlgorithm | string | `RS256` | OIDC_PREFERRED_JWS |
| metamart.config.authentication.oidcConfiguration.responseType | string | `code` | OIDC_RESPONSE_TYPE |
| metamart.config.authentication.oidcConfiguration.scope | string | `openid email profile` | OIDC_SCOPE |
| metamart.config.authentication.oidcConfiguration.serverUrl | string | `http://metamart:8585` | OIDC_SERVER_URL |
| metamart.config.authentication.oidcConfiguration.tenant | string | `Empty` | OIDC_TENANT |
| metamart.config.authentication.oidcConfiguration.useNonce | bool | `true` | OIDC_USE_NONCE |
| metamart.config.authentication.saml.debugMode | bool | false | SAML_DEBUG_MODE |
| metamart.config.authentication.saml.idp.entityId | string | `Empty` | SAML_IDP_ENTITY_ID |
| metamart.config.authentication.saml.idp.ssoLoginUrl |  string | `Empty` | SAML_IDP_SSO_LOGIN_URL |
| metamart.config.authentication.saml.idp.idpX509Certificate.secretRef | string | `Empty` | SAML_IDP_CERTIFICATE |
| metamart.config.authentication.saml.idp.idpX509Certificate.secretKey |  string | `Empty` | SAML_IDP_CERTIFICATE |
| metamart.config.authentication.saml.idp.authorityUrl | string | `http://metamart:8585/api/v1/saml/login` | SAML_AUTHORITY_URL |
| metamart.config.authentication.saml.idp.nameId | string | `urn:oasis:names:tc:SAML:2.0:nameid-format:emailAddress` | SAML_IDP_NAME_ID |
| metamart.config.authentication.saml.sp.entityId | string | `http://metamart:8585/api/v1/saml/acs` | SAML_SP_ENTITY_ID |
| metamart.config.authentication.saml.sp.acs | string | `http://metamart:8585/api/v1/saml/acs` | SAML_SP_ACS |
| metamart.config.authentication.saml.sp.spX509Certificate.secretRef | string | `Empty`  | SAML_SP_CERTIFICATE |
| metamart.config.authentication.saml.sp.spX509Certificate.secretKey | string | `Empty`  | SAML_SP_CERTIFICATE |
| metamart.config.authentication.saml.sp.callback | string | `http://metamart:8585/saml/callback` | SAML_SP_CALLBACK |
| metamart.config.authentication.saml.security.strictMode |  bool | false | SAML_STRICT_MODE |
| metamart.config.authentication.saml.security.tokenValidity | int | 3600 | SAML_SP_TOKEN_VALIDITY |
| metamart.config.authentication.saml.security.sendEncryptedNameId | bool | false | SAML_SEND_ENCRYPTED_NAME_ID |
| metamart.config.authentication.saml.security.sendSignedAuthRequest | bool | false | SAML_SEND_SIGNED_AUTH_REQUEST |
| metamart.config.authentication.saml.security.signSpMetadata | bool  | false | SAML_SIGNED_SP_METADATA |
| metamart.config.authentication.saml.security.wantMessagesSigned | bool  | false | SAML_WANT_MESSAGE_SIGNED |
| metamart.config.authentication.saml.security.wantAssertionsSigned | bool  | false | SAML_WANT_ASSERTION_SIGNED |
| metamart.config.authentication.saml.security.wantAssertionEncrypted | bool  | false | SAML_WANT_ASSERTION_ENCRYPTED |
| metamart.config.authentication.saml.security.wantNameIdEncrypted | bool  | false | SAML_WANT_NAME_ID_ENCRYPTED |
| metamart.config.authentication.saml.security.keyStoreFilePath |  string | `Empty` | SAML_KEYSTORE_FILE_PATH |
| metamart.config.authentication.saml.security.keyStoreAlias.secretRef | string  | `Empty` | SAML_KEYSTORE_ALIAS |
| metamart.config.authentication.saml.security.keyStoreAlias.secretKey | string  | `Empty` | SAML_KEYSTORE_ALIAS |
| metamart.config.authentication.saml.security.keyStorePassword.secretRef | string  | `Empty` | SAML_KEYSTORE_PASSWORD |
| metamart.config.authentication.saml.security.keyStorePassword.secretKey | string  | `Empty` | SAML_KEYSTORE_PASSWORD |
| metamart.config.authorizer.enabled | bool | `true` | |
| metamart.config.authorizer.allowedEmailRegistrationDomains | list | `[all]` | AUTHORIZER_ALLOWED_REGISTRATION_DOMAIN |
| metamart.config.authorizer.className | string | `org.metamart.service.security.DefaultAuthorizer` | AUTHORIZER_CLASS_NAME |
| metamart.config.authorizer.containerRequestFilter | string | `org.metamart.service.security.JwtFilter` | AUTHORIZER_REQUEST_FILTER |
| metamart.config.authorizer.enforcePrincipalDomain | bool | `false` | AUTHORIZER_ENFORCE_PRINCIPAL_DOMAIN |
| metamart.config.authorizer.enableSecureSocketConnection | bool | `false` | AUTHORIZER_ENABLE_SECURE_SOCKET |
| metamart.config.authorizer.initialAdmins | list | `[admin]` | AUTHORIZER_ADMIN_PRINCIPALS |
| metamart.config.authorizer.principalDomain | string | `meta-mart.org` | AUTHORIZER_PRINCIPAL_DOMAIN |
| metamart.config.airflow.auth.password.secretRef | string | `airflow-secrets` | AIRFLOW_PASSWORD |
| metamart.config.airflow.auth.password.secretKey | string | `metamart-airflow-password` | AIRFLOW_PASSWORD |
| metamart.config.airflow.auth.username | string | `admin` | AIRFLOW_USERNAME |
| metamart.config.airflow.enabled | bool | `true` | |
| metamart.config.airflow.host | string | `http://metamart-dependencies-web:8080` | PIPELINE_SERVICE_CLIENT_ENDPOINT |
| metamart.config.airflow.metamart.serverHostApiUrl | string | `http://metamart:8585/api` | SERVER_HOST_API_URL |
| metamart.config.airflow.sslCertificatePath | string | `/no/path` | PIPELINE_SERVICE_CLIENT_SSL_CERT_PATH |
| metamart.config.airflow.verifySsl | string | `no-ssl` | PIPELINE_SERVICE_CLIENT_VERIFY_SSL |
| metamart.config.clusterName | string | `metamart` | METAMART_CLUSTER_NAME |
| metamart.config.database.enabled | bool | `true` | |
| metamart.config.database.auth.password.secretRef | string | `mysql-secrets` | DB_USER_PASSWORD |
| metamart.config.database.auth.password.secretKey | string | `metamart-mysql-password` | DB_USER_PASSWORD |
| metamart.config.database.auth.username | string | `metamart_user` | DB_USER|
| metamart.config.database.databaseName | string | `metamart_db` | OM_DATABASE |
| metamart.config.database.dbParams| string | `allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC` | DB_PARAMS |
| metamart.config.database.dbScheme| string | `mysql` | DB_SCHEME |
| metamart.config.database.driverClass| string | `com.mysql.cj.jdbc.Driver` | DB_DRIVER_CLASS |
| metamart.config.database.host | string | `mysql` | DB_HOST |
| metamart.config.database.port | int | 3306 | DB_PORT |
| metamart.config.elasticsearch.enabled | bool | `true` | |
| metamart.config.elasticsearch.auth.enabled | bool | `false` | |
| metamart.config.elasticsearch.auth.username | string | `elasticsearch` | ELASTICSEARCH_USER |
| metamart.config.elasticsearch.auth.password.secretRef | string | `elasticsearch-secrets` | ELASTICSEARCH_PASSWORD |
| metamart.config.elasticsearch.auth.password.secretKey | string | `metamart-elasticsearch-password` | ELASTICSEARCH_PASSWORD |
| metamart.config.elasticsearch.host | string | `opensearch` | ELASTICSEARCH_HOST |
| metamart.config.elasticsearch.keepAliveTimeoutSecs | int | `600` | ELASTICSEARCH_KEEP_ALIVE_TIMEOUT_SECS |
| metamart.config.elasticsearch.port | int | 9200 | ELASTICSEARCH_PORT |
| metamart.config.elasticsearch.searchType | string | `opensearch` | SEARCH_TYPE |
| metamart.config.elasticsearch.scheme | string | `http` | ELASTICSEARCH_SCHEME |
| metamart.config.elasticsearch.clusterAlias | string | `Empty String` | ELASTICSEARCH_CLUSTER_ALIAS |
| metamart.config.elasticsearch.searchIndexMappingLanguage | string | `EN`| ELASTICSEARCH_INDEX_MAPPING_LANG |
| metamart.config.elasticsearch.trustStore.enabled | bool | `false` | |
| metamart.config.elasticsearch.trustStore.path | string | `Empty String` | ELASTICSEARCH_TRUST_STORE_PATH |
| metamart.config.elasticsearch.trustStore.password.secretRef | string | `elasticsearch-truststore-secrets` | ELASTICSEARCH_TRUST_STORE_PASSWORD |
| metamart.config.elasticsearch.trustStore.password.secretKey | string | `metamart-elasticsearch-truststore-password` | ELASTICSEARCH_TRUST_STORE_PASSWORD |
| metamart.config.eventMonitor.enabled | bool | `true` | |
| metamart.config.eventMonitor.type | string | `prometheus` | EVENT_MONITOR |
| metamart.config.eventMonitor.batchSize | int | `10` | EVENT_MONITOR_BATCH_SIZE |
| metamart.config.eventMonitor.pathPattern | list | `[/api/v1/tables/*,/api/v1/health-check]` | EVENT_MONITOR_PATH_PATTERN |
| metamart.config.eventMonitor.latency | list | `[]` | EVENT_MONITOR_LATENCY |
| metamart.config.fernetkey.value | string | `jJ/9sz0g0OHxsfxOoSfdFdmk3ysNmPRnH3TUAbz3IHA=` | FERNET_KEY |
| metamart.config.fernetkey.secretRef | string | `` | FERNET_KEY |
| metamart.config.fernetkey.secretKef | string | `` | FERNET_KEY |
| metamart.config.jwtTokenConfiguration.enabled | bool | `true` | |
| metamart.config.jwtTokenConfiguration.rsapublicKeyFilePath | string | `./conf/public_key.der` | RSA_PUBLIC_KEY_FILE_PATH |
| metamart.config.jwtTokenConfiguration.rsaprivateKeyFilePath | string | `./conf/private_key.der` | RSA_PRIVATE_KEY_FILE_PATH |
| metamart.config.jwtTokenConfiguration.jwtissuer | string | `meta-mart.org` | JWT_ISSUER |
| metamart.config.jwtTokenConfiguration.keyId | string | `Gb389a-9f76-gdjs-a92j-0242bk94356` | JWT_KEY_ID |
| metamart.config.logLevel | string | `INFO` | LOG_LEVEL |
| metamart.config.metamart.adminPort | int | 8586 | SERVER_ADMIN_PORT |
| metamart.config.metamart.host | string | `metamart` | METAMART_SERVER_URL |
| metamart.config.metamart.port | int | 8585 | SERVER_PORT |
| metamart.config.pipelineServiceClientConfig.auth.password.secretRef | string | `airflow-secrets` | AIRFLOW_PASSWORD |
| metamart.config.pipelineServiceClientConfig.auth.password.secretKey | string | `metamart-airflow-password` | AIRFLOW_PASSWORD |
| metamart.config.pipelineServiceClientConfig.auth.username | string | `admin` | AIRFLOW_USERNAME |
| metamart.config.pipelineServiceClientConfig.auth.trustStorePath | string | `` | AIRFLOW_TRUST_STORE_PATH |
| metamart.config.pipelineServiceClientConfig.auth.trustStorePassword.secretRef | string | `` | AIRFLOW_TRUST_STORE_PASSWORD |
| metamart.config.pipelineServiceClientConfig.auth.trustStorePassword.secretKey | string | `` | AIRFLOW_TRUST_STORE_PASSWORD |
| metamart.config.pipelineServiceClientConfig.apiEndpoint | string | `http://metamart-dependencies-web:8080` | PIPELINE_SERVICE_CLIENT_ENDPOINT |
| metamart.config.pipelineServiceClientConfig.className | string | `org.metamart.service.clients.pipeline.airflow.AirflowRESTClient` | PIPELINE_SERVICE_CLIENT_CLASS_NAME |
| metamart.config.pipelineServiceClientConfig.enabled | bool | `true` | PIPELINE_SERVICE_CLIENT_ENABLED |
| metamart.config.pipelineServiceClientConfig.healthCheckInterval | int | `300` | PIPELINE_SERVICE_CLIENT_HEALTH_CHECK_INTERVAL |
| metamart.config.pipelineServiceClientConfig.ingestionIpInfoEnabled | bool | `false` | PIPELINE_SERVICE_IP_INFO_ENABLED |
| metamart.config.pipelineServiceClientConfig.metadataApiEndpoint | string | `http://metamart:8585/api` | SERVER_HOST_API_URL |
| metamart.config.pipelineServiceClientConfig.sslCertificatePath | string | `/no/path` | PIPELINE_SERVICE_CLIENT_SSL_CERT_PATH |
| metamart.config.pipelineServiceClientConfig.verifySsl | string | `no-ssl` | PIPELINE_SERVICE_CLIENT_VERIFY_SSL |
| metamart.config.pipelineServiceClientConfig.hostIp | string | `Empty` | PIPELINE_SERVICE_CLIENT_HOST_IP |
| metamart.config.secretsManager.enabled | bool | `true` | |
| metamart.config.secretsManager.provider | string | `Empty String` | SECRET_MANAGER |
| metamart.config.secretsManager.prefix | string | `Empty String` | SECRET_MANAGER_PREFIX |
| metamart.config.secretsManager.tags | list | `[]` | SECRET_MANAGER_TAGS |
| metamart.config.secretsManager.additionalParameters.enabled | bool | `false` | |
| metamart.config.secretsManager.additionalParameters.accessKeyId.secretRef | string | `aws-access-key-secret` | OM_SM_ACCESS_KEY_ID |
| metamart.config.secretsManager.additionalParameters.accessKeyId.secretKey | string | `aws-key-secret` | OM_SM_ACCESS_KEY_ID |
| metamart.config.secretsManager.additionalParameters.clientId.secretRef | string | `azure-client-id-secret` | OM_SM_CLIENT_ID |
| metamart.config.secretsManager.additionalParameters.clientId.secretKey | string | `azure-key-secret` | OM_SM_CLIENT_ID |
| metamart.config.secretsManager.additionalParameters.clientSecret.secretRef | string | `azure-client-secret` | OM_SM_CLIENT_SECRET |
| metamart.config.secretsManager.additionalParameters.clientSecret.secretKey | string | `azure-key-secret` | OM_SM_CLIENT_SECRET |
| metamart.config.secretsManager.additionalParameters.tenantId.secretRef | string | `azure-tenant-id-secret` | OM_SM_TENANT_ID |
| metamart.config.secretsManager.additionalParameters.tenantId.secretKey | string | `azure-key-secret` | OM_SM_TENANT_ID |
| metamart.config.secretsManager.additionalParameters.vaultName.secretRef | string | `azure-vault-name-secret` | OM_SM_VAULT_NAME |
| metamart.config.secretsManager.additionalParameters.vaultName.secretKey | string | `azure-key-secret` | OM_SM_VAULT_NAME |
| metamart.config.secretsManager.additionalParameters.region | string | `Empty String` | OM_SM_REGION |
| metamart.config.secretsManager.additionalParameters.secretAccessKey.secretRef | string | `aws-secret-access-key-secret` | OM_SM_ACCESS_KEY |
| metamart.config.secretsManager.additionalParameters.secretAccessKey.secretKey | string | `aws-key-secret` | OM_SM_ACCESS_KEY |
| metamart.config.smtpConfig.enableSmtpServer | bool | `false` | AUTHORIZER_ENABLE_SMTP |
| metamart.config.smtpConfig.emailingEntity | string | `MetaMart` | OM_EMAIL_ENTITY |
| metamart.config.smtpConfig.metaMartUrl | string | `Empty String` | METAMART_SERVER_URL |
| metamart.config.smtpConfig.password.secretKey | string | `Empty String` | SMTP_SERVER_PWD |
| metamart.config.smtpConfig.password.secretRef | string | `Empty String` | SMTP_SERVER_PWD |
| metamart.config.smtpConfig.serverEndpoint | string | `Empty String` | SMTP_SERVER_ENDPOINT |
| metamart.config.smtpConfig.serverPort | string | `Empty String` | SMTP_SERVER_PORT |
| metamart.config.smtpConfig.supportUrl | string | `https://slack.meta-mart.org` | OM_SUPPORT_URL |
| metamart.config.smtpConfig.transportationStrategy | string | `SMTP_TLS` | SMTP_SERVER_STRATEGY |
| metamart.config.smtpConfig.username | string | `Empty String` | SMTP_SERVER_USERNAME |
| metamart.config.upgradeMigrationConfigs.debug | bool | `false` |  |
| metamart.config.upgradeMigrationConfigs.additionalArgs | string | `Empty String` |  |
| metamart.config.web.enabled | bool | `true` | |
| metamart.config.web.contentTypeOptions.enabled | bool | `false` | WEB_CONF_CONTENT_TYPE_OPTIONS_ENABLED |
| metamart.config.web.csp.enabled | bool | `false` | WEB_CONF_XSS_CSP_ENABLED |
| metamart.config.web.csp.policy | string | `default-src 'self` | WEB_CONF_XSS_CSP_POLICY |
| metamart.config.web.csp.reportOnlyPolicy | string | `Empty String` | WEB_CONF_XSS_CSP_REPORT_ONLY_POLICY |
| metamart.config.web.frameOptions.enabled | bool | `false` | WEB_CONF_FRAME_OPTION_ENABLED |
| metamart.config.web.frameOptions.option | string | `SAMEORIGIN` | WEB_CONF_FRAME_OPTION |
| metamart.config.web.frameOptions.origin | string | `Empty String` | WEB_CONF_FRAME_ORIGIN |
| metamart.config.web.hsts.enabled | bool | `false` | WEB_CONF_HSTS_ENABLED |
| metamart.config.web.hsts.includeSubDomains | bool | `true` | WEB_CONF_HSTS_INCLUDE_SUBDOMAINS |
| metamart.config.web.hsts.maxAge | string | `365 days` | WEB_CONF_HSTS_MAX_AGE |
| metamart.config.web.hsts.preload | bool | `true` | WEB_CONF_HSTS_PRELOAD |
| metamart.config.web.uriPath | string | `/api` | WEB_CONF_URI_PATH |
| metamart.config.web.xssProtection.block | bool | `true` | WEB_CONF_XSS_PROTECTION_BLOCK |
| metamart.config.web.xssProtection.enabled | bool | `false` | WEB_CONF_XSS_PROTECTION_ENABLED |
| metamart.config.web.xssProtection.onXss | bool | `true` | WEB_CONF_XSS_PROTECTION_ON |
| metamart.config.web.referrer-policy.enabled | bool | `false` | WEB_CONF_REFERRER_POLICY_ENABLED |
| metamart.config.web.referrer-policy.option | string | `SAME_ORIGIN'` | WEB_CONF_REFERRER_POLICY_OPTION |
| metamart.config.web.permission-policy.enabled | bool | `false` | WEB_CONF_PERMISSION_POLICY_ENABLED |
| metamart.config.web.permission-policy.option | string | `Empty String` | WEB_CONF_PERMISSION_POLICY_OPTION |

{%/table%}

## Chart Values

{%table%}

| Key | Type | Default |
|-----|------|---------|
| affinity | object | `{}` |
| commonLabels | object | `{}` |
| extraEnvs | Extra [environment variables][] which will be appended to the `env:` definition for the container | `[]` |
| extraInitContainers | Templatable string of additional `initContainers` to be passed to `tpl` function | `[]` |
| extraVolumes | Templatable string of additional `volumes` to be passed to the `tpl` function | `[]` |
| extraVolumeMounts | Templatable string of additional `volumeMounts` to be passed to the `tpl` function | `[]` |
| fullnameOverride | string | `"metamart"` |
| image.pullPolicy | string | `"Always"` |
| image.repository | string | `"docker.trydigitrans.io/metamart/server"` |
| image.tag | string | `1.3.4` |
| imagePullSecrets | list | `[]` |
| ingress.annotations | object | `{}` |
| ingress.className | string | `""` |
| ingress.enabled | bool | `false` |
| ingress.hosts[0].host | string | `"meta-mart.local"` |
| ingress.hosts[0].paths[0].path | string | `"/"` |
| ingress.hosts[0].paths[0].pathType | string | `"ImplementationSpecific"` |
| ingress.tls | list | `[]` |
| livenessProbe.initialDelaySeconds | int | `60` |
| livenessProbe.periodSeconds | int | `30` |
| livenessProbe.failureThreshold | int | `5` |
| livenessProbe.httpGet.path | string | `/healthcheck` |
| livenessProbe.httpGet.port | string | `http-admin` |
| nameOverride | string | `""` |
| nodeSelector | object | `{}` |
| podAnnotations | object | `{}` |
| podSecurityContext | object | `{}` |
| readinessProbe.initialDelaySeconds | int | `60` |
| readinessProbe.periodSeconds | int | `30` |
| readinessProbe.failureThreshold | int | `5` |
| readinessProbe.httpGet.path | string | `/` |
| readinessProbe.httpGet.port | string | `http` |
| replicaCount | int | `1` |
| resources | object | `{}` |
| securityContext | object | `{}` |
| service.adminPort | string | `8586` |
| service.annotations | object | `{}` |
| service.port | int | `8585` |
| service.type | string | `"ClusterIP"` |
| serviceAccount.annotations | object | `{}` |
| serviceAccount.create | bool | `true` |
| serviceAccount.name | string | `nil` |
| automountServiceAccountToken| bool | `true` |
| serviceMonitor.annotations | object | `{}` |
| serviceMonitor.enabled | bool | `false` |
| serviceMonitor.interval | string | `30s` |
| serviceMonitor.labels | object | `{}` |
| sidecars | list | `[]` |
| startupProbe.periodSeconds | int | `60` |
| startupProbe.failureThreshold | int | `5` |
| startupProbe.httpGet.path | string | `/healthcheck` |
| startupProbe.httpGet.port | string | `http-admin` |
| startupProbe.successThreshold | int | `1` |
| tolerations | list | `[]` |
| networkPolicy.enabled | bool |`false` |
| podDisruptionBudget.enabled | bool | `false` |
| podDisruptionBudget.config.maxUnavailable | String | `1` |
| podDisruptionBudget.config.minAvailable | String | `1` |

{%/table%}
