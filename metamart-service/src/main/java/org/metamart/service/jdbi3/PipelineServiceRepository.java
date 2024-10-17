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

package org.metamart.service.jdbi3;

import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.entity.services.PipelineService;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.type.PipelineConnection;
import org.metamart.service.Entity;
import org.metamart.service.resources.services.pipeline.PipelineServiceResource;

@Slf4j
public class PipelineServiceRepository
    extends ServiceEntityRepository<PipelineService, PipelineConnection> {

  public PipelineServiceRepository() {
    super(
        PipelineServiceResource.COLLECTION_PATH,
        Entity.PIPELINE_SERVICE,
        Entity.getCollectionDAO().pipelineServiceDAO(),
        PipelineConnection.class,
        "",
        ServiceType.PIPELINE);
    supportsSearch = true;
  }
}
