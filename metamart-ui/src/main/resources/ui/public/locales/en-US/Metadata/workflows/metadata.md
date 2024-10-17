# Metadata

MetadataService Metadata Pipeline Configuration.

## Configuration

$$section
### Enable Debug Logs $(id="enableDebugLog")

Set the `Enable Debug Log` toggle to set the logging level of the process to debug. You can check these logs in the Ingestion tab of the service and dig deeper into any errors you might find.
$$

$$section
### Override Metadata $(id="overrideMetadata")

Set the `Override Metadata` toggle to control whether to override the existing metadata in the MetaMart server with the metadata fetched from the source.

If the toggle is `enabled`, the metadata fetched from the source will override and replace the existing metadata in the MetaMart.

If the toggle is `disabled`, the metadata fetched from the source will not override the existing metadata in the MetaMart server. In this case the metadata will only get updated for fields that has no value added in MetaMart.

This is applicable for fields like description, tags, owner and displayName

$$

$$section
### Number of Retries $(id="retries")

Times to retry the workflow in case it ends with a failure.
$$