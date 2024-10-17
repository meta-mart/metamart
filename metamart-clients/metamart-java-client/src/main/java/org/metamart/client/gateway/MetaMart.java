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

package org.metamart.client.gateway;

import com.fasterxml.jackson.annotation.JsonInclude;
import feign.Feign;
import feign.form.FormEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import feign.slf4j.Slf4jLogger;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.metamart.client.ApiClient;
import org.metamart.client.api.SystemApi;
import org.metamart.client.security.factory.AuthenticationProviderFactory;
import org.metamart.schema.api.MetaMartServerVersion;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.schema.utils.VersionUtils;

@Slf4j
public class MetaMart {
  private static final MetaMartServerVersion METAMART_VERSION_CLIENT;

  static {
    METAMART_VERSION_CLIENT = VersionUtils.getMetaMartServerVersion("/catalog/VERSION");
  }

  private ApiClient apiClient;
  private static final String REQUEST_INTERCEPTOR_KEY = "custom";

  public MetaMart(MetaMartConnection config) {
    initClient(config);
    validateVersion();
  }

  public MetaMart(MetaMartConnection config, boolean validateVersion) {
    initClient(config);
    if (validateVersion) validateVersion();
  }

  public void initClient(MetaMartConnection config) {
    apiClient = new ApiClient();
    Feign.Builder builder =
        Feign.builder()
            .encoder(new FormEncoder(new JacksonEncoder(apiClient.getObjectMapper())))
            .decoder(new JacksonDecoder(apiClient.getObjectMapper()))
            .logger(new Slf4jLogger())
            .client(new OkHttpClient());
    initClient(config, builder);
  }

  public void initClient(MetaMartConnection config, Feign.Builder builder) {
    if (Objects.isNull(apiClient)) {
      apiClient = new ApiClient();
    }

    apiClient.setFeignBuilder(builder);
    AuthenticationProviderFactory factory = new AuthenticationProviderFactory();
    apiClient.addAuthorization("oauth", factory.getAuthProvider(config));
    String basePath = config.getHostPort() + "/";
    apiClient.setBasePath(basePath);
    apiClient.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
  }

  public <T extends ApiClient.Api> T buildClient(Class<T> clientClass) {
    return apiClient.buildClient(clientClass);
  }

  public void validateVersion() {
    String[] clientVersion = getClientVersion();
    String[] serverVersion = getServerVersion();
    // MAJOR MINOR REVISION
    if (serverVersion[0].equals(clientVersion[0])
        && serverVersion[1].equals(clientVersion[1])
        && serverVersion[2].equals(clientVersion[2])) {
      LOG.debug("MetaMart Client Initialized successfully.");
    } else {
      LOG.error(
          "MetaMart Client Failed to be Initialized successfully. Version mismatch between CLient and Server issue");
    }
  }

  public String[] getServerVersion() {
    SystemApi api = apiClient.buildClient(SystemApi.class);
    org.metamart.client.model.MetaMartServerVersion serverVersion = api.getCatalogVersion();
    return VersionUtils.getVersionFromString(serverVersion.getVersion());
  }

  public String[] getClientVersion() {
    return VersionUtils.getVersionFromString(METAMART_VERSION_CLIENT.getVersion());
  }
}
