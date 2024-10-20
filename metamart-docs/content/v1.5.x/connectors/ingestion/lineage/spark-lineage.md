---
title: Spark Lineage Ingestion
slug: /connectors/ingestion/lineage/spark-lineage
---

# Spark Lineage Ingestion

A spark job may involve movement/transfer of data which may result into a data lineage, to capture such lineages you can make use of `MetaMart Spark Agent` which you can configure with your spark session and capture these spark lineages into your MetaMart instance.

In this guide we will explain how you can make use of the `MetaMart Spark Agent` to capture such lineage.


## Requirement

To use the `MetaMart Spark Agent`, you will have to download the latest jar from [here](https://github.com/meta-mart/metamart-spark-agent/releases).

We support spark version 3.1 and above.


## Configuration

While configuring the spark session, in this guide we will make use of PySpark to demonstrate the use of `MetaMart Spark Agent`


{% codePreview %}

{% codeInfoContainer %}

{% codeInfo srNumber=1 %}

Once you have downloaded the jar from [here](https://github.com/meta-mart/metamart-spark-agent/releases) in your spark configuration you will have to add the path to your `metamart-spark-agent.jar` along with other required jars to run your spark job, in this example it is `mysql-connector-java.jar`

{% /codeInfo %}



{% codeInfo srNumber=2 %}

`metamart-spark-agent.jar` comes with a custom spark listener i.e. `org.metamart.spark.agent.MetaMartSparkListener` you will need to add this as `extraListeners` spark configuration.

{% /codeInfo %}


{% codeInfo srNumber=3 %}

`spark.metamart.transport.hostPort`: Specify the host & port of the instance where your MetaMart is hosted.

{% /codeInfo %}

{% codeInfo srNumber=4 %}

`spark.metamart.transport.type` is required configuration with value as `metamart`.

{% /codeInfo %}


{% codeInfo srNumber=5 %}

`spark.metamart.transport.jwtToken`: Specify your MetaMart Jwt token here. Checkout [this](/deployment/security/enable-jwt-tokens#generate-token) documentation on how you can generate a jwt token in MetaMart.

{% /codeInfo %}


{% codeInfo srNumber=6 %}

`spark.metamart.transport.pipelineServiceName`: This spark job will be creating a new pipeline service of type `Spark`, use this configuration to customize the pipeline service name.

Note: If the pipeline service with the specified name already exists then we will be updating/using the same pipeline service.

{% /codeInfo %}

{% codeInfo srNumber=7 %}

`spark.metamart.transport.pipelineName`: This spark job will also create a new pipeline within the pipeline service defined above. Use this configuration to customize the name of pipeline.

Note: If the pipeline with the specified name already exists then we will be updating/using the same pipeline.

{% /codeInfo %}


{% codeInfo srNumber=8 %}

`spark.metamart.transport.pipelineSourceUrl`: You can use this configuration to provide additional context to your pipeline by specifying a url related to the pipeline.

{% /codeInfo %}

{% codeInfo srNumber=9 %}

`spark.metamart.transport.pipelineDescription`: Provide pipeline description using this spark configuration.

{% /codeInfo %}

{% codeInfo srNumber=10 %}

`spark.metamart.transport.databaseServiceNames`: Provide the comma separated list of database service names which contains the source tables used in this job. If you do not provide this configuration then we will be searching through all the services available in metamart.

{% /codeInfo %}


{% codeInfo srNumber=11 %}

`spark.metamart.transport.timeout`: Provide the timeout to communicate with MetaMart APIs.

{% /codeInfo %}

{% codeInfo srNumber=12 %}

In this job we are reading data from `employee` table and moving it to another table `employee_new` of within same mysql source.

{% /codeInfo %}

{% /codeInfoContainer %}

{% codeBlock fileName="pyspark.py" %}


```py {% isCodeBlock=true %}
from pyspark.sql import SparkSession

spark = (
    SparkSession.builder.master("local")
    .appName("localTestApp")
```
```py {% srNumber=1 %}
    .config(
        "spark.jars",
        "path/to/metamart-spark-agent.jar,path/to/mysql-connector-java-8.0.30.jar",
    )
```
```py {% srNumber=2 %}
    .config(
        "spark.extraListeners",
        "org.metamart.spark.agent.MetaMartSparkListener",
    )
```
```py {% srNumber=3 %}
    .config("spark.metamart.transport.hostPort", "http://localhost:8585")
```
```py {% srNumber=4 %}
    .config("spark.metamart.transport.type", "metamart")
```
```py {% srNumber=5 %}
    .config("spark.metamart.transport.jwtToken", "<metamart-jwt-token>")
```
```py {% srNumber=6 %}
    .config(
        "spark.metamart.transport.pipelineServiceName", "my_pipeline_service"
    )
```
```py {% srNumber=7 %}
    .config("spark.metamart.transport.pipelineName", "my_pipeline_name")
```
```py {% srNumber=8 %}
    .config(
        "spark.metamart.transport.pipelineSourceUrl",
        "http://your.org/path/to/pipeline",
    )
```
```py {% srNumber=9 %}
    .config(
        "spark.metamart.transport.pipelineDescription", "My ETL Pipeline"
    )
```
```py {% srNumber=10 %}
    .config(
        "spark.metamart.transport.databaseServiceNames",
        "random,local_mysql",
    )
```
```py {% srNumber=11 %}
    .config("spark.metamart.transport.timeout", "30")
```
```py {% srNumber=12 %}
    .getOrCreate()
)

# Read table using jdbc()

# Read from MySQL Table
employee_df = (
    spark.read.format("jdbc")
    .option("url", "jdbc:mysql://localhost:3306/metamart_db")
    .option("driver", "com.mysql.cj.jdbc.Driver")
    .option("dbtable", "employee")
    .option("user", "metamart_user")
    .option("password", "metamart_password")
    .load()
)

# Write data to the new employee_new table
(
    employee_df.write.format("jdbc")
    .option("url", "jdbc:mysql://localhost:3306/metamart_db")
    .option("driver", "com.mysql.cj.jdbc.Driver")
    .option("dbtable", "employee_new")
    .option("user", "metamart_user")
    .option("password", "metamart_password")
    .mode("overwrite")
    .save()
)

# Stop the Spark session
spark.stop()
```

{% /codeBlock %}

{% /codePreview %}


Once this pyspark job get finished you will see a new pipeline service with name `my_pipeline_service` generated in your metamart instance which would contain a pipeline with name `my_pipeline` as per the above example and you should also see lineage between the table `employee` and `employee_new` via `my_pipeline`.


{% image
  src="/images/v1.5/connectors/spark/spark-pipeline-service.png"
  alt="Spark Pipeline Service"
  caption="Spark Pipeline Service"
 /%}


 {% image
  src="/images/v1.5/connectors/spark/spark-pipeline-details.png"
  alt="Spark Pipeline Details"
  caption="Spark Pipeline Details"
 /%}



 {% image
  src="/images/v1.5/connectors/spark/spark-pipeline-lineage.png"
  alt="Spark Pipeline Lineage"
  caption="Spark Pipeline Lineage"
 /%}

## Using Spark Agent with Databricks

Follow the below steps in order to use MetaMart Spark Agent with databricks.

### 1. Upload the jar to compute cluster

To use the `MetaMart Spark Agent`, you will have to download the latest jar from [here](https://github.com/meta-mart/metamart-spark-agent/releases) and upload it to your databricks compute cluster.

To upload the jar you can visit the compute details page and then go to the libraries tab

{% image
  src="/images/v1.5/connectors/spark/spark-upload-jar.png"
  alt="Spark Upload Jar"
  caption="Spark Upload Jar"
 /%}

Click on the "Install Now" button and choose `dbfs` mode and upload the `MetaMart Spark Agent` jar.

{% image
  src="/images/v1.5/connectors/spark/spark-upload-jar-2.png"
  alt="Spark Upload Jar"
  caption="Spark Upload Jar"
 /%}

Once your jar is uploaded copy the path of the jar for the next steps.

{% image
  src="/images/v1.5/connectors/spark/spark-uploaded-jar.png"
  alt="Spark Upload Jar"
  caption="Spark Upload Jar"
 /%}


### 2. Create Initialization Script


Once your jar is uploaded you need to create a initialization script in your workspace.

```
#!/bin/bash

STAGE_DIR_JAR="<path to jar copied from step 1>"

echo "BEGIN: Upload Spark Listener JARs"
cp -f $STAGE_DIR_JAR /mnt/driver-daemon/jars || { echo "Error copying Spark Listener library file"; exit 1;}
echo "END: Upload Spark Listener JARs"

echo "BEGIN: Modify Spark config settings"
cat << 'EOF' > /databricks/driver/conf/openlineage-spark-driver-defaults.conf
[driver] {
  "spark.extraListeners" = "org.metamart.spark.agent.MetaMartSparkListener"
}
EOF
echo "END: Modify Spark config settings"
```

Note: The copied path would look like this `dbfs:/FileStore/jars/....` you need to modify it like `/dbfs/FileStore/jars/...` this.

{% image
  src="/images/v1.5/connectors/spark/prepare-script.png"
  alt="Prepare Script"
  caption="Prepare Script"
 /%}



### 3. Configure Initialization Script

Once you have created a initialization script, you will need to attach this script to your compute instance, to do that you can go to advanced config > init scripts and add your script path.

{% image
  src="/images/v1.5/connectors/spark/prepare-script.png"
  alt="Prepare Script"
  caption="Prepare Script"
 /%}


{% image
  src="/images/v1.5/connectors/spark/spark-init-script.png"
  alt="Spark Init Script"
  caption="Spark Init Script"
 /%}


### 4. Configure Spark

After configuring the init script, you will need to update the spark config as well.


{% image
  src="/images/v1.5/connectors/spark/spark-config-set.png"
  alt="Spark Set Config"
  caption="Spark Set Config"
 /%}

these are the possible configurations that you can do, please refer the `Configuration` section above to get the detailed information about the same. 

```
spark.extraListeners org.metamart.spark.agent.MetaMartSparkListener
spark.metamart.transport.type metamart
spark.metamart transport.pipelineSourceUrl http://<your-pipeline-host-port> 
spark.metamart transport.pipelineDescription "your pipeline description" 
spark.metamart.transport.hostPort https://<your-metamart-host-port> 
spark metamart transport.pipelineServiceName demo_pipeline 
spark.metamart transport.pipelineName demo_pipeline 
spark.metamart transport.databaseServiceNames db-service-name1,db-service-name2 
spark.metamart.transport.jwtToken <your-jwt-token> 
spark.metamart.transport.timeout 30
```

After all these steps are completed you can start/restart your compute instance and you are ready to extract the lineage from spark to MetaMart.
