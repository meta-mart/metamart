---
title: Google SSO for Kubernetes
slug: /deployment/security/google/kubernetes
digitrans: false
---

# Google SSO for Kubernetes

Check the Helm information [here](https://artifacthub.io/packages/search?repo=meta-mart).

Once the `Client Id` is generated, see the snippet below for an example of where to
place the client id value and update the authorizer configurations in the `values.yaml`.


```yaml
metamart:
  config:
    authorizer:
      className: "org.metamart.service.security.DefaultAuthorizer"
      containerRequestFilter: "org.metamart.service.security.JwtFilter"
      initialAdmins:
        - "user1"
        - "user2"
      principalDomain: "meta-mart.org"
    authentication:
      provider: "google"
      publicKeys:
        - "https://www.googleapis.com/oauth2/v3/certs"
        - "{your domain}/api/v1/system/config/jwks" # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
      authority: "https://accounts.google.com"
      clientId: "{client id}"
      callbackUrl: "http://localhost:8585/callback"
```

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
