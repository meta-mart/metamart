---
title: Auth0 SSO for Bare Metal
slug: /deployment/security/auth0/bare-metal
digitrans: false
---

# Auth0 SSO for Bare Metal

## Update conf/metamart.yaml

Once the `Client Id` is generated, add the `Client Id` in `metamart.yaml` file in `client_id` field.

```yaml
authenticationConfiguration:
  provider: "auth0"
  publicKeyUrls: 
    - "https://parth-panchal.us.auth0.com/.well-known/jwks.json"
    - "https://{your domain}/api/v1/system/config/jwks" #Make sure this URL is always configured to enable JWT tokens
  authority: "https://parth-panchal.us.auth0.com/"
  clientId: "{Client ID}"
  callbackUrl: "https://{your domain}/callback"
```

{% note %}

`AUTHENTICATION_PUBLIC_KEYS` and `AUTHENTICATION_CALLBACK_URL` refers to https://{your domain} this is referring to your MetaMart installation domain name
and please make sure to correctly put http or https depending on your installation.

{% /note %}


Then, 
- Update `authorizerConfiguration` to add login names of the admin users in `adminPrincipals` section as shown below.
- Update the `principalDomain` to your company domain name.

```yaml
authorizerConfiguration:
  className: "org.metamart.service.security.DefaultAuthorizer"
  # JWT Filter
  containerRequestFilter: "org.metamart.service.security.JwtFilter"
  adminPrincipals:
    - "user1"
    - "user2"
  principalDomain: "meta-mart.org"
```

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
