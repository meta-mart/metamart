package org.metamart.service.jdbi3;

import static org.metamart.schema.type.EventType.ENTITY_CREATED;
import static org.metamart.schema.type.EventType.ENTITY_DELETED;
import static org.metamart.schema.type.EventType.ENTITY_UPDATED;

import com.slack.api.bolt.model.builtin.DefaultBot;
import com.slack.api.bolt.model.builtin.DefaultInstaller;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import javax.json.JsonPatch;
import javax.json.JsonValue;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.metamart.api.configuration.UiThemePreference;
import org.metamart.schema.email.SmtpSettings;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineServiceClientResponse;
import org.metamart.schema.security.client.MetaMartJWTClientConfig;
import org.metamart.schema.service.configuration.slackApp.SlackAppConfiguration;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.schema.settings.Settings;
import org.metamart.schema.settings.SettingsType;
import org.metamart.schema.system.StepValidation;
import org.metamart.schema.system.ValidationResponse;
import org.metamart.schema.util.EntitiesCount;
import org.metamart.schema.util.ServicesCount;
import org.metamart.sdk.PipelineServiceClientInterface;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.exception.CustomExceptionMessage;
import org.metamart.service.fernet.Fernet;
import org.metamart.service.jdbi3.CollectionDAO.SystemDAO;
import org.metamart.service.migration.MigrationValidationClient;
import org.metamart.service.resources.settings.SettingsCache;
import org.metamart.service.search.SearchRepository;
import org.metamart.service.secrets.SecretsManager;
import org.metamart.service.secrets.SecretsManagerFactory;
import org.metamart.service.security.JwtFilter;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.MetaMartConnectionBuilder;
import org.metamart.service.util.RestUtil;
import org.metamart.service.util.ResultList;

@Slf4j
@Repository
public class SystemRepository {
  private static final String FAILED_TO_UPDATE_SETTINGS = "Failed to Update Settings";
  public static final String INTERNAL_SERVER_ERROR_WITH_REASON = "Internal Server Error. Reason :";
  private final SystemDAO dao;
  private final MigrationValidationClient migrationValidationClient;

  private enum ValidationStepDescription {
    DATABASE("Validate that we can properly run a query against the configured database."),
    SEARCH("Validate that the search client is available."),
    PIPELINE_SERVICE_CLIENT("Validate that the pipeline service client is available."),
    JWT_TOKEN("Validate that the ingestion-bot JWT token can be properly decoded."),
    MIGRATION("Validate that all the necessary migrations have been properly executed.");

    public final String key;

    ValidationStepDescription(String param) {
      this.key = param;
    }
  }

  private static final String INDEX_NAME = "table_search_index";

  public SystemRepository() {
    this.dao = Entity.getCollectionDAO().systemDAO();
    Entity.setSystemRepository(this);
    migrationValidationClient = MigrationValidationClient.getInstance();
  }

  public EntitiesCount getAllEntitiesCount(ListFilter filter) {
    return dao.getAggregatedEntitiesCount(filter.getCondition());
  }

  public ServicesCount getAllServicesCount(ListFilter filter) {
    return dao.getAggregatedServicesCount(filter.getCondition());
  }

  public ResultList<Settings> listAllConfigs() {
    List<Settings> settingsList = null;
    try {
      settingsList = dao.getAllConfig();
    } catch (Exception ex) {
      LOG.error("Error while trying fetch all Settings " + ex.getMessage());
    }
    int count = 0;
    if (settingsList != null) {
      count = settingsList.size();
    }
    return new ResultList<>(settingsList, null, null, count);
  }

  public Settings getConfigWithKey(String key) {
    try {
      Settings fetchedSettings = dao.getConfigWithKey(key);
      if (fetchedSettings == null) {
        return null;
      }

      return fetchedSettings;
    } catch (Exception ex) {
      LOG.error("Error while trying fetch Settings ", ex);
    }
    return null;
  }

  public Settings getEmailConfigInternal() {
    try {
      Settings setting = dao.getConfigWithKey(SettingsType.EMAIL_CONFIGURATION.value());
      SmtpSettings emailConfig =
          SystemRepository.decryptEmailSetting((SmtpSettings) setting.getConfigValue());
      setting.setConfigValue(emailConfig);
      return setting;
    } catch (Exception ex) {
      LOG.error("Error while trying fetch EMAIL Settings " + ex.getMessage());
    }
    return null;
  }

  public Settings getSlackApplicationConfigInternal() {
    try {
      Settings setting = dao.getConfigWithKey(SettingsType.SLACK_APP_CONFIGURATION.value());
      SlackAppConfiguration slackAppConfiguration =
          SystemRepository.decryptSlackAppSetting((String) setting.getConfigValue());
      setting.setConfigValue(slackAppConfiguration);
      return setting;
    } catch (Exception ex) {
      LOG.error("Error while trying fetch Slack Settings " + ex.getMessage());
    }
    return null;
  }

  @Transaction
  public Response createOrUpdate(Settings setting) {
    Settings oldValue = getConfigWithKey(setting.getConfigType().toString());

    if (oldValue != null && oldValue.getConfigType().equals(SettingsType.EMAIL_CONFIGURATION)) {
      SmtpSettings configValue =
          JsonUtils.convertValue(oldValue.getConfigValue(), SmtpSettings.class);
      if (configValue != null) {
        SmtpSettings.Templates templates = configValue.getTemplates();
        SmtpSettings newConfigValue =
            JsonUtils.convertValue(setting.getConfigValue(), SmtpSettings.class);
        if (newConfigValue != null) {
          newConfigValue.setTemplates(templates);
          setting.setConfigValue(newConfigValue);
        }
      }
    }

    try {
      updateSetting(setting);
    } catch (Exception ex) {
      LOG.error(FAILED_TO_UPDATE_SETTINGS + ex.getMessage());
      return Response.status(500, INTERNAL_SERVER_ERROR_WITH_REASON + ex.getMessage()).build();
    }
    if (oldValue == null) {
      return (new RestUtil.PutResponse<>(Response.Status.CREATED, setting, ENTITY_CREATED))
          .toResponse();
    } else {
      return (new RestUtil.PutResponse<>(Response.Status.OK, setting, ENTITY_UPDATED)).toResponse();
    }
  }

  public Response createNewSetting(Settings setting) {
    try {
      updateSetting(setting);
    } catch (Exception ex) {
      LOG.error(FAILED_TO_UPDATE_SETTINGS + ex.getMessage());
      return Response.status(500, INTERNAL_SERVER_ERROR_WITH_REASON + ex.getMessage()).build();
    }
    return (new RestUtil.PutResponse<>(Response.Status.CREATED, setting, ENTITY_CREATED))
        .toResponse();
  }

  @SuppressWarnings("unused")
  public Response deleteSettings(SettingsType type) {
    Settings oldValue = getConfigWithKey(type.toString());
    dao.delete(type.value());
    return (new RestUtil.DeleteResponse<>(oldValue, ENTITY_DELETED)).toResponse();
  }

  public Response patchSetting(String settingName, JsonPatch patch) {
    Settings original = getConfigWithKey(settingName);
    // Apply JSON patch to the original entity to get the updated entity
    JsonValue updated = JsonUtils.applyPatch(original.getConfigValue(), patch);
    original.setConfigValue(updated);
    try {
      updateSetting(original);
    } catch (Exception ex) {
      LOG.error(FAILED_TO_UPDATE_SETTINGS + ex.getMessage());
      return Response.status(500, INTERNAL_SERVER_ERROR_WITH_REASON + ex.getMessage()).build();
    }
    return (new RestUtil.PutResponse<>(Response.Status.OK, original, ENTITY_UPDATED)).toResponse();
  }

  public void updateSetting(Settings setting) {
    try {
      if (setting.getConfigType() == SettingsType.EMAIL_CONFIGURATION) {
        SmtpSettings emailConfig =
            JsonUtils.convertValue(setting.getConfigValue(), SmtpSettings.class);
        setting.setConfigValue(encryptEmailSetting(emailConfig));
      } else if (setting.getConfigType() == SettingsType.SLACK_APP_CONFIGURATION) {
        SlackAppConfiguration appConfiguration =
            JsonUtils.convertValue(setting.getConfigValue(), SlackAppConfiguration.class);
        setting.setConfigValue(encryptSlackAppSetting(appConfiguration));
      } else if (setting.getConfigType() == SettingsType.SLACK_BOT) {
        DefaultBot appConfiguration =
            JsonUtils.convertValue(setting.getConfigValue(), DefaultBot.class);
        setting.setConfigValue(encryptSlackDefaultBotSetting(appConfiguration));
      } else if (setting.getConfigType() == SettingsType.SLACK_INSTALLER) {
        DefaultInstaller appConfiguration =
            JsonUtils.convertValue(setting.getConfigValue(), DefaultInstaller.class);
        setting.setConfigValue(encryptSlackDefaultInstallerSetting(appConfiguration));
      } else if (setting.getConfigType() == SettingsType.SLACK_STATE) {
        String slackState = JsonUtils.convertValue(setting.getConfigValue(), String.class);
        setting.setConfigValue(encryptSlackStateSetting(slackState));
      } else if (setting.getConfigType() == SettingsType.CUSTOM_UI_THEME_PREFERENCE) {
        JsonUtils.validateJsonSchema(setting.getConfigValue(), UiThemePreference.class);
      }
      dao.insertSettings(
          setting.getConfigType().toString(), JsonUtils.pojoToJson(setting.getConfigValue()));
      // Invalidate Cache
      SettingsCache.invalidateSettings(setting.getConfigType().value());
    } catch (Exception ex) {
      LOG.error("Failing in Updating Setting.", ex);
      throw new CustomExceptionMessage(
          Response.Status.INTERNAL_SERVER_ERROR,
          "FAILED_TO_UPDATE_SLACK_OR_EMAIL",
          ex.getMessage());
    }
  }

  public Settings getSlackbotConfigInternal() {
    try {
      Settings setting = dao.getConfigWithKey(SettingsType.SLACK_BOT.value());
      DefaultBot slackBotConfiguration =
          SystemRepository.decryptSlackDefaultBotSetting((String) setting.getConfigValue());
      setting.setConfigValue(slackBotConfiguration);
      return setting;
    } catch (Exception ex) {
      LOG.error("Error while trying fetch Slack bot Settings " + ex.getMessage());
    }
    return null;
  }

  public Settings getSlackInstallerConfigInternal() {
    try {
      Settings setting = dao.getConfigWithKey(SettingsType.SLACK_INSTALLER.value());
      DefaultInstaller slackInstallerConfiguration =
          SystemRepository.decryptSlackDefaultInstallerSetting((String) setting.getConfigValue());
      setting.setConfigValue(slackInstallerConfiguration);
      return setting;
    } catch (Exception ex) {
      LOG.error("Error while trying to fetch slack installer setting " + ex.getMessage());
    }
    return null;
  }

  public Settings getSlackStateConfigInternal() {
    try {
      Settings setting = dao.getConfigWithKey(SettingsType.SLACK_STATE.value());
      String slackStateConfiguration =
          SystemRepository.decryptSlackStateSetting((String) setting.getConfigValue());
      setting.setConfigValue(slackStateConfiguration);
      return setting;
    } catch (Exception ex) {
      LOG.error("Error while trying to fetch slack state setting " + ex.getMessage());
    }
    return null;
  }

  @SneakyThrows
  public static String encryptSlackDefaultBotSetting(DefaultBot decryptedSetting) {
    String json = JsonUtils.pojoToJson(decryptedSetting);
    if (Fernet.getInstance().isKeyDefined()) {
      return Fernet.getInstance().encryptIfApplies(json);
    }
    return json;
  }

  @SneakyThrows
  public static DefaultBot decryptSlackDefaultBotSetting(String encryptedSetting) {
    if (Fernet.getInstance().isKeyDefined()) {
      encryptedSetting = Fernet.getInstance().decryptIfApplies(encryptedSetting);
    }
    return JsonUtils.readValue(encryptedSetting, DefaultBot.class);
  }

  @SneakyThrows
  public static String encryptSlackDefaultInstallerSetting(DefaultInstaller decryptedSetting) {
    String json = JsonUtils.pojoToJson(decryptedSetting);
    if (Fernet.getInstance().isKeyDefined()) {
      return Fernet.getInstance().encryptIfApplies(json);
    }
    return json;
  }

  @SneakyThrows
  public static DefaultInstaller decryptSlackDefaultInstallerSetting(String encryptedSetting) {
    if (Fernet.getInstance().isKeyDefined()) {
      encryptedSetting = Fernet.getInstance().decryptIfApplies(encryptedSetting);
    }
    return JsonUtils.readValue(encryptedSetting, DefaultInstaller.class);
  }

  @SneakyThrows
  public static String encryptSlackStateSetting(String decryptedSetting) {
    String json = JsonUtils.pojoToJson(decryptedSetting);
    if (Fernet.getInstance().isKeyDefined()) {
      return Fernet.getInstance().encryptIfApplies(json);
    }
    return json;
  }

  @SneakyThrows
  public static String decryptSlackStateSetting(String encryptedSetting) {
    if (Fernet.getInstance().isKeyDefined()) {
      encryptedSetting = Fernet.getInstance().decryptIfApplies(encryptedSetting);
    }
    return JsonUtils.readValue(encryptedSetting, String.class);
  }

  public static SmtpSettings encryptEmailSetting(SmtpSettings decryptedSetting) {
    if (Fernet.getInstance().isKeyDefined()) {
      String encryptedPwd = Fernet.getInstance().encryptIfApplies(decryptedSetting.getPassword());
      return decryptedSetting.withPassword(encryptedPwd);
    }
    return decryptedSetting;
  }

  public static SmtpSettings decryptEmailSetting(SmtpSettings encryptedSetting) {
    if (Fernet.getInstance().isKeyDefined() && Fernet.isTokenized(encryptedSetting.getPassword())) {
      String decryptedPassword = Fernet.getInstance().decrypt(encryptedSetting.getPassword());
      return encryptedSetting.withPassword(decryptedPassword);
    }
    return encryptedSetting;
  }

  @SneakyThrows
  public static String encryptSlackAppSetting(SlackAppConfiguration decryptedSetting) {
    String json = JsonUtils.pojoToJson(decryptedSetting);
    if (Fernet.getInstance().isKeyDefined()) {
      return Fernet.getInstance().encryptIfApplies(json);
    }
    return json;
  }

  @SneakyThrows
  public static SlackAppConfiguration decryptSlackAppSetting(String encryptedSetting) {
    if (Fernet.getInstance().isKeyDefined()) {
      encryptedSetting = Fernet.getInstance().decryptIfApplies(encryptedSetting);
    }
    return JsonUtils.readValue(encryptedSetting, SlackAppConfiguration.class);
  }

  public ValidationResponse validateSystem(
      MetaMartApplicationConfig applicationConfig,
      PipelineServiceClientInterface pipelineServiceClient,
      JwtFilter jwtFilter) {
    ValidationResponse validation = new ValidationResponse();

    validation.setDatabase(getDatabaseValidation(applicationConfig));
    validation.setSearchInstance(getSearchValidation(applicationConfig));
    validation.setPipelineServiceClient(
        getPipelineServiceClientValidation(applicationConfig, pipelineServiceClient));
    validation.setJwks(getJWKsValidation(applicationConfig, jwtFilter));
    validation.setMigrations(getMigrationValidation(migrationValidationClient));

    return validation;
  }

  private StepValidation getDatabaseValidation(MetaMartApplicationConfig applicationConfig) {
    try {
      dao.testConnection();
      return new StepValidation()
          .withDescription(ValidationStepDescription.DATABASE.key)
          .withPassed(Boolean.TRUE)
          .withMessage(
              String.format("Connected to %s", applicationConfig.getDataSourceFactory().getUrl()));
    } catch (Exception exc) {
      return new StepValidation()
          .withDescription(ValidationStepDescription.DATABASE.key)
          .withPassed(Boolean.FALSE)
          .withMessage(exc.getMessage());
    }
  }

  private StepValidation getSearchValidation(MetaMartApplicationConfig applicationConfig) {
    SearchRepository searchRepository = Entity.getSearchRepository();
    if (Boolean.TRUE.equals(searchRepository.getSearchClient().isClientAvailable())
        && searchRepository
            .getSearchClient()
            .indexExists(Entity.getSearchRepository().getIndexOrAliasName(INDEX_NAME))) {
      return new StepValidation()
          .withDescription(ValidationStepDescription.SEARCH.key)
          .withPassed(Boolean.TRUE)
          .withMessage(
              String.format(
                  "Connected to %s", applicationConfig.getElasticSearchConfiguration().getHost()));
    } else {
      return new StepValidation()
          .withDescription(ValidationStepDescription.SEARCH.key)
          .withPassed(Boolean.FALSE)
          .withMessage("Search instance is not reachable or available");
    }
  }

  private StepValidation getPipelineServiceClientValidation(
      MetaMartApplicationConfig applicationConfig,
      PipelineServiceClientInterface pipelineServiceClient) {
    PipelineServiceClientResponse pipelineResponse = pipelineServiceClient.getServiceStatus();
    if (pipelineResponse.getCode() == 200) {
      return new StepValidation()
          .withDescription(ValidationStepDescription.PIPELINE_SERVICE_CLIENT.key)
          .withPassed(Boolean.TRUE)
          .withMessage(
              String.format(
                  "%s is available at %s",
                  pipelineServiceClient.getPlatform(),
                  applicationConfig.getPipelineServiceClientConfiguration().getApiEndpoint()));
    } else {
      return new StepValidation()
          .withDescription(ValidationStepDescription.PIPELINE_SERVICE_CLIENT.key)
          .withPassed(Boolean.FALSE)
          .withMessage(pipelineResponse.getReason());
    }
  }

  private StepValidation getJWKsValidation(
      MetaMartApplicationConfig applicationConfig, JwtFilter jwtFilter) {
    SecretsManager secretsManager = SecretsManagerFactory.getSecretsManager();
    MetaMartConnection metaMartServerConnection =
        new MetaMartConnectionBuilder(applicationConfig).build();
    MetaMartJWTClientConfig realJWTConfig =
        secretsManager.decryptJWTConfig(metaMartServerConnection.getSecurityConfig());
    try {
      jwtFilter.validateJwtAndGetClaims(realJWTConfig.getJwtToken());
      return new StepValidation()
          .withDescription(ValidationStepDescription.JWT_TOKEN.key)
          .withPassed(Boolean.TRUE)
          .withMessage("Ingestion Bot token has been validated");
    } catch (Exception e) {
      return new StepValidation()
          .withDescription(ValidationStepDescription.JWT_TOKEN.key)
          .withPassed(Boolean.FALSE)
          .withMessage(e.getMessage());
    }
  }

  private StepValidation getMigrationValidation(
      MigrationValidationClient migrationValidationClient) {
    List<String> currentVersions = migrationValidationClient.getCurrentVersions();
    // Compare regardless of ordering
    if (new HashSet<>(currentVersions)
        .equals(new HashSet<>(migrationValidationClient.getExpectedMigrationList()))) {
      return new StepValidation()
          .withDescription(ValidationStepDescription.MIGRATION.key)
          .withPassed(Boolean.TRUE)
          .withMessage(String.format("Executed migrations: %s", currentVersions));
    }
    List<String> missingVersions =
        new ArrayList<>(migrationValidationClient.getExpectedMigrationList());
    missingVersions.removeAll(currentVersions);

    List<String> unexpectedVersions = new ArrayList<>(currentVersions);
    unexpectedVersions.removeAll(migrationValidationClient.getExpectedMigrationList());

    return new StepValidation()
        .withDescription(ValidationStepDescription.MIGRATION.key)
        .withPassed(Boolean.FALSE)
        .withMessage(
            String.format(
                "Missing migrations that were not executed %s. Unexpected executed migrations %s",
                missingVersions, unexpectedVersions));
  }
}
