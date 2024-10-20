package org.metamart.service.pipelineService;

import java.util.List;
import java.util.Map;
import org.metamart.schema.ServiceEntityInterface;
import org.metamart.schema.api.configuration.pipelineServiceClient.PipelineServiceClientConfiguration;
import org.metamart.schema.entity.app.App;
import org.metamart.schema.entity.app.AppMarketPlaceDefinition;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineServiceClientResponse;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineStatus;
import org.metamart.service.clients.pipeline.PipelineServiceClient;

public class MockPipelineServiceClient extends PipelineServiceClient {

  public MockPipelineServiceClient(
      PipelineServiceClientConfiguration pipelineServiceClientConfiguration) {
    super(pipelineServiceClientConfiguration);
  }

  @Override
  public PipelineServiceClientResponse getServiceStatusInternal() {
    return null;
  }

  @Override
  public PipelineServiceClientResponse runAutomationsWorkflow(Workflow workflow) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse runApplicationFlow(App application) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse validateAppRegistration(AppMarketPlaceDefinition app) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse deployPipeline(
      IngestionPipeline ingestionPipeline, ServiceEntityInterface service) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse runPipeline(
      IngestionPipeline ingestionPipeline, ServiceEntityInterface service) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse deletePipeline(IngestionPipeline ingestionPipeline) {
    return null;
  }

  @Override
  public List<PipelineStatus> getQueuedPipelineStatusInternal(IngestionPipeline ingestionPipeline) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse toggleIngestion(IngestionPipeline ingestionPipeline) {
    return null;
  }

  @Override
  public Map<String, String> getLastIngestionLogs(
      IngestionPipeline ingestionPipeline, String after) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse killIngestion(IngestionPipeline ingestionPipeline) {
    return null;
  }

  @Override
  public Map<String, String> requestGetHostIp() {
    return null;
  }
}
