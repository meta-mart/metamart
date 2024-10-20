# Metadata

Database Service Metadata Pipeline Configuration.

## Configuration

$$section
### Search Index Filter Pattern $(id="searchIndexFilterPattern")

Search index filter patterns to control whether to include search index as part of metadata ingestion. 

**Include**: Explicitly include search index by adding a list of regular expressions to the `Include` field. MetaMart will include all search indexes with names matching one or more of the supplied regular expressions. All other search indexes will be excluded.

For example, to include only those search indexes whose name starts with the word `demo`, add the regex pattern in the include field as `^demo.*`.

**Exclude**: Explicitly exclude search index by adding a list of regular expressions to the `Exclude` field. MetaMart will exclude all search indexes with names matching one or more of the supplied regular expressions. All other search indexes will be included.

For example, to exclude all search indexes with the name containing the word `demo`, add the regex pattern in the exclude field as `.*demo.*`.

Checkout [this](https://docs.meta-mart.org/connectors/ingestion/workflows/metadata/filter-patterns/database#database-filter-pattern) document for further examples on filter patterns.
$$


$$section
### Enable Debug Logs $(id="enableDebugLog")

Set the `Enable Debug Log` toggle to set the logging level of the process to debug. You can check these logs in the Ingestion tab of the service and dig deeper into any errors you might find.
$$


$$section
### Mark Deleted Search Indexes $(id="markDeletedSearchIndexes")

Optional configuration to soft delete `search indexes` in MetaMart if the source `search indexes` are deleted. After deleting, all the associated entities like lineage, etc., with that `search index` will be deleted.
$$

$$section
### Include Sample Data $(id="includeSampleData")

Set the Ingest Sample Data toggle to control whether to ingest sample data as part of metadata ingestion.
$$

$$section
### Override Metadata $(id="overrideMetadata")

Set the `Override Metadata` toggle to control whether to override the existing metadata in the MetaMart server with the metadata fetched from the source.

If the toggle is `enabled`, the metadata fetched from the source will override and replace the existing metadata in the MetaMart.

If the toggle is `disabled`, the metadata fetched from the source will not override the existing metadata in the MetaMart server. In this case the metadata will only get updated for fields that has no value added in MetaMart.

This is applicable for fields like description, tags, owner and displayName

$$

$$section
### Sample Size $(id="sampleSize")

If include sample data is enabled, 10 records will be ingested by default. Using this field you can customize the size of sample data.
$$

