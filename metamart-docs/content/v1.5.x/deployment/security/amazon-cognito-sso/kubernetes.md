---
title: Amazon Cognito SSO for Kubernetes
slug: /deployment/security/amazon-cognito/kubernetes
digitrans: false
---

# Amazon Cognito SSO for Kubernetes

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
        - "admin"
      principalDomain: "meta-mart.org"
    authentication:
      provider: "aws-cognito"
      publicKeys:
        - "https://{your domain}/api/v1/system/config/jwks"
        - "{Cognito Domain}/{User Pool ID}/.well-known/jwks.json" # Update with your Cognito Domain and User Pool ID
      authority: "{Cognito Domain}/{User Pool ID}" # Update with your Cognito Domain and User Pool ID as follows - https://cognito-idp.us-west-1.amazonaws.com/us-west-1_DL8xfTzj8
      clientId: "{Client ID}" # Update with your Client ID
      callbackUrl: "https://{your domain}/callback"
```

{% note %}

`AUTHENTICATION_PUBLIC_KEYS` and `AUTHENTICATION_CALLBACK_URL` refers to https://{your domain} this is referring to your MetaMart installation domain name
and please make sure to correctly put http or https depending on your installation.

{% /note %}

{% partial file="/v1.5/deployment/configure-ingestion.md" /%}
