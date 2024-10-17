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

package org.metamart.service.resources.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.metamart.service.util.TestUtils.TEST_AUTH_HEADERS;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.IOException;
import javax.validation.Validator;
import javax.ws.rs.client.WebTarget;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.metamart.api.configuration.UiThemePreference;
import org.metamart.schema.api.configuration.LoginConfiguration;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.MetaMartApplicationTest;
import org.metamart.service.clients.pipeline.PipelineServiceAPIClientConfig;
import org.metamart.service.security.jwt.JWKSKey;
import org.metamart.service.security.jwt.JWKSResponse;
import org.metamart.service.util.TestUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ConfigResourceTest extends MetaMartApplicationTest {

  static MetaMartApplicationConfig config;

  @BeforeAll
  static void setup() throws IOException, ConfigurationException {
    // Get config object from test yaml file
    ObjectMapper objectMapper = Jackson.newObjectMapper();
    Validator validator = Validators.newValidator();
    YamlConfigurationFactory<MetaMartApplicationConfig> factory =
        new YamlConfigurationFactory<>(
            MetaMartApplicationConfig.class, validator, objectMapper, "dw");
    config = factory.build(new FileConfigurationSourceProvider(), CONFIG_PATH);
  }

  @Test
  void get_auth_configs_200_OK() throws IOException {
    WebTarget target = getConfigResource("auth");
    AuthenticationConfiguration auth =
        TestUtils.get(target, AuthenticationConfiguration.class, TEST_AUTH_HEADERS);
    assertEquals(config.getAuthenticationConfiguration().getProvider(), auth.getProvider());
    assertEquals(config.getAuthenticationConfiguration().getProviderName(), auth.getProviderName());
    assertEquals(config.getAuthenticationConfiguration().getAuthority(), auth.getAuthority());
    assertEquals(config.getAuthenticationConfiguration().getCallbackUrl(), auth.getCallbackUrl());
    assertEquals(
        config.getAuthenticationConfiguration().getJwtPrincipalClaims(),
        auth.getJwtPrincipalClaims());
    assertEquals(config.getAuthenticationConfiguration().getClientId(), auth.getClientId());
  }

  @Test
  void get_authorizer_configs_200_OK() throws IOException {
    WebTarget target = getConfigResource("authorizer");
    AuthorizerConfiguration auth =
        TestUtils.get(target, AuthorizerConfiguration.class, TEST_AUTH_HEADERS);
    assertEquals(config.getAuthorizerConfiguration().getClassName(), auth.getClassName());
    assertEquals(
        config.getAuthorizerConfiguration().getPrincipalDomain(), auth.getPrincipalDomain());
    assertEquals(
        config.getAuthorizerConfiguration().getAdminPrincipals(), auth.getAdminPrincipals());
    assertEquals(
        config.getAuthorizerConfiguration().getContainerRequestFilter(),
        auth.getContainerRequestFilter());
    assertEquals(
        config.getAuthorizerConfiguration().getEnableSecureSocketConnection(),
        auth.getEnableSecureSocketConnection());
    assertEquals(
        config.getAuthorizerConfiguration().getEnforcePrincipalDomain(),
        auth.getEnforcePrincipalDomain());
  }

  @Test
  void get_airflow_configs_200_OK() throws IOException {
    WebTarget target = getConfigResource("pipeline-service-client");
    PipelineServiceAPIClientConfig auth =
        TestUtils.get(target, PipelineServiceAPIClientConfig.class, TEST_AUTH_HEADERS);
    assertEquals(
        config.getPipelineServiceClientConfiguration().getApiEndpoint(), auth.getApiEndpoint());
  }

  @Test
  void get_Custom_Ui_Theme_Preference_200_OK() throws IOException {
    // Test Against Default Values
    WebTarget target = getConfigResource("customUiThemePreference");
    UiThemePreference uiThemePreference =
        TestUtils.get(target, UiThemePreference.class, TEST_AUTH_HEADERS);

    assertEquals("", uiThemePreference.getCustomTheme().getPrimaryColor());
    assertEquals("", uiThemePreference.getCustomTheme().getSuccessColor());
    assertEquals("", uiThemePreference.getCustomTheme().getErrorColor());
    assertEquals("", uiThemePreference.getCustomTheme().getWarningColor());
    assertEquals("", uiThemePreference.getCustomTheme().getInfoColor());
    assertEquals("", uiThemePreference.getCustomLogoConfig().getCustomLogoUrlPath());
    assertEquals("", uiThemePreference.getCustomLogoConfig().getCustomMonogramUrlPath());
  }

  @Test
  void get_Login_Configuration_200_OK() throws IOException {
    // Test Against Default Values
    WebTarget target = getConfigResource("loginConfig");
    LoginConfiguration loginConfiguration =
        TestUtils.get(target, LoginConfiguration.class, TEST_AUTH_HEADERS);
    assertEquals(3, loginConfiguration.getMaxLoginFailAttempts());
    assertEquals(600, loginConfiguration.getAccessBlockTime());
    assertEquals(3600, loginConfiguration.getJwtTokenExpiryTime());
  }

  @Test
  void get_jwks_configs_200_OK() throws IOException {
    WebTarget target = getConfigResource("jwks");
    JWKSResponse auth = TestUtils.get(target, JWKSResponse.class, TEST_AUTH_HEADERS);
    assertNotNull(auth);
    assertEquals(1, auth.getJwsKeys().size());
    JWKSKey jwksKey = auth.getJwsKeys().get(0);
    assertEquals("RS256", jwksKey.getAlg());
    assertEquals("sig", jwksKey.getUse());
    assertEquals("RSA", jwksKey.getKty());
    assertNotNull(jwksKey.getN());
    assertNotNull(jwksKey.getE());
  }
}
