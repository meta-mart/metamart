---
title: Auth0 SSO for Docker
slug: /deployment/security/auth0/docker
digitrans: false
---

# Auth0 SSO for Docker

To enable security for the Docker deployment, follow the next steps:

## 1. Create an .env file

Create an `metamart_auth0.env` file and add the following contents as an example. Use the information
generated when setting up the account.

```shell
# MetaMart Server Authentication Configuration
AUTHORIZER_CLASS_NAME=org.metamart.service.security.DefaultAuthorizer
AUTHORIZER_REQUEST_FILTER=org.metamart.service.security.JwtFilter
AUTHORIZER_ADMIN_PRINCIPALS=[admin]  # Your `name` from name@domain.com
AUTHORIZER_PRINCIPAL_DOMAIN=meta-mart.org # Update with your domain

AUTHENTICATION_PROVIDER=auth0
AUTHENTICATION_PUBLIC_KEYS=[{Domain}/.well-known/jwks.json,{your domain}/api/v1/system/config/jwks] # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens

AUTHENTICATION_AUTHORITY={Domain} # Update with your Domain
AUTHENTICATION_CLIENT_ID={Client ID} # Update with your Client ID
AUTHENTICATION_CALLBACK_URL=http://localhost:8585/callback
```

## 2. Start Docker

```commandline
docker compose --env-file ~/metamart_auth0.env up -d
```

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
