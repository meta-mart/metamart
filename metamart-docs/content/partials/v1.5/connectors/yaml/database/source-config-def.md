#### Source Configuration - Source Config

{% codeInfo srNumber=100 %}

The `sourceConfig` is defined [here](https://github.com/meta-mart/MetaMart/blob/main/metamart-spec/src/main/resources/json/schema/metadataIngestion/databaseServiceMetadataPipeline.json):

**markDeletedTables**: To flag tables as soft-deleted if they are not present anymore in the source system.

**markDeletedStoredProcedures**: Optional configuration to soft delete stored procedures in MetaMart if the source stored procedures are deleted. Also, if the stored procedures is deleted, all the associated entities like lineage, etc., with that stored procedures will be deleted.

**includeTables**: true or false, to ingest table data. Default is true.

**includeViews**: true or false, to ingest views definitions.

**includeTags**: Optional configuration to toggle the tags ingestion.

**includeOwners**: Set the 'Include Owners' toggle to control whether to include owners to the ingested entity if the owner email matches with a user stored in the OM server as part of metadata ingestion. If the ingested entity already exists and has an owner, the owner will not be overwritten.

**includeStoredProcedures**: Optional configuration to toggle the Stored Procedures ingestion.

**includeDDL**: Optional configuration to toggle the DDL Statements ingestion.

**queryLogDuration**: Configuration to tune how far we want to look back in query logs to process Stored Procedures results. 

**queryParsingTimeoutLimit**: Configuration to set the timeout for parsing the query in seconds.

**useFqnForFiltering**: Regex will be applied on fully qualified name (e.g service_name.db_name.schema_name.table_name) instead of raw name (e.g. table_name).

**databaseFilterPattern**, **schemaFilterPattern**, **tableFilterPattern**: Note that the filter supports regex as include or exclude. You can find examples [here](/connectors/ingestion/workflows/metadata/filter-patterns/database)

**threads (beta)**: The number of threads to use when extracting the metadata using multithreading. Please take a look [here](/connectors/ingestion/workflows/metadata/multithreading) before configuring this.

**incremental (beta)**: Incremental Extraction configuration. Currently implemented for:

- [BigQuery](/connectors/ingestion/workflows/metadata/incremental-extraction/bigquery)
- [Redshift](/connectors/ingestion/workflows/metadata/incremental-extraction/redshift)
- [Snowflake](/connectors/ingestion/workflows/metadata/incremental-extraction/snowflake)

{% /codeInfo %}
