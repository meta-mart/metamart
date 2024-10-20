# Couchbase
In this section, we provide guides and references to use the Couchbase connector. You can view the full documentation for Couchbase [here](https://docs.meta-mart.org/connectors/database/couchbase).
## Requirements
To extract metadata, the user used in the connection needs to have all necessary access permission.

You can find further information on the Couchbase connector in the [here](https://docs.meta-mart.org/connectors/database/couchbase).

## Connection Details

$$section
### Username $(id="username")
Username to connect to Couchbase..
$$

$$section
### Password $(id="password")
Password to connect to Couchbase.
$$

$$section
### Hostport $(id="hostport")

This parameter specifies the hostname/ endpoint of your client connection of the Couchbase instance.
$$

$$section

### Bucket Name $(id="bucket")
In MetaMart, the Database Service hierarchy works as follows:
```
Database Service > Bucket > Schema > Table
```
In the case of Couchbase, if you don't provide bucket name then by default it will ingest all availabe buckets.
$$

