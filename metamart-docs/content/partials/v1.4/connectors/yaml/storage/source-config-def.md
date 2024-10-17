#### Source Configuration - Source Config

{% codeInfo srNumber=100 %}

The `sourceConfig` is defined [here](https://github.com/meta-mart/MetaMart/blob/main/metamart-spec/src/main/resources/json/schema/metadataIngestion/storageServiceMetadataPipeline.json):

**containerFilterPattern**: Note that the filter supports regex as include or exclude. You can find examples [here](/connectors/ingestion/workflows/metadata/filter-patterns/database).

**storageMetadataConfigSource**: Path to the `metamart_storage_manifest.json` global manifest file. It can be located in S3, a local path or as a URL to the file.

{% /codeInfo %}