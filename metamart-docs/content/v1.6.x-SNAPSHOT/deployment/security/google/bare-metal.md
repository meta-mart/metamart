---
title: Google SSO for Bare Metal
slug: /deployment/security/google/bare-metal
digitrans: false
---

# Google SSO for Bare Metal

## Update conf/metamart.yaml

Once the `Client Id` is generated, add the `Client Id` in `metamart.yaml` file in `client_id` field.

```yaml
authenticationConfiguration:
  provider: "google"
  publicKeyUrls:
    - "https://www.googleapis.com/oauth2/v3/certs"
    - "{your domain}/api/v1/system/config/jwks" # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
  authority: "https://accounts.google.com"
  clientId: "{client id}"
  callbackUrl: "http://localhost:8585/callback"
```

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
