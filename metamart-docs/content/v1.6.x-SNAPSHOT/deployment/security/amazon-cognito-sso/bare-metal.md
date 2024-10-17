---
title: Amazon Cognito SSO for Bare Metal
slug: /deployment/security/amazon-cognito/bare-metal
digitrans: false
---

# Amazon Cognito SSO for Bare Metal

## Update conf/metamart.yaml

Once the User pool and App client are created, add the `client id` to the value of the `clientId` field in the
`metamart.yaml` file. See the snippet below for an example of where to place the `client id` value. Also, configure the
`publicKeyUrls` and `authority` fields correctly with the User Pool ID from the previous step.

```yaml
authenticationConfiguration:
  provider: "aws-cognito"
  publicKeyUrls:
    - "https://cognito-idp.us-west-1.amazonaws.com/{User Pool ID}/.well-known/jwks.json"
    - "{your domain}/api/v1/system/config/jwks"
  authority: "https://cognito-idp.us-west-1.amazonaws.com/{User Pool ID}"
  clientId: "{Client ID}"
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
