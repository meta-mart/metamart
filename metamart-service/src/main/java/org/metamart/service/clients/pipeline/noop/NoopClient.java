/*
 *  Copyright 2021 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.clients.pipeline.noop;

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
import org.metamart.sdk.exception.PipelineServiceClientException;
import org.metamart.service.clients.pipeline.PipelineServiceClient;

public class NoopClient extends PipelineServiceClient {

  static final String EXCEPTION_MSG = "The NoopClient does not implement the %s method";

  public NoopClient(PipelineServiceClientConfiguration pipelineServiceClientConfiguration) {
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
    throw new PipelineServiceClientException(String.format(EXCEPTION_MSG, "deploy"));
  }

  @Override
  public PipelineServiceClientResponse runPipeline(
      IngestionPipeline ingestionPipeline, ServiceEntityInterface service) {
    throw new PipelineServiceClientException(String.format(EXCEPTION_MSG, "run"));
  }

  @Override
  public PipelineServiceClientResponse deletePipeline(IngestionPipeline ingestionPipeline) {
    throw new PipelineServiceClientException(String.format(EXCEPTION_MSG, "delete"));
  }

  @Override
  public List<PipelineStatus> getQueuedPipelineStatusInternal(IngestionPipeline ingestionPipeline) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse toggleIngestion(IngestionPipeline ingestionPipeline) {
    throw new PipelineServiceClientException(String.format(EXCEPTION_MSG, "toggle"));
  }

  @Override
  public Map<String, String> getLastIngestionLogs(
      IngestionPipeline ingestionPipeline, String after) {
    return null;
  }

  @Override
  public PipelineServiceClientResponse killIngestion(IngestionPipeline ingestionPipeline) {
    throw new PipelineServiceClientException(String.format(EXCEPTION_MSG, "kill"));
  }

  @Override
  public Map<String, String> requestGetHostIp() {
    return null;
  }
}
