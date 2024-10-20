#### Source Configuration - Source Config

{% codeInfo srNumber=100 %}

The `sourceConfig` is defined [here](https://github.com/meta-mart/MetaMart/blob/main/metamart-spec/src/main/resources/json/schema/metadataIngestion/pipelineServiceMetadataPipeline.json):

**dbServiceNames**: Database Service Name for the creation of lineage, if the source supports it.

**includeTags**: Set the 'Include Tags' toggle to control whether to include tags as part of metadata ingestion.

**includeUnDeployedPipelines**: Set the 'Include UnDeployed Pipelines' toggle to control whether to include un-deployed pipelines as part of metadata ingestion. By default it is set to `true`

**markDeletedPipelines**: Set the Mark Deleted Pipelines toggle to flag pipelines as soft-deleted if they are not present anymore in the source system.

**pipelineFilterPattern** and **chartFilterPattern**: Note that the `pipelineFilterPattern` and `chartFilterPattern` both support regex as include or exclude.

{% /codeInfo %}