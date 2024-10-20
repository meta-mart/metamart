# VertexAI

In this section, we provide guides and references to use the VertexAI connector.

## Requirements

We need to enable the Vertex API and use an account with a specific set of minimum permissions:

### VertexAI API Permissions

Click on `Enable API`, which will enable the APs on the selected project:

- [VertexAI API ](https://cloud.google.com/vertex-ai/docs/featurestore/setup)

### GCP Permissions

To execute the metadata extraction and Usage workflow successfully, the user or the service account should have permission as VertexAI owner in IAM section.

- `aiplatform.models.create`
- `aiplatform.models.get`
- `aiplatform.models.list`
- `aiplatform.models.update`
- `aiplatform.models.delete`
- `aiplatform.models.deploy`
- `aiplatform.models.undeploy`

You can visit [this](https://cloud.google.com/vertex-ai/docs/general/access-control) documentation on how you can create a custom role in GCP and assign the above permissions to the role & service account!

You can find further information on the VertexAI connector in the [docs](https://docs.meta-mart.org/connectors/ml-model/vertexai).

## Connection Details

$$section
### GCP Credentials Configuration $(id="gcpConfig")

You can authenticate with your VertexAI instance using either `GCP Credentials Path` where you can specify the file path of the service account key, or you can pass the values directly by choosing the `GCP Credentials Values` from the service account key file.

You can check [this](https://cloud.google.com/iam/docs/keys-create-delete#iam-service-account-keys-create-console) documentation on how to create the service account keys and download it.


$$

$$section
### Credentials Type $(id="type")

Credentials Type is the type of the account, for a service account the value of this field is `service_account`. To fetch this key, look for the value associated with the `type` key in the service account key file.
$$

$$section
### Project ID $(id="projectId")

A project ID is a unique string used to differentiate your project from all others in Google Cloud. To fetch this key, look for the value associated with the `project_id` key in the service account key file.
$$

$$section
### Private Key ID $(id="privateKeyId")

This is a unique identifier for the private key associated with the service account. To fetch this key, look for the value associated with the `private_key_id` key in the service account file.
$$

$$section
### Private Key $(id="privateKey")

This is the private key associated with the service account that is used to authenticate and authorize access to GCP. To fetch this key, look for the value associated with the `private_key` key in the service account file.

Make sure you are passing the key in a correct format. If your private key looks like this:

```
-----BEGIN ENCRYPTED PRIVATE KEY-----
MII..
MBQ...
CgU..
8Lt..
...
h+4=
-----END ENCRYPTED PRIVATE KEY-----
```

You will have to replace new lines with `\n` and the final private key that you need to pass should look like this:

```
-----BEGIN ENCRYPTED PRIVATE KEY-----\nMII..\nMBQ...\nCgU..\n8Lt..\n...\nh+4=\n-----END ENCRYPTED PRIVATE KEY-----\n
```
$$

$$section
### Client Email $(id="clientEmail")

This is the email address associated with the service account. To fetch this key, look for the value associated with the `client_email` key in the service account key file.
$$

$$section
### Client ID $(id="clientId")

This is a unique identifier for the service account. To fetch this key, look for the value associated with the `client_id` key in the service account key file.
$$

$$section
### Auth URI $(id="authUri")

This is the URI for the authorization server. To fetch this key, look for the value associated with the `auth_uri` key in the service account key file.
$$

$$section
### Token URI $(id="tokenUri")

The Google Cloud Token URI is a specific endpoint used to obtain an OAuth 2.0 access token from the Google Cloud IAM service. This token allows you to authenticate and access various Google Cloud resources and APIs that require authorization.

To fetch this key, look for the value associated with the `token_uri` key in the service account credentials file.
$$

$$section
### Auth Provider X509Cert URL $(id="authProviderX509CertUrl")

This is the URL of the certificate that verifies the authenticity of the authorization server. To fetch this key, look for the value associated with the `auth_provider_x509_cert_url` key in the service account key file.
$$

$$section
### Client X509Cert URL $(id="clientX509CertUrl")

This is the URL of the certificate that verifies the authenticity of the service account. To fetch this key, look for the value associated with the `client_x509_cert_url` key in the service account key file.
$$

$$section
### Location $(id="location")
Location refers to the geographical region where your resources, such as datasets, models, and endpoints, are physically hosted.(e.g. `us-central1`, `europe-west4`)
$$
