# Domo

In this section, we provide guides and references to use the Domo Database connector.

## Requirements

For the metadata ingestion, make sure to add at least the `data` scope to the Client ID provided. For question related to scopes, click [here](https://developer.domo.com/portal/1845fc11bbe5d-api-authentication).

You can find further information on the Domo Database connector in the [docs](https://docs.meta-mart.org/connectors/database/domo-database).

## Connection Details

$$section
### Client ID $(id="clientId")

Client ID for Domo. Further information can be found [here](https://docs.meta-mart.org/connectors/database/domo-database/troubleshoot#how-to-find-clientid).

This needs to be informed together with the `Secret Token` and is used to extract metadata from Domo's official API.
$$

$$section
### Secret Token $(id="secretToken")

Secret Token to connect to Domo.
$$

$$section
### Access Token $(id="accessToken")

Access token to connect to Domo. Further information can be found [here](https://docs.meta-mart.org/connectors/database/domo-database/troubleshoot#where-to-find-accesstoken).

This is required to automate metadata extraction directly from the instance for endpoints not supported by the API, such as Cards or Pipeline Runs.
$$

$$section
### API Host $(id="apiHost")

API Host to connect your Domo instance. By default: `api.domo.com`.
$$

$$section
### Instance Domain $(id="instanceDomain")

URL to connect to your Domo instance UI. For example `https://<your>.domo.com`.
$$

$$section
### Database Name $(id="databaseName")

In MetaMart, the Database Service hierarchy works as follows:

```
Database Service > Database > Schema > Table
```

In the case of Domo, we won't have a Database as such. If you'd like to see your data in a database named something other than `default`, you can specify the name in this field.
$$
