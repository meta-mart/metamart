#### Source Configuration - Source Config

{% codeInfo srNumber=100 %}

The `sourceConfig` is defined [here](https://github.com/meta-mart/MetaMart/blob/main/metamart-spec/src/main/resources/json/schema/metadataIngestion/searchServiceMetadataPipeline.json):

**includeSampleData**: Set the Ingest Sample Data toggle to control whether to ingest sample data as part of metadata ingestion.

**sampleSize**: If include sample data is enabled, 10 records will be ingested by default. Using this field you can customize the size of sample data.

**markDeletedSearchIndexes**: Optional configuration to soft delete `search indexes` in MetaMart if the source `search indexes` are deleted. After deleting, all the associated entities like lineage, etc., with that `search index` will be deleted.

**searchIndexFilterPattern**: Note that the `searchIndexFilterPattern` support regex to include or exclude search indexes during metadata ingestion process.

{% /codeInfo %}