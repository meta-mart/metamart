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

package org.metamart.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.health.conf.HealthConfiguration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import java.util.LinkedHashMap;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.metamart.schema.api.configuration.dataQuality.DataQualityConfiguration;
import org.metamart.schema.api.configuration.events.EventHandlerConfiguration;
import org.metamart.schema.api.configuration.pipelineServiceClient.PipelineServiceClientConfiguration;
import org.metamart.schema.api.fernet.FernetConfiguration;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.schema.api.security.jwt.JWTTokenConfiguration;
import org.metamart.schema.configuration.LimitsConfiguration;
import org.metamart.schema.email.SmtpSettings;
import org.metamart.schema.security.secrets.SecretsManagerConfiguration;
import org.metamart.schema.service.configuration.elasticsearch.ElasticSearchConfiguration;
import org.metamart.service.config.OMWebConfiguration;
import org.metamart.service.migration.MigrationConfiguration;
import org.metamart.service.monitoring.EventMonitorConfiguration;

@Getter
@Setter
public class MetaMartApplicationConfig extends Configuration {
  @JsonProperty("database")
  @NotNull
  @Valid
  private DataSourceFactory dataSourceFactory;

  @JsonProperty("swagger")
  private SwaggerBundleConfiguration swaggerBundleConfig;

  @JsonProperty("authorizerConfiguration")
  private AuthorizerConfiguration authorizerConfiguration;

  @JsonProperty("authenticationConfiguration")
  private AuthenticationConfiguration authenticationConfiguration;

  @JsonProperty("jwtTokenConfiguration")
  private JWTTokenConfiguration jwtTokenConfiguration;

  @JsonProperty("elasticsearch")
  private ElasticSearchConfiguration elasticSearchConfiguration;

  @JsonProperty("eventHandlerConfiguration")
  private EventHandlerConfiguration eventHandlerConfiguration;

  @JsonProperty("pipelineServiceClientConfiguration")
  private PipelineServiceClientConfiguration pipelineServiceClientConfiguration;

  private static final String CERTIFICATE_PATH = "certificatePath";

  public PipelineServiceClientConfiguration getPipelineServiceClientConfiguration() {

    LinkedHashMap<String, String> temporarySSLConfig =
        (LinkedHashMap<String, String>) pipelineServiceClientConfiguration.getSslConfig();
    if (temporarySSLConfig != null && temporarySSLConfig.containsKey(CERTIFICATE_PATH)) {
      temporarySSLConfig.put("caCertificate", temporarySSLConfig.get(CERTIFICATE_PATH));
      temporarySSLConfig.remove(CERTIFICATE_PATH);
    }
    pipelineServiceClientConfiguration.setSslConfig(temporarySSLConfig);
    return pipelineServiceClientConfiguration;
  }

  @JsonProperty("migrationConfiguration")
  @NotNull
  private MigrationConfiguration migrationConfiguration;

  @JsonProperty("fernetConfiguration")
  private FernetConfiguration fernetConfiguration;

  @JsonProperty("health")
  @NotNull
  @Valid
  private HealthConfiguration healthConfiguration = new HealthConfiguration();

  @JsonProperty("secretsManagerConfiguration")
  private SecretsManagerConfiguration secretsManagerConfiguration;

  @JsonProperty("eventMonitoringConfiguration")
  private EventMonitorConfiguration eventMonitorConfiguration;

  @JsonProperty("clusterName")
  private String clusterName;

  @JsonProperty("email")
  private SmtpSettings smtpSettings;

  @Valid
  @NotNull
  @JsonProperty("web")
  private OMWebConfiguration webConfiguration = new OMWebConfiguration();

  @JsonProperty("dataQualityConfiguration")
  private DataQualityConfiguration dataQualityConfiguration;

  @JsonProperty("limits")
  private LimitsConfiguration limitsConfiguration;

  @Override
  public String toString() {
    return "catalogConfig{"
        + ", dataSourceFactory="
        + dataSourceFactory
        + ", swaggerBundleConfig="
        + swaggerBundleConfig
        + ", authorizerConfiguration="
        + authorizerConfiguration
        + '}';
  }
}
