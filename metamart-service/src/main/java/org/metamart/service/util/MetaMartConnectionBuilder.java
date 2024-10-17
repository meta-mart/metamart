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

import java.util.Objects;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.api.configuration.pipelineServiceClient.PipelineServiceClientConfiguration;
import org.metamart.schema.auth.JWTAuthMechanism;
import org.metamart.schema.auth.SSOAuthMechanism;
import org.metamart.schema.entity.Bot;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.teams.AuthenticationMechanism;
import org.metamart.schema.entity.teams.User;
import org.metamart.schema.security.client.MetaMartJWTClientConfig;
import org.metamart.schema.security.secrets.SecretsManagerClientLoader;
import org.metamart.schema.security.secrets.SecretsManagerProvider;
import org.metamart.schema.security.ssl.ValidateSSLClientConfig;
import org.metamart.schema.security.ssl.VerifySSL;
import org.metamart.schema.services.connections.metadata.AuthProvider;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.jdbi3.BotRepository;
import org.metamart.service.jdbi3.IngestionPipelineRepository;
import org.metamart.service.jdbi3.UserRepository;
import org.metamart.service.secrets.SecretsManager;
import org.metamart.service.secrets.SecretsManagerFactory;
import org.metamart.service.util.EntityUtil.Fields;

@Slf4j
public class MetaMartConnectionBuilder {

  AuthProvider authProvider;
  MetaMartJWTClientConfig securityConfig;
  private VerifySSL verifySSL;
  private String metaMartURL;
  private String clusterName;
  private SecretsManagerProvider secretsManagerProvider;
  private SecretsManagerClientLoader secretsManagerLoader;
  private Object metaMartSSLConfig;
  BotRepository botRepository;
  UserRepository userRepository;
  SecretsManager secretsManager;

  public MetaMartConnectionBuilder(
      MetaMartApplicationConfig metaMartApplicationConfig) {
    initializeMetaMartConnectionBuilder(metaMartApplicationConfig);
    initializeBotUser(Entity.INGESTION_BOT_NAME);
  }

  public MetaMartConnectionBuilder(
      MetaMartApplicationConfig metaMartApplicationConfig, String botName) {
    initializeMetaMartConnectionBuilder(metaMartApplicationConfig);
    initializeBotUser(botName);
  }

  public MetaMartConnectionBuilder(
      MetaMartApplicationConfig metaMartApplicationConfig,
      IngestionPipeline ingestionPipeline) {
    initializeMetaMartConnectionBuilder(metaMartApplicationConfig);
    // Try to load the pipeline bot or default to using the ingestion bot
    try {
      initializeBotUser(getBotFromPipeline(ingestionPipeline));
    } catch (Exception e) {
      LOG.warn(
          String.format(
              "Could not initialize bot for pipeline [%s] due to [%s]",
              ingestionPipeline.getPipelineType(), e));
      initializeBotUser(Entity.INGESTION_BOT_NAME);
    }
  }

  private String getBotFromPipeline(IngestionPipeline ingestionPipeline) {
    String botName;
    switch (ingestionPipeline.getPipelineType()) {
      case METADATA, DBT -> botName = Entity.INGESTION_BOT_NAME;
      case APPLICATION -> {
        String type = IngestionPipelineRepository.getPipelineWorkflowType(ingestionPipeline);
        botName = String.format("%sApplicationBot", type);
      }
        // TODO: Remove this once we internalize the DataInsights app
        // For now we need it since DataInsights has its own pipelineType inherited from when it was
        // a standalone workflow
      case DATA_INSIGHT -> botName = "DataInsightsApplicationBot";
      default -> botName =
          String.format("%s-bot", ingestionPipeline.getPipelineType().toString().toLowerCase());
    }
    return botName;
  }

  private void initializeMetaMartConnectionBuilder(
      MetaMartApplicationConfig metaMartApplicationConfig) {
    botRepository = (BotRepository) Entity.getEntityRepository(Entity.BOT);
    userRepository = (UserRepository) Entity.getEntityRepository(Entity.USER);

    PipelineServiceClientConfiguration pipelineServiceClientConfiguration =
        metaMartApplicationConfig.getPipelineServiceClientConfiguration();
    metaMartURL = pipelineServiceClientConfiguration.getMetadataApiEndpoint();
    verifySSL = pipelineServiceClientConfiguration.getVerifySSL();

    /*
     How this information flows:
     - The OM Server has SSL configured
     - We need to provide a way to tell the pipelineServiceClient to use / not use it when connecting
       to the server.

     Then, we pick up this information from the pipelineServiceClient configuration and will pass it
     inside the MetaMartServerConnection property of the IngestionPipeline.

     Based on that, the Ingestion Framework will instantiate the client. This means,
     that the SSL configs we add here are to go from pipelineServiceClient -> MetaMart Server.
    */
    metaMartSSLConfig =
        getOMSSLConfigFromPipelineServiceClient(
            pipelineServiceClientConfiguration.getVerifySSL(),
            pipelineServiceClientConfiguration.getSslConfig());

    clusterName = metaMartApplicationConfig.getClusterName();
    secretsManagerLoader = pipelineServiceClientConfiguration.getSecretsManagerLoader();
    secretsManager = SecretsManagerFactory.getSecretsManager();
    secretsManagerProvider = secretsManager.getSecretsManagerProvider();
  }

  private void initializeBotUser(String botName) {
    User botUser = retrieveBotUser(botName);
    securityConfig = extractSecurityConfig(botUser);
    authProvider = extractAuthProvider(botUser);
  }

  private AuthProvider extractAuthProvider(User botUser) {
    AuthenticationMechanism.AuthType authType = botUser.getAuthenticationMechanism().getAuthType();
    return switch (authType) {
      case SSO -> AuthProvider.fromValue(
          JsonUtils.convertValue(
                  botUser.getAuthenticationMechanism().getConfig(), SSOAuthMechanism.class)
              .getSsoServiceType()
              .value());
      case JWT -> AuthProvider.METAMART;
      default -> throw new IllegalArgumentException(
          String.format("Not supported authentication mechanism type: [%s]", authType.value()));
    };
  }

  private MetaMartJWTClientConfig extractSecurityConfig(User botUser) {
    AuthenticationMechanism authMechanism = botUser.getAuthenticationMechanism();
    if (Objects.requireNonNull(botUser.getAuthenticationMechanism().getAuthType())
        == AuthenticationMechanism.AuthType.JWT) {
      JWTAuthMechanism jwtAuthMechanism =
          JsonUtils.convertValue(authMechanism.getConfig(), JWTAuthMechanism.class);
      secretsManager.decryptJWTAuthMechanism(jwtAuthMechanism);
      return new MetaMartJWTClientConfig().withJwtToken(jwtAuthMechanism.getJWTToken());
    }
    throw new IllegalArgumentException(
        String.format(
            "Not supported authentication mechanism type: [%s]",
            authMechanism.getAuthType().value()));
  }

  public MetaMartConnection build() {
    return new MetaMartConnection()
        .withAuthProvider(authProvider)
        .withHostPort(metaMartURL)
        .withSecurityConfig(securityConfig)
        .withVerifySSL(verifySSL)
        .withClusterName(clusterName)
        // What is the SM configuration, i.e., tool used to manage secrets: AWS SM, Parameter
        // Store,...
        .withSecretsManagerProvider(secretsManagerProvider)
        // How the Ingestion Framework will know how to load the SM creds in the client side, e.g.,
        // airflow.cfg
        .withSecretsManagerLoader(secretsManagerLoader)
        /*
        This is not about the pipeline service client SSL, but the OM server SSL.
        The Ingestion Framework will use this value to load the certificates when connecting to the server.
        */
        .withSslConfig(metaMartSSLConfig);
  }

  private User retrieveBotUser(String botName) {
    User botUser = retrieveIngestionBotUser(botName);
    if (botUser == null) {
      throw new IllegalArgumentException("Please, verify that the ingestion-bot is present.");
    }
    return botUser;
  }

  private User retrieveIngestionBotUser(String botName) {
    try {
      Bot bot = botRepository.getByName(null, botName, Fields.EMPTY_FIELDS);
      if (bot.getBotUser() == null) {
        return null;
      }
      User user =
          userRepository.getByName(
              null,
              bot.getBotUser().getFullyQualifiedName(),
              new EntityUtil.Fields(Set.of("authenticationMechanism")));
      if (user.getAuthenticationMechanism() != null) {
        user.getAuthenticationMechanism().setConfig(user.getAuthenticationMechanism().getConfig());
      }
      return user;
    } catch (EntityNotFoundException ex) {
      LOG.debug((String.format("User for bot [%s]", botName)) + " [{}] not found.", botName);
      return null;
    }
  }

  protected Object getOMSSLConfigFromPipelineServiceClient(VerifySSL verifySSL, Object sslConfig) {
    return switch (verifySSL) {
      case NO_SSL, IGNORE -> null;
      case VALIDATE -> JsonUtils.convertValue(sslConfig, ValidateSSLClientConfig.class);
    };
  }
}
