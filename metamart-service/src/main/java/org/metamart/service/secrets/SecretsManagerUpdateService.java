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

package org.metamart.service.secrets;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.ServiceConnectionEntityInterface;
import org.metamart.schema.ServiceEntityInterface;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.teams.User;
import org.metamart.service.Entity;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.exception.SecretsManagerUpdateException;
import org.metamart.service.jdbi3.EntityRepository;
import org.metamart.service.jdbi3.IngestionPipelineRepository;
import org.metamart.service.jdbi3.ListFilter;
import org.metamart.service.jdbi3.ServiceEntityRepository;
import org.metamart.service.jdbi3.UserRepository;
import org.metamart.service.jdbi3.WorkflowRepository;
import org.metamart.service.util.EntityUtil;
import org.metamart.service.util.EntityUtil.Fields;

/**
 * Update service using the configured secret manager.
 *
 * <p>- It will update all the services entities with connection parameters
 *
 * <p>- It will update all the user bots with authentication mechanism
 *
 * <p>- It will update all the ingestion pipelines of type metadata with DBT config
 */
@Slf4j
public class SecretsManagerUpdateService {
  private final SecretsManager secretManager;
  private final SecretsManager oldSecretManager;
  private final UserRepository userRepository;
  private final IngestionPipelineRepository ingestionPipelineRepository;
  private final WorkflowRepository workflowRepository;

  private final Map<
          Class<? extends ServiceConnectionEntityInterface>, ServiceEntityRepository<?, ?>>
      connectionTypeRepositoriesMap;

  public SecretsManagerUpdateService(SecretsManager secretsManager, String clusterName) {
    this.secretManager = secretsManager;
    this.connectionTypeRepositoriesMap = retrieveConnectionTypeRepositoriesMap();
    this.userRepository = (UserRepository) Entity.getEntityRepository(Entity.USER);
    this.ingestionPipelineRepository =
        (IngestionPipelineRepository) Entity.getEntityRepository(Entity.INGESTION_PIPELINE);
    this.workflowRepository = (WorkflowRepository) Entity.getEntityRepository(Entity.WORKFLOW);
    // by default, it is going to be non-managed secrets manager since decrypt is the same for all
    // of them
    this.oldSecretManager = SecretsManagerFactory.createSecretsManager(null, clusterName);
  }

  public void updateEntities() {
    updateServices();
    updateBotUsers();
    updateIngestionPipelines();
    updateWorkflows();
  }

  private void updateServices() {
    LOG.info(
        String.format(
            "Updating services in case of an update on the JSON schema: [%s]",
            secretManager.getSecretsManagerProvider().value()));
    retrieveServices().forEach(this::updateService);
  }

  private void updateBotUsers() {
    LOG.info(
        String.format(
            "Updating bot users in case of an update on the JSON schema: [%s]",
            secretManager.getSecretsManagerProvider().value()));
    retrieveBotUsers().forEach(this::updateBotUser);
  }

  private void updateIngestionPipelines() {
    LOG.info(
        String.format(
            "Updating ingestion pipelines in case of an update on the JSON schema: [%s]",
            secretManager.getSecretsManagerProvider().value()));
    retrieveIngestionPipelines().forEach(this::updateIngestionPipeline);
  }

  private void updateWorkflows() {
    LOG.info(
        String.format(
            "Updating workflows in case of an update on the JSON schema: [%s]",
            secretManager.getSecretsManagerProvider().value()));
    retrieveWorkflows().forEach(this::updateWorkflow);
  }

  private void updateService(ServiceEntityInterface serviceEntityInterface) {
    ServiceEntityRepository<?, ?> repository =
        connectionTypeRepositoriesMap.get(serviceEntityInterface.getConnection().getClass());
    try {
      ServiceEntityInterface service =
          repository.getDao().findEntityById(serviceEntityInterface.getId());
      // we have to decrypt using the old secrets manager and encrypt again with the new one
      service
          .getConnection()
          .setConfig(
              oldSecretManager.decryptServiceConnectionConfig(
                  service.getConnection().getConfig(),
                  service.getServiceType().value(),
                  repository.getServiceType()));
      service
          .getConnection()
          .setConfig(
              secretManager.encryptServiceConnectionConfig(
                  service.getConnection().getConfig(),
                  service.getServiceType().value(),
                  service.getName(),
                  repository.getServiceType()));
      repository.getDao().update(service);
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private List<ServiceEntityInterface> retrieveServices() {
    return connectionTypeRepositoriesMap.values().stream()
        .map(this::retrieveServices)
        .flatMap(List<ServiceEntityInterface>::stream)
        .collect(Collectors.toList());
  }

  private List<ServiceEntityInterface> retrieveServices(
      ServiceEntityRepository<?, ?> serviceEntityRepository) {
    try {
      return serviceEntityRepository
          .listAfter(
              null,
              EntityUtil.Fields.EMPTY_FIELDS,
              new ListFilter(),
              serviceEntityRepository.getDao().listCount(new ListFilter()),
              null)
          .getData()
          .stream()
          .map(ServiceEntityInterface.class::cast)
          .filter(
              service ->
                  !Objects.isNull(service.getConnection())
                      && !Objects.isNull(service.getConnection().getConfig()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private Map<Class<? extends ServiceConnectionEntityInterface>, ServiceEntityRepository<?, ?>>
      retrieveConnectionTypeRepositoriesMap() {
    Map<Class<? extends ServiceConnectionEntityInterface>, ServiceEntityRepository<?, ?>>
        connTypeRepositoriesMap =
            Entity.getEntityList().stream()
                .map(this::retrieveServiceRepository)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(
                    Collectors.toMap(
                        ServiceEntityRepository::getServiceConnectionClass, Function.identity()));

    if (connTypeRepositoriesMap.isEmpty()) {
      throw new SecretsManagerUpdateException("Unexpected error: ServiceRepository not found.");
    }
    return connTypeRepositoriesMap;
  }

  private Optional<ServiceEntityRepository<?, ?>> retrieveServiceRepository(String entityType) {
    try {
      EntityRepository<? extends EntityInterface> repository =
          Entity.getEntityRepository(entityType);
      if (ServiceEntityRepository.class.isAssignableFrom(repository.getClass())) {
        return Optional.of(((ServiceEntityRepository<?, ?>) repository));
      }
      return Optional.empty();
    } catch (EntityNotFoundException e) {
      return Optional.empty();
    }
  }

  private List<User> retrieveBotUsers() {
    try {
      return userRepository
          .listAfter(
              null,
              new Fields(Set.of("authenticationMechanism")),
              new ListFilter(),
              userRepository.getDao().listCount(new ListFilter()),
              null)
          .getData()
          .stream()
          .filter(user -> Boolean.TRUE.equals(user.getIsBot()))
          .collect(Collectors.toList());
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private void updateBotUser(User botUser) {
    try {
      User user = userRepository.getDao().findEntityById(botUser.getId());
      oldSecretManager.decryptAuthenticationMechanism(
          botUser.getName(), user.getAuthenticationMechanism());
      secretManager.encryptAuthenticationMechanism(
          botUser.getName(), user.getAuthenticationMechanism());
      userRepository.getDao().update(user);
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private List<IngestionPipeline> retrieveIngestionPipelines() {
    try {
      return ingestionPipelineRepository
          .listAfter(
              null,
              EntityUtil.Fields.EMPTY_FIELDS,
              new ListFilter(),
              ingestionPipelineRepository.getDao().listCount(new ListFilter()),
              null)
          .getData();
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private List<Workflow> retrieveWorkflows() {
    try {
      return workflowRepository
          .listAfter(
              null,
              EntityUtil.Fields.EMPTY_FIELDS,
              new ListFilter(),
              workflowRepository.getDao().listCount(new ListFilter()),
              null)
          .getData();
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private void updateIngestionPipeline(IngestionPipeline ingestionPipeline) {
    try {
      IngestionPipeline ingestion =
          ingestionPipelineRepository.getDao().findEntityById(ingestionPipeline.getId());
      // we have to decrypt using the old secrets manager and encrypt again with the new one
      oldSecretManager.decryptIngestionPipeline(ingestionPipeline);
      secretManager.encryptIngestionPipeline(ingestionPipeline);
      ingestionPipelineRepository.getDao().update(ingestion);
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }

  private void updateWorkflow(Workflow workflow) {
    try {
      Workflow workflowObject = workflowRepository.getDao().findEntityById(workflow.getId());
      // we have to decrypt using the old secrets manager and encrypt again with the new one
      workflowObject = oldSecretManager.decryptWorkflow(workflowObject);
      workflowObject = secretManager.encryptWorkflow(workflowObject);
      ingestionPipelineRepository.getDao().update(workflowObject);
    } catch (Exception e) {
      throw new SecretsManagerUpdateException(e.getMessage(), e.getCause());
    }
  }
}
