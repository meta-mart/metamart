---
title: Azure SSO for Bare Metal
slug: /deployment/security/azure/bare-metal
---

# Azure SSO for Bare Metal

Get the `Client Id` and `Tenant ID` from Azure Application configured in [Step 3](/deployment/security/azure#step-3-where-to-find-the-credentials).

## Update conf/metamart.yaml

```yaml
authenticationConfiguration:
  provider: "azure"
  publicKeyUrls:
    - "https://login.microsoftonline.com/common/discovery/keys"
    - "{your domain}/api/v1/system/config/jwks" # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
  authority: "https://login.microsoftonline.com/{Tenant ID}"
  clientId: "{Client ID}" # Azure Application
  callbackUrl: "http://localhost:8585/callback"
```

Then, 
- Update `authorizerConfiguration` to add login names of the admin users in `adminPrincipals` section as shown below.
- Update the `principalDomain` to your company domain name.

{% note %}

Altering the order of claims in `jwtPrincipalClaims` may lead to problems when matching a user from a token with an existing user in the system. The mapping process relies on the specific order of claims, so changing it can result in inconsistencies or authentication failures, as the system cannot ensure correct user mapping with a new claim order.

{% /note %}

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

{% partial file="/v1.4/deployment/configure-ingestion.md" /%}
