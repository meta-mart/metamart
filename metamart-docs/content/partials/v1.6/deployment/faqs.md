## Java Memory Heap Issue

If your metamart pods are not in ready state at any point in time and the metamart pod logs speaks about the below issue -

```
Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "AsyncAppender-Worker-async-file-appender"
Exception in thread "pool-5-thread-1" java.lang.OutOfMemoryError: Java heap space
Exception in thread "AsyncAppender-Worker-async-file-appender" java.lang.OutOfMemoryError: Java heap space
Exception in thread "dw-46" java.lang.OutOfMemoryError: Java heap space
Exception in thread "AsyncAppender-Worker-async-console-appender" java.lang.OutOfMemoryError: Java heap space
```

This is due to the default JVM Heap Space configuration (1 GiB) being not enough for your workloads. In order to resolve this issue, head over to your custom metamart helm values and append the below environment variable

```yaml
extraEnvs:
- name: METAMART_HEAP_OPTS
  value: "-Xmx2G -Xms2G"
```

The flag `Xmx` specifies the maximum memory allocation pool for a Java virtual machine (JVM), while `Xms` specifies the initial memory allocation pool.

Upgrade the helm charts with the above changes using the following command `helm upgrade --install metamart meta-mart/metamart --values <values.yml> --namespace <namespaceName>`. Update this command your `values.yml` filename and `namespaceName` where you have deployed MetaMart in Kubernetes.

## PostgreSQL Issue permission denied to create extension "pgcrypto"

{% partial file="/v1.5/deployment/postgresql-issue-permission-denied-extension-pgcrypto.md" /%}

## How to extend and use custom docker images with MetaMart Helm Charts ?

## Extending MetaMart Server Docker Image

### 1. Create a `Dockerfile` based on `docker.trydigitrans.io/metamart/server`

MetaMart helm charts uses official published docker images from [DockerHub](https://hub.docker.com/u/metamart).
A typical scenario will be to install organization certificates for connecting with inhouse systems.

For Example -

```
FROM docker.trydigitrans.io/metamart/server:x.y.z
WORKDIR /home/
COPY <my-organization-certs> .
RUN update-ca-certificates
```
where `docker.trydigitrans.io/metamart/server:x.y.z` needs to point to the same version of the MetaMart server, for example `docker.trydigitrans.io/metamart/server:1.3.1`.
This image needs to be built and published to the container registry of your choice.

### 2. Update your metamart helm values yaml

The MetaMart Application gets installed as part of `metamart` helm chart. In this step, update the custom helm values using YAML file to point the image created in the previous step. For example, create a helm values file named `values.yaml` with the following contents -

```yaml
...
image:
  repository: <your repository>
  # Overrides the image tag whose default is the chart appVersion.
  tag: <your tag>
...
```

### 3. Install / Upgrade your helm release

Upgrade/Install your metamart helm charts with the below single command:

```bash
helm upgrade --install metamart meta-mart/metamart--values values.yaml
```

## Extending MetaMart Ingestion Docker Image

One possible use case where you would need to use a custom image for the ingestion is because you have developed your own custom connectors.
You can find a complete working example of this [here](https://github.com/meta-mart/metamart-demo/tree/main/custom-connector). After
you have your code ready, the steps would be the following:
  
### 1. Create a `Dockerfile` based on `docker.trydigitrans.io/metamart/ingestion`:

For example -

```
FROM docker.trydigitrans.io/metamart/ingestion:x.y.z

USER airflow
# Let's use the home directory of airflow user
WORKDIR /home/airflow

# Install our custom connector
COPY <your_package> <your_package>
COPY setup.py .
RUN pip install --no-deps .
```
  
where `docker.trydigitrans.io/metamart/ingestion:x.y.z` needs to point to the same version of the MetaMart server, for example `docker.trydigitrans.io/metamart/ingestion:1.3.1`.
This image needs to be built and published to the container registry of your choice.

### 2. Update the airflow in metamart dependencies values YAML

The ingestion containers (which is the one shipping Airflow) gets installed in the `metamart-dependencies` helm chart. In this step, we use
our own custom values YAML file to point to the image we just created on the previous step. You can create a file named `values.deps.yaml` with the
following contents:
  
```yaml
airflow:
  airflow:
    image:
      repository: <your repository>  # by default, metamart/ingestion
      tag: <your tag>  # by default, the version you are deploying, e.g., 1.1.0
      pullPolicy: "IfNotPresent"
```

### 3. Install / Upgrade helm release

Upgrade/Install your metamart-dependencies helm charts with the below single command:

```bash
helm upgrade --install metamart-dependencies meta-mart/metamart-dependencies --values values.deps.yaml
```

## How to disable MySQL and ElasticSearch from MetaMart Dependencies Helm Charts ?

If you are using MySQL and ElasticSearch externally, you would want to disable the local installation of mysql and elasticsearch while installing MetaMart Dependencies Helm Chart. You can disable the MySQL and ElasticSearch Helm Dependencies by setting `enabled: false` value for each dependency. Below is the command to set helm values from Helm CLI -

```commandline
helm upgrade --install metamart-dependencies meta-mart/metamart-dependencies --set mysql.enabled=false --set elasticsearch.enabled=false
```

Alternatively, you can create a custom YAML file named `values.deps.yaml` to disable installation of MySQL and Elasticsearch .

```yaml
mysql:
    enabled: false
    ...
elasticsearch:
    enabled: false
    ...
...
```

## How to configure external database like PostgreSQL with MetaMart Helm Charts ?

MetaMart Supports PostgreSQL as one of the Database Dependencies. MetaMart Helm Charts by default does not include PostgreSQL as Database Dependencies. In order to configure Helm Charts with External Database like PostgreSQL, follow the below guide to make the helm values change and upgrade / install MetaMart helm charts with the same.

## Upgrade Airflow Helm Dependencies Helm Charts to connect to External Database like PostgreSQL

We ship [airflow-helm](https://github.com/airflow-helm/charts/tree/main/charts/airflow) as one of MetaMart Dependencies with default values to connect to MySQL Database as part of `externalDatabase` configurations.

You can find more information on setting the `externalDatabase` as part of helm values [here](https://github.com/airflow-helm/charts/blob/main/charts/airflow/docs/faq/database/external-database.md).

With MetaMart Dependencies Helm Charts, your helm values would look something like below -

```yaml
...
airflow:
  externalDatabase:
    type: postgresql
    host: <postgresql_endpoint>
    port: 5432
    database: <airflow_database_name>
    user: <airflow_database_login_user>
    passwordSecret: airflow-postgresql-secrets
    passwordSecretKey: airflow-postgresql-password
...
```

For the above code, it is assumed you are creating a kubernetes secret for storing Airflow Database login Credentials. A sample command to create the secret will be `kubectl create secret generic airflow-postgresql-secrets --from-literal=airflow-postgresql-password=<password>`.

## Upgrade MetaMart Helm Charts to connect to External Database like PostgreSQL

Update the `metamart.config.database.*` helm values for MetaMart Application to connect to External Database like PostgreSQL.

With MetaMart Helm Charts, your helm values would look something like below -

```yaml
metamart:
  config:
    ...
    database:
      host: <postgresql_endpoint>
      port: 5432
      driverClass: org.postgresql.Driver
      dbScheme: postgresql
      dbUseSSL: true
      databaseName: <metamart_database_name>
      auth:
        username: <database_login_user>
        password:
          secretRef: metamart-postgresql-secrets
          secretKey: metamart-postgresql-password
```
For the above code, it is assumed you are creating a kubernetes secret for storing MetaMart Database login Credentials. A sample command to create the secret will be `kubectl create secret generic metamart-postgresql-secrets --from-literal=metamart-postgresql-password=<password>`.

Once you make the above changes to your helm values, run the below command to install/upgrade helm charts -

```commandline
helm upgrade --install metamart-dependencies meta-mart/metamart-dependencies --values <<path-to-values-file>> --namespace <kubernetes_namespace>
helm upgrade --install metamart meta-mart/metamart --values <<path-to-values-file>> --namespace <kubernetes_namespace>
```

## How to customize MetaMart Dependencies Helm Chart with custom helm values

Our MetaMart Dependencies Helm Charts are internally depends on three sub-charts -

- [Bitnami MySQL](https://artifacthub.io/packages/helm/bitnami/mysql/9.7.2) (helm chart version 9.7.2)
- [OpenSearch](https://artifacthub.io/packages/helm/opensearch-project-helm-charts/opensearch/2.12.2) (helm chart version 2.12.2)
- [Airflow](https://artifacthub.io/packages/helm/airflow-helm/airflow/8.8.0) (helm chart version 8.8.0)

If you are looking to customize the deployments of any of the above dependencies, please refer to the above links for customizations of helm values for further references.

By default, MetaMart Dependencies helm chart provides initial generic customization of these helm values in order to get you started quickly. You can refer to the metamart-dependencies helm charts default values [here](https://github.com/meta-mart/metamart-helm-charts/blob/main/charts/deps/values.yaml).