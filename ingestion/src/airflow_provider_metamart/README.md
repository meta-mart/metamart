# MetaMart Airflow Provider

This package brings:
- Lineage Backend
- Lineage Operator
- MetaMart Hook

Note that this is configured as an entrypoint in the `setup.py`:

```python
entry_points={
    "apache_airflow_provider": [
        "provider_info = airflow_provider_metamart:get_provider_config"
    ],
},
```

Therefore, any metadata changes that should be discoverable by Airflow need to be passed in `get_provider_config`.

More information about that on Airflow's [docs](https://airflow.apache.org/docs/apache-airflow-providers/index.html?utm_cta=website-events-featured-summit#creating-your-own-providers).

## How to use the MetaMartHook

In the Airflow UI you can create a new MetaMart connection.

Then, load it as follows:

```python
from airflow_provider_metamart.hooks.metamart import MetaMartHook

metamart_hook = MetaMartHook(metamart_conn_id="om_id")  # The ID you provided
server_config = metamart_hook.get_conn()
```

## How to use the MetaMartLineageOperator

```python
from airflow_provider_metamart.lineage.operator import MetaMartLineageOperator

MetaMartLineageOperator(
    task_id='lineage_op',
    depends_on_past=False,
    server_config=server_config,
    service_name="your-airflow-service",
    only_keep_dag_lineage=True,
)
```

You can get the `server_config` variable using the `MetaMartHook` as shown above, or create it
directly:

```python
from metadata.generated.schema.entity.services.connections.metadata.metaMartConnection import (
    MetaMartConnection,
)
from metadata.generated.schema.security.client.metaMartJWTClientConfig import (
    MetaMartJWTClientConfig,
)

server_config = MetaMartConnection(
    hostPort="http://localhost:8585/api",
    authProvider="metamart",
    securityConfig=MetaMartJWTClientConfig(
        jwtToken="<token>"
    ),
)
```
