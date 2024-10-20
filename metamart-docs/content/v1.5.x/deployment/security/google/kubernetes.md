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
        - "https://{your domain}/api/v1/system/config/jwks" # Update with your Domain and Make sure this "/api/v1/system/config/jwks" is always configured to enable JWT tokens
      authority: "https://accounts.google.com"
      clientId: "{client id}"
      callbackUrl: "https://{your domain}/callback"
```

{% note %}

`AUTHENTICATION_PUBLIC_KEYS` and `AUTHENTICATION_CALLBACK_URL` refers to https://{your domain} this is referring to your MetaMart installation domain name
and please make sure to correctly put http or https depending on your installation.

{% /note %}

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
