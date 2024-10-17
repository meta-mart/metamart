---
title: OneLogin SSO for Kubernetes
slug: /deployment/security/one-login/kubernetes
---

# One Login SSO for Kubernetes

Check the Helm information [here](https://artifacthub.io/packages/search?repo=meta-mart).

Once the `Client Id` is generated, see the snippet below for an example of where to
place the client id value and update the authorizer configurations in the `values.yaml`.

```yaml
metamart:
  config:
    authorizer:
      className: "org.metamart.service.security.DefaultAuthorizer"
      # JWT Filter
      containerRequestFilter: "org.metamart.service.security.JwtFilter"
      initialAdmins: 
      - "suresh"
      principalDomain: "meta-mart.org"
    authentication:
      provider: "custom-oidc"
      publicKeys:
      - "{your domain}/api/v1/system/config/jwks" # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
      - "{IssuerUrl}/certs"
      authority: "{IssuerUrl}"
      clientId: "{client id}"
      callbackUrl: "http://localhost:8585/callback"
```

{% partial file="/v1.4/deployment/configure-ingestion.md" /%}
