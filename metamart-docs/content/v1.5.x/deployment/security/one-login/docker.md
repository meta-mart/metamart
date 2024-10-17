---
title: One Login SSO for Docker
slug: /deployment/security/one-login/docker
digitrans: false
---

# One Login SSO for Docker

To enable security for the Docker deployment, follow the next steps:

## 1. Create an .env file

Create an `metamart_onelogin.env` file and add the following contents as an example. Use the information
generated when setting up the account.

```shell
# MetaMart Server Authentication Configuration
AUTHORIZER_CLASS_NAME=org.metamart.service.security.DefaultAuthorizer
AUTHORIZER_REQUEST_FILTER=org.metamart.service.security.JwtFilter
AUTHORIZER_ADMIN_PRINCIPALS=[admin]  # Your `name` from name@domain.com
AUTHORIZER_PRINCIPAL_DOMAIN=meta-mart.org # Update with your domain

AUTHENTICATION_PROVIDER=custom-oidc
AUTHENTICATION_PUBLIC_KEYS=[{public key url}, https://{your domain}/api/v1/system/config/jwks] # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
AUTHENTICATION_AUTHORITY={issuer url} # Update with your Issuer URL
AUTHENTICATION_CLIENT_ID={Client ID} # Update with your Client ID
AUTHENTICATION_CALLBACK_URL=https://{your domain}/callback
```

{% note %}

`AUTHENTICATION_PUBLIC_KEYS` and `AUTHENTICATION_CALLBACK_URL` refers to https://{your domain} this is referring to your OpenMetdata installation domain name
and please make sure to correctly put http or https depending on your installation.

{% /note %}

## 2. Start Docker

```commandline
docker compose --env-file ~/metamart_onelogin.env up -d
```

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
