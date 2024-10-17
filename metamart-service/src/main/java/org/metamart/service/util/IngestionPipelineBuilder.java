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

package org.metamart.service.util;

import static org.metamart.schema.entity.services.ingestionPipelines.PipelineType.DBT;

import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.metadataIngestion.DbtPipeline;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.service.secrets.converter.ClassConverterFactory;

public final class IngestionPipelineBuilder {

  private IngestionPipelineBuilder() {
    // Final
  }

  /** Build `IngestionPipeline` object with concrete class for the config which by definition it is a `Object`. */
  public static void addDefinedConfig(IngestionPipeline ingestionPipeline) {
    if (DBT.equals(ingestionPipeline.getPipelineType())
        && ingestionPipeline.getSourceConfig() != null) {
      ingestionPipeline
          .getSourceConfig()
          .setConfig(
              ClassConverterFactory.getConverter(DbtPipeline.class)
                  .convert(ingestionPipeline.getSourceConfig().getConfig()));
    }
    if (ingestionPipeline.getMetaMartServerConnection() != null) {
      ingestionPipeline.setMetaMartServerConnection(
          (MetaMartConnection)
              ClassConverterFactory.getConverter(MetaMartConnection.class)
                  .convert(ingestionPipeline.getMetaMartServerConnection()));
    }
  }
}
