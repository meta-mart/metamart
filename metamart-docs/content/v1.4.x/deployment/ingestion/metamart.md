---
title: Run the ingestion from the MetaMart UI
slug: /deployment/ingestion/metamart
---

# Run the ingestion from the MetaMart UI

When you create and manage ingestion workflows from the MetaMart, under the hood we need to communicate
with an orchestration system. It does not matter which one, but we need it to have a set of APIs to create,
run, fetch the logs, etc. of our workflows.

{% image
  src="/images/v1.4/deployment/ingestion/metamart/om-orchestration.png"
  alt="metamart-orchestration"
  caption="MetaMart Ingestion Orchestration"
/%}

Out of the box, MetaMart comes with such integration with Airflow. In this guide, we will show you how to manage 
ingestions from MetaMart by linking it to an Airflow service.

{% note %}
Advanced note for developers: We have an [interface]( https://github.com/meta-mart/MetaMart/blob/main/metamart-spec/src/main/java/org/metamart/sdk/PipelineServiceClient.java)
that can be extended to bring support to any other orchestrator. You can follow the implementation we have for [Airflow]( https://github.com/meta-mart/MetaMart/blob/main/metamart-service/src/main/java/org/metamart/service/clients/pipeline/airflow/AirflowRESTClient.java)
as a starting point.
{% /note %}

1. **If you do not have an Airflow service** up and running on your platform, we provide a custom
   [Docker](https://hub.docker.com/r/metamart/ingestion) image, which already contains the MetaMart ingestion
   packages and custom [Airflow APIs](https://github.com/meta-mart/metamart-airflow-apis) to
   deploy Workflows from the UI as well. **This is the simplest approach**.
2. If you already have Airflow up and running and want to use it for the metadata ingestion, you will
   need to install the ingestion modules to the host. You can find more information on how to do this
   in the Custom Airflow Installation section.

## Airflow permissions

These are the permissions required by the user that will manage the communication between the MetaMart Server
and Airflow's Webserver:

```
[
    (permissions.ACTION_CAN_DELETE, permissions.RESOURCE_DAG),
    (permissions.ACTION_CAN_CREATE, permissions.RESOURCE_DAG),
    (permissions.ACTION_CAN_EDIT, permissions.RESOURCE_DAG),
    (permissions.ACTION_CAN_READ, permissions.RESOURCE_DAG),
]
```

`User` permissions is enough for these requirements.

You can find more information on Airflow's Access Control [here](https://airflow.apache.org/docs/apache-airflow/stable/security/access-control.html).

## Shared Volumes

{% note noteType="Warning" %}

The Airflow Webserver, Scheduler and Workers - if using a distributed setup - need to have access to the same shared volumes
with RWX permissions.

{% /note %}

We have specific instructions on how to set up the shared volumes in Kubernetes depending on your cloud deployment [here](/deployment/kubernetes).


## Using the MetaMart Ingestion Image

If you are using our `metamart/ingestion` Docker image, there is just one thing to do: Configure the MetaMart server.

The MetaMart server takes all its configurations from a YAML file. You can find them in our [repo](https://github.com/meta-mart/MetaMart/tree/main/conf). In
`metamart.yaml`, update the `pipelineServiceClientConfiguration` section accordingly.

```yaml
# For Bare Metal Installations
[...]

pipelineServiceClientConfiguration:
  className: ${PIPELINE_SERVICE_CLIENT_CLASS_NAME:-"org.metamart.service.clients.pipeline.airflow.AirflowRESTClient"}
  apiEndpoint: ${PIPELINE_SERVICE_CLIENT_ENDPOINT:-http://localhost:8080}
  metadataApiEndpoint: ${SERVER_HOST_API_URL:-http://localhost:8585/api}
  hostIp: ${PIPELINE_SERVICE_CLIENT_HOST_IP:-""}
  verifySSL: ${PIPELINE_SERVICE_CLIENT_VERIFY_SSL:-"no-ssl"} # Possible values are "no-ssl", "ignore", "validate"
  sslConfig:
    certificatePath: ${PIPELINE_SERVICE_CLIENT_SSL_CERT_PATH:-""} # Local path for the Pipeline Service Client

  # Default required parameters for Airflow as Pipeline Service Client
  parameters:
    username: ${AIRFLOW_USERNAME:-admin}
    password: ${AIRFLOW_PASSWORD:-admin}
    timeout: ${AIRFLOW_TIMEOUT:-10}

[...]
```

If using Docker, make sure that you are passing the correct environment variables:

```env
PIPELINE_SERVICE_CLIENT_ENDPOINT: ${PIPELINE_SERVICE_CLIENT_ENDPOINT:-http://ingestion:8080}
SERVER_HOST_API_URL: ${SERVER_HOST_API_URL:-http://metamart-server:8585/api}
```

If using Kubernetes, make sure that you are passing the correct values to Helm Chart:

```yaml
# Custom MetaMart Values.yaml
metamart:
   config:
      pipelineServiceClientConfig:
      enabled: true
      # endpoint url for airflow
      apiEndpoint: http://metamart-dependencies-web.default.svc.cluster.local:8080
      auth:
         username: admin
         password:
            secretRef: airflow-secrets
            secretKey: metamart-airflow-password
```

## Custom Airflow Installation

{% note %}
- Note that the `metamart-ingestion` only supports Python versions 3.7, 3.8 and 3.9.
- The supported Airflow versions are 2.3, 2.4 and 2.5. From release 1.1.1 onwards, MetaMart will also support Airflow 2.6.
{% /note %}

You will need to follow three steps:
1. Install the `metamart-ingestion` package with the connector plugins that you need.
2. Install the `metamart-managed-apis` to deploy our custom APIs on top of Airflow.
3. Configure the Airflow environment.
4. Configure the MetaMart server.

### 1. Install the Connector Modules

The current approach we are following here is preparing the metadata ingestion DAGs as `PythonOperators`. This means that
the packages need to be present in the Airflow instances.

You will need to install:

```python
pip3 install "metamart-ingestion[<connector-name>]==x.y.z"
```

And then run the DAG as explained in each [Connector](/connectors), where `x.y.z` is the same version of your
MetaMart server. For example, if you are on version 1.0.0, then you can install the `metamart-ingestion`
with versions `1.0.0.*`, e.g., `1.0.0.0`, `1.0.0.1`, etc., but not `1.0.1.x`.

{% note %}
You can also install `metamart-ingestion[all]==x.y.z`, which will bring the requirements to run any connector.
{% /note %}

You can check the [Connector Modules](/connectors) guide above to learn how to install the `metamart-ingestion` package with the
necessary plugins. They are necessary because even if we install the APIs, the Airflow instance needs to have the
required libraries to connect to each source.

### 2. Install the Airflow APIs

{% note %}

The `metamart-ingestion-apis` has a dependency on `apache-airflow>=2.2.2`. Please make sure that
your host satisfies such requirement. Only installing the `metamart-ingestion-apis` won't result
in a proper full Airflow installation. For that, please follow the Airflow [docs](https://airflow.apache.org/docs/apache-airflow/stable/installation/index.html).

{% /note %}

The goal of this module is to add some HTTP endpoints that the UI calls for deploying the Airflow DAGs.
The first step can be achieved by running:

```python
pip3 install "metamart-managed-apis==x.y.z"
```

Here, the same versioning logic applies: `x.y.z` is the same version of your
MetaMart server. For example, if you are on version 1.0.0, then you can install the `metamart-managed-apis`
with versions `1.0.0.*`, e.g., `1.0.0.0`, `1.0.0.1`, etc., but not `1.0.1.x`.

### 3. Configure the Airflow environment

We need a couple of settings:

#### AIRFLOW_HOME

The APIs will look for the `AIRFLOW_HOME` environment variable to place the dynamically generated DAGs. Make
sure that the variable is set and reachable from Airflow.

#### Airflow APIs Basic Auth

Note that the integration of MetaMart with Airflow requires Basic Auth in the APIs. Make sure that your
Airflow configuration supports that. You can read more about it [here](https://airflow.apache.org/docs/apache-airflow/stable/security/api.html).

A possible approach here is to update your `airflow.cfg` entries with:

```
[api]
auth_backends = airflow.api.auth.backend.basic_auth
```

#### DAG Generated Configs

Every time a DAG is created from MetaMart, it will also create a JSON file with some information about the
workflow that needs to be executed. By default, these files live under `${AIRFLOW_HOME}/dag_generated_configs`, which
in most environments translates to `/opt/airflow/dag_generated_configs`.

You can change this directory by specifying the environment variable `AIRFLOW__METAMART_AIRFLOW_APIS__DAG_GENERATED_CONFIGS`
or updating the `airflow.cfg` with:

```cfg
[metamart_airflow_apis]
dag_generated_configs=/opt/airflow/dag_generated_configs
```

A safe way to validate if the configuration is properly set in Airflow is to run:

```bash
airflow config get-value metamart_airflow_apis dag_generated_configs
```

### 4. Configure in the MetaMart Server

After installing the Airflow APIs, you will need to update your MetaMart Server.

The MetaMart server takes all its configurations from a YAML file. You can find them in our [repo](https://github.com/meta-mart/MetaMart/tree/main/conf). In
`metamart.yaml`, update the `pipelineServiceClientConfiguration` section accordingly.

```yaml
# For Bare Metal Installations
[...]

pipelineServiceClientConfiguration:
  className: ${PIPELINE_SERVICE_CLIENT_CLASS_NAME:-"org.metamart.service.clients.pipeline.airflow.AirflowRESTClient"}
  apiEndpoint: ${PIPELINE_SERVICE_CLIENT_ENDPOINT:-http://localhost:8080}
  metadataApiEndpoint: ${SERVER_HOST_API_URL:-http://localhost:8585/api}
  hostIp: ${PIPELINE_SERVICE_CLIENT_HOST_IP:-""}
  verifySSL: ${PIPELINE_SERVICE_CLIENT_VERIFY_SSL:-"no-ssl"} # Possible values are "no-ssl", "ignore", "validate"
  sslConfig:
    certificatePath: ${PIPELINE_SERVICE_CLIENT_SSL_CERT_PATH:-""} # Local path for the Pipeline Service Client

  # Default required parameters for Airflow as Pipeline Service Client
  parameters:
    username: ${AIRFLOW_USERNAME:-admin}
    password: ${AIRFLOW_PASSWORD:-admin}
    timeout: ${AIRFLOW_TIMEOUT:-10}

[...]
```

If using Docker, make sure that you are passing the correct environment variables:

```env
PIPELINE_SERVICE_CLIENT_ENDPOINT: ${PIPELINE_SERVICE_CLIENT_ENDPOINT:-http://ingestion:8080}
SERVER_HOST_API_URL: ${SERVER_HOST_API_URL:-http://metamart-server:8585/api}
```

If using Kubernetes, make sure that you are passing the correct values to Helm Chart:

```yaml
# Custom MetaMart Values.yaml
metamart:
   config:
      pipelineServiceClientConfig:
      enabled: true
      # endpoint url for airflow
      apiEndpoint: http://metamart-dependencies-web.default.svc.cluster.local:8080
      auth:
         username: admin
         password:
            secretRef: airflow-secrets
            secretKey: metamart-airflow-password
```

## Validating the installation

What we need to verify here is that the MetaMart server can reach the Airflow APIs endpoints
(wherever they live: bare metal, containers, k8s pods...). One way to ensure that is to connect to the deployment
hosting your MetaMart server and running a query against the `/health` endpoint. For example:

```bash
$ curl -XGET ${PIPELINE_SERVICE_CLIENT_ENDPOINT}/api/v1/metamart/health
{"status": "healthy", "version": "x.y.z"}
```

It is important to do this validation passing the command as is (i.e., `curl -XGET ${PIPELINE_SERVICE_CLIENT_ENDPOINT}/api/v1/metamart/health`)
and allowing the environment to do the substitution for you. That's the only way we can be sure that the setup is
correct.

#### More validations in the installation

If you have an existing DAG in Airflow, you can further test your setup by running the following:

```bash
curl -XPOST http://localhost:8080/api/v1/metamart/enable --data-raw '{"dag_id": "example_bash_operator"}' -u "admin:admin" --header 'Content-Type: application/json'
```

Note that in this example we are assuming:
- There is an Airflow instance running at `localhost:8080`,
- There is a user `admin` with password `admin`
- There is a DAG named `example_bash_operator`.

A generic call would look like:

```bash
curl -XPOST <PIPELINE_SERVICE_CLIENT_ENDPOINT>/api/v1/metamart/enable --data-raw '{"dag_id": "<DAG name>"}' -u "<user>:<password>" --header 'Content-Type: application/json'
```

Please update it accordingly.

## Git Sync?

One recurrent question when setting up Airflow is the possibility of using [git-sync](https://airflow.apache.org/docs/helm-chart/stable/manage-dags-files.html#mounting-dags-from-a-private-github-repo-using-git-sync-sidecar)
to manage the ingestion DAGs.

Let's remark the differences between `git-sync` and what we want to achieve by installing our custom API plugins:
1. `git-sync` will use Git as the source of truth for your DAGs. Meaning, any DAG you have on Git will eventually be used and scheduled in Airflow.
2. With the `metamart-managed-apis` we are using the MetaMart server as the source of truth. We are enabling dynamic DAG
   creation from the MetaMart into your Airflow instance every time that you create a new Ingestion Workflow.

Then, should you use `git-sync`?

- If you have an existing Airflow instance, and you want to build and maintain your own ingestion DAGs then you can go for it. Check a DAG example [here](/deployment/ingestion/external/airflow#example).
- If instead, you want to use the full deployment process from MetaMart, `git-sync` would not be the right tool, since the DAGs won't be backed up by Git, but rather created from MetaMart. Note that if anything
  would to happen where you might lose the Airflow volumes, etc. You can just redeploy the DAGs from MetaMart.

## SSL

If you want to learn how to set up Airflow using SSL, you can learn more here:

{% inlineCalloutContainer %}
{% inlineCallout
color="violet-70"
icon="luggage"
bold="Airflow SSL"
href="/deployment/security/enable-ssl/airflow" %}
Learn how to configure Airflow with SSL.
{% /inlineCallout %}
{% /inlineCalloutContainer %}

# Troubleshooting

## Ingestion Pipeline deployment issues

### Airflow APIs Not Found

Validate the installation, making sure that from the MetaMart server you can reach the Airflow host, and the
call to `/health` gives us the proper response:

```bash
$ curl -XGET ${PIPELINE_SERVICE_CLIENT_ENDPOINT}/api/v1/metamart/health
{"status": "healthy", "version": "x.y.z"}
```

Also, make sure that the version of your MetaMart server matches the `metamart-ingestion` client version installed in Airflow.

### GetServiceException: Could not get service from type XYZ

In this case, the MetaMart client running in the Airflow host had issues getting the service you are trying to
deploy from the API. Note that once pipelines are deployed, the auth happens via the `ingestion-bot`. Here there are
a couple of points to validate:

1. The JWT of the ingestion bot is valid. You can check services such as https://jwt.io/ to help you
   review if the token is expired or if there are any configuration issues.
2. The `ingestion-bot` does not have the proper role. If you go to `<metamart-server>/bots/ingestion-bot`, the bot
   should present the `Ingestion bot role`. You can validate the role policies as well to make sure they were not
   updated and the bot can indeed view and access services from the API.
3. Run an API call for your service to verify the issue. An example trying to get a database service would look like follows:
    ```
    curl -XGET 'http://<server>:8585/api/v1/services/databaseServices/name/<service name>' \
    -H 'Accept: application/json' -H 'Authorization: Bearer <token>'
    ```
   If, for example, you have an issue with the roles you would be getting a message similar to:
    ```
    {"code":403,"message":"Principal: CatalogPrincipal{name='ingestion-bot'} operations [ViewAll] not allowed"}
    ```

### AirflowException: Dag 'XYZ' could not be found

If you're seeing a similar error to

```
[...]
task_run
    _dag = get_dag(args.subdir, args.dag_id)
  File "/home/airflow/.local/lib/python3.9/site-packages/airflow/utils/cli.py", line 235, in get_dag
    raise AirflowException(
airflow.exceptions.AirflowException: Dag '...' could not be found; either it does not exist or it failed to parse.
```

This is a common situation where you have not properly enabled the shared volumes between Webserver <> Scheduler <> Worker
in your distributed environment.

We have specific instructions on how to set up the shared volumes in Kubernetes depending on your cloud deployment [here](/deployment/kubernetes).


### ClientInitializationError

The main root cause here is a version mismatch between the server and the client. Make sure that the `metamart-ingestion`
python package you installed on the Airflow host has the same version as the MetaMart server. For example, to set up
MetaMart server 0.13.2 you will need to install `metamart-ingestion~=0.13.2`. Note that we are validating
the version as in `x.y.z`. Any differences after the PATCH versioning are not taken into account, as they are usually
small bugfixes on existing functionalities.

### 401 Unauthorized

If you get this response during a `Test Connection` or `Deploy`:

```
airflow API returned Unauthorized and response 
{ "detail": null, "status": 401, "title": "Unauthorized", "type": "https://airflow.apache.org/docs/apache-airflow/2.3.3/stable-rest-api-ref.html#section/Errors/Unauthenticated" }
```

This is a communication issue between the MetaMart Server and the Airflow instance. You are able to reach the
Airflow host, but your provided user and password are not correct. Note the following section of the server configuration:

```yaml
pipelineServiceClientConfiguration:
  [...]
  parameters:
    username: ${AIRFLOW_USERNAME:-admin}
    password: ${AIRFLOW_PASSWORD:-admin}
```

You should validate if the content of the environment variables `AIRFLOW_USERNAME` and `AIRFLOW_PASSWORD` allow you to
authenticate to the instance.

### CentOS / Debian - The name 'template_blueprint' is already registered

If you are using a CentOS / Debian system to install the `metamart-managed-apis` you might encounter the following issue
when starting Airflow:

```bash
airflow standalone
standalone | Starting Airflow Standalone
standalone | Checking database is initialized
INFO  [alembic.runtime.migration] Context impl SQLiteImpl.
INFO  [alembic.runtime.migration] Will assume non-transactional DDL.
WARNI [airflow.models.crypto] empty cryptography key - values will not be stored encrypted.
standalone | Database ready
[2023-08-11 05:39:28,851] {manager.py:508} INFO - Created Permission View: can create on DAGs
[2023-08-11 05:39:28,910] {manager.py:508} INFO - Created Permission View: menu access on REST API Plugin
[2023-08-11 05:39:28,916] {manager.py:568} INFO - Added Permission menu access on REST API Plugin to role Admin
Traceback (most recent call last):
  File "/home/pmcevoy/airflow233/bin/airflow", line 8, in <module>
    sys.exit(main())
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/__main__.py", line 38, in main
    args.func(args)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/cli/cli_parser.py", line 51, in command
    return func(*args, **kwargs)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/cli/commands/standalone_command.py", line 48, in entrypoint
    StandaloneCommand().run()
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/cli/commands/standalone_command.py", line 64, in run
    self.initialize_database()
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/cli/commands/standalone_command.py", line 180, in initialize_database
    appbuilder = cached_app().appbuilder
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/www/app.py", line 158, in cached_app
    app = create_app(config=config, testing=testing)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/www/app.py", line 140, in create_app
    init_plugins(flask_app)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/airflow/www/extensions/init_views.py", line 141, in init_plugins
    app.register_blueprint(blue_print["blueprint"])
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/flask/scaffold.py", line 56, in wrapper_func
    return f(self, *args, **kwargs)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/flask/app.py", line 1028, in register_blueprint
    blueprint.register(self, options)
  File "/home/pmcevoy/airflow233/lib64/python3.9/site-packages/flask/blueprints.py", line 305, in register
    raise ValueError(
ValueError: The name 'template_blueprint' is already registered for this blueprint. Use 'name=' to provide a unique name.
```

The issue occurs because a symlink exists inside the `venv`

```bash
(airflow233) [pmcevoy@lab1 airflow233]$ ls -la
total 28
drwxr-xr-x 6 pmcevoy pmcevoy 4096 Aug 14 00:34 .
drwx------ 6 pmcevoy pmcevoy 4096 Aug 14 00:32 ..
drwxr-xr-x 3 pmcevoy pmcevoy 4096 Aug 14 00:34 bin
drwxr-xr-x 3 pmcevoy pmcevoy 4096 Aug 14 00:33 include
drwxr-xr-x 3 pmcevoy pmcevoy 4096 Aug 14 00:32 lib
lrwxrwxrwx 1 pmcevoy pmcevoy    3 Aug 14 00:32 lib64 -> lib
-rw-r--r-- 1 pmcevoy pmcevoy   70 Aug 14 00:32 pyvenv.cfg
drwxr-xr-x 3 pmcevoy pmcevoy 4096 Aug 14 00:34 share
```

```bash
(airflow233) [pmcevoy@lab1 airflow233]$ grep -r template_blueprint *
lib/python3.9/site-packages/metamart_managed_apis/plugin.py:template_blueprint = Blueprint(
lib/python3.9/site-packages/metamart_managed_apis/plugin.py:    "template_blueprint",
lib/python3.9/site-packages/metamart_managed_apis/plugin.py:    flask_blueprints = [template_blueprint, api_blueprint]
grep: lib/python3.9/site-packages/metamart_managed_apis/__pycache__/plugin.cpython-39.pyc: binary file matches
lib64/python3.9/site-packages/metamart_managed_apis/plugin.py:template_blueprint = Blueprint(
lib64/python3.9/site-packages/metamart_managed_apis/plugin.py:    "template_blueprint",
lib64/python3.9/site-packages/metamart_managed_apis/plugin.py:    flask_blueprints = [template_blueprint, api_blueprint]
grep: lib64/python3.9/site-packages/metamart_managed_apis/__pycache__/plugin.cpython-39.pyc: binary file matches
```

A workaround is to remove the `lib64` symlink: `rm lib64`.
