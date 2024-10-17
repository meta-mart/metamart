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

package org.metamart.service.secrets.masker;

import java.util.Set;
import org.metamart.schema.auth.BasicAuthMechanism;
import org.metamart.schema.auth.JWTAuthMechanism;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.teams.AuthenticationMechanism;
import org.metamart.schema.security.client.MetaMartJWTClientConfig;

public abstract class EntityMasker {

  protected static final Set<Class<?>> DO_NOT_MASK_CLASSES =
      Set.of(MetaMartJWTClientConfig.class, JWTAuthMechanism.class, BasicAuthMechanism.class);

  public abstract Object maskServiceConnectionConfig(
      Object connectionConfig, String connectionType, ServiceType serviceType);

  public abstract void maskAuthenticationMechanism(
      String name, AuthenticationMechanism authenticationMechanism);

  public abstract void maskIngestionPipeline(IngestionPipeline ingestionPipeline);

  public abstract Workflow maskWorkflow(Workflow workflow);

  public abstract Object unmaskServiceConnectionConfig(
      Object connectionConfig,
      Object originalConnectionConfig,
      String connectionType,
      ServiceType serviceType);

  public abstract void unmaskIngestionPipeline(
      IngestionPipeline ingestionPipeline, IngestionPipeline originalIngestionPipeline);

  public abstract void unmaskAuthenticationMechanism(
      String name,
      AuthenticationMechanism authenticationMechanism,
      AuthenticationMechanism originalAuthenticationMechanism);

  public abstract Workflow unmaskWorkflow(Workflow workflow, Workflow originalWorkflow);
}
