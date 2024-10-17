---
title: Try MetaMart in Docker
slug: /quick-start/local-kubernetes-deployment
---

# Local Kubernetes Deployment

This installation doc will help you start a MetaMart standalone instance on your local machine.

[metamart-helm-charts](https://github.com/meta-mart/metamart-helm-charts) houses Kubernetes Helm charts 
for deploying MetaMart and its dependencies (Elasticsearch, MySQL and Airflow) on a Kubernetes cluster.



## Requirements

- A local [Kubernetes](https://kubernetes.io/) cluster with installation of [Docker Desktop](https://www.docker.com/products/docker-desktop/) or [MiniKube](https://minikube.sigs.k8s.io/docs/start/)
- [kubectl](https://kubernetes.io/docs/tasks/tools/) to manage Kubernetes resources
- [Helm](https://helm.sh/) to deploy resources based on Helm charts from the MetaMart repository

{%note%}

MetaMart ONLY supports Helm 3.

This guide assumes your helm chart release names as `metamart` and `metamart-dependencies` and the kubernetes namespace used is `default`.

{%/note%}

## Procedure

---

### 1. Start Local Kubernetes Cluster

For this guide, we will be using minikube as our local kubernetes cluster. Run the following command to start a minikube cluster with 4 vCPUs and 8 GiB Memory.

```
minikube start --cpus=4 --memory=8192
```

{%note%}

If you are using minikube to start a local kubernetes instance on MacOS with M1 chipset, use the following command to start the cluster required for MetaMart Helm Charts to install locally (with docker desktop running as container runtime engine).

`minikube start --cpus=4 --memory=8192 --cni=bridge --driver=docker`

{%/note%}

### 2. Create Kubernetes Secrets required for Helm Charts

Create kubernetes secrets that contains MySQL and Airflow passwords as secrets.

```commandline
kubectl create secret generic mysql-secrets --from-literal=metamart-mysql-password=metamart_password
kubectl create secret generic airflow-secrets --from-literal=metamart-airflow-password=admin
kubectl create secret generic airflow-mysql-secrets --from-literal=airflow-mysql-password=airflow_pass
```

### 3. Add Helm Repository for Local Deployment

Run the below command to add MetaMart Helm Repository -

```commandline
helm repo add meta-mart https://helm.meta-mart.org/
```


To verify, run `helm repo list` to ensure the MetaMart repository was added.

```commandline
NAME        	URL                            
meta-mart	https://helm.meta-mart.org/
```

### 4. Install MetaMart Dependencies Helm Chart

We created a separate [chart](https://github.com/meta-mart/metamart-helm-charts/tree/main/charts/deps) to configure and install the MetaMart Application Dependencies with example configurations.

Deploy the dependencies by running the following command -

```commandline
helm install metamart-dependencies meta-mart/metamart-dependencies
```

Run `kubectl get pods` to check whether all the pods for the dependencies are running. You should get a result similar to below.

```commandline
NAME                                                       READY   STATUS     RESTARTS   AGE
opensearch-0                                               1/1     Running   0          4m26s
mysql-0                                                    1/1     Running   0          4m26s
metamart-dependencies-db-migrations-5984f795bc-t46wh   1/1     Running   0          4m26s
metamart-dependencies-scheduler-5b574858b6-75clt       1/1     Running   0          4m26s
metamart-dependencies-sync-users-654b7d58b5-2z5sf      1/1     Running   0          4m26s
metamart-dependencies-triggerer-8d498cc85-wjn69        1/1     Running   0          4m26s
metamart-dependencies-web-64bc79d7c6-7n6v2             1/1     Running   0          4m26s
```

Wait for all the above Pods to be in ***`running`*** status and ***`ready`*** state.

{%note%}

Please note that the pods names above as `metamart-dependencies-*` are part of airflow deployments.

{%/note%}

Helm Chart for MetaMart Dependencies uses the following helm charts:
- [Bitnami MySQL](https://artifacthub.io/packages/helm/bitnami/mysql/9.7.2) (helm chart version 9.7.2)
- [OpenSearch](https://artifacthub.io/packages/helm/opensearch-project-helm-charts/opensearch/2.12.2) (helm chart version 2.12.2)
- [Airflow](https://artifacthub.io/packages/helm/airflow-helm/airflow/8.8.0) (helm chart version 8.8.0)

### 5. Install MetaMart Helm Chart

Deploy MetaMart Application by running the following command -

```commandline
helm install metamart meta-mart/metamart
```

Run **`kubectl get pods --selector=app.kubernetes.io/name=metamart`** to check the status of pods running. You should get a result similar to the output below -

```commandline
NAME                            READY   STATUS    RESTARTS   AGE
metamart-5c55f6759c-52dvq   1/1     Running   0          90s
```

Wait for the above Pod to be in ***`running`*** status and ***`ready`*** state.

### 6. Port Forward MetaMart Kubernetes Service to view UI

To expose the MetaMart UI on a local Kubernetes Cluster, run the below command -

```commandline
kubectl port-forward service/metamart 8585:http
```

The above command will port forward traffic from local machine port 8585 to a named port of MetaMart kubernetes service `http`.

Browse the Application with url `http://localhost:8585` from your Browser. The default login credentials are `admin@meta-mart.org:admin` to log into MetaMart Application.

### 7. Cleanup

Use the below command to uninstall MetaMart Helm Charts Release.

```commandline
helm uninstall metamart
helm uninstall metamart-dependencies
```

MySQL and ElasticSearch MetaMart Dependencies are deployed as StatefulSets and have persistent volumes (pv) and
persistent volume claims (`pvc`). These will need to be manually cleaned after helm uninstall. You can use `kubectl delete persistentvolumeclaims mysql-0 elasticsearch-0` CLI command for the same.

## Troubleshooting

### Pods fail to start due to `ErrImagePull` issue

Sometimes, kubernetes timeout pulling the docker images. In such cases, you will receive `ErrImagePull` issue. In order to resolve this, you can manually pull the required docker images in your kubernetes environment. 

You can find the docker image name of the failing pods using the command below -

```
kubectl get pods -n <NAMESPACE_NAME> <POD_NAME> -o jsonpath="{..image}"
```

The command `docker pull <docker_image_name>` will make sure to get the image available for kubernetes and resolve the issue.

### View metamart kubernetes pod logs

Run the below command to list metamart kubernetes pods deployed in a namespace:

```commandline
kubectl get pods --namespace <NAMESPACE_NAME> -l='app.kubernetes.io/managed-by=Helm,app.kubernetes.io/instance=<RELEASE_NAME>'
```

For example, list pods deployed by helm release name `metamart` in the namespace `ometa-dev`:

```commandline
kubectl get pods --namespace ometa-dev -l='app.kubernetes.io/managed-by=Helm,app.kubernetes.io/instance=metamart'
```

Next, view the logs of pod by running the below command,

```commandline
kubectl logs <POD_NAME> --namespace <NAMESPACE_NAME>
```

For more information, visit the kubectl logs command line reference documentation [here](https://kubernetes.io/docs/tasks/debug-application-cluster/debug-running-pod/).

## Next Steps

1. Refer the [How-to Guides](/how-to-guides) for an overview of all the features in MetaMart.
2. Visit the [Connectors](/connectors) documentation to see what services you can integrate with
   MetaMart.
3. Visit the [API](/swagger.html) documentation and explore the rich set of MetaMart APIs.

## Deploy in Cloud (Production)

{% inlineCalloutContainer %}
  {% inlineCallout
    color="violet-70"
    icon="10k"
    bold="Deploy in Cloud"
    href="/deployment/kubernetes" %}
    Deploy MetaMart in Kubernetes Cloud Environments
  {% /inlineCallout %}
{% /inlineCalloutContainer %}