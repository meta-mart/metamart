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

package org.metamart.service.resources.settings;

import static org.metamart.schema.settings.SettingsType.CUSTOM_UI_THEME_PREFERENCE;
import static org.metamart.schema.settings.SettingsType.EMAIL_CONFIGURATION;
import static org.metamart.schema.settings.SettingsType.LOGIN_CONFIGURATION;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.util.concurrent.TimeUnit;
import javax.annotation.CheckForNull;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.metamart.api.configuration.LogoConfiguration;
import org.metamart.api.configuration.ThemeConfiguration;
import org.metamart.api.configuration.UiThemePreference;
import org.metamart.schema.api.configuration.LoginConfiguration;
import org.metamart.schema.email.SmtpSettings;
import org.metamart.schema.settings.Settings;
import org.metamart.schema.settings.SettingsType;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.jdbi3.SystemRepository;
import org.metamart.service.util.JsonUtils;

@Slf4j
public class SettingsCache {
  private static volatile boolean initialized = false;
  protected static final LoadingCache<String, Settings> CACHE =
      CacheBuilder.newBuilder()
          .maximumSize(1000)
          .expireAfterWrite(3, TimeUnit.MINUTES)
          .build(new SettingsLoader());
  protected static SystemRepository systemRepository;

  private SettingsCache() {
    // Private constructor for singleton
  }

  // Expected to be called only once from the DefaultAuthorizer
  public static void initialize(MetaMartApplicationConfig config) {
    if (!initialized) {
      systemRepository = Entity.getSystemRepository();
      initialized = true;
      createDefaultConfiguration(config);
    }
  }

  private static void createDefaultConfiguration(MetaMartApplicationConfig applicationConfig) {
    // Initialise Email Setting
    Settings storedSettings = systemRepository.getConfigWithKey(EMAIL_CONFIGURATION.toString());
    if (storedSettings == null) {
      // Only in case a config doesn't exist in DB we insert it
      SmtpSettings emailConfig = applicationConfig.getSmtpSettings();
      Settings setting =
          new Settings().withConfigType(EMAIL_CONFIGURATION).withConfigValue(emailConfig);
      systemRepository.createNewSetting(setting);
    }

    // Initialise Theme Setting
    Settings storedCustomUiThemeConf =
        systemRepository.getConfigWithKey(CUSTOM_UI_THEME_PREFERENCE.toString());
    if (storedCustomUiThemeConf == null) {
      // Only in case a config doesn't exist in DB we insert it
      Settings setting =
          new Settings()
              .withConfigType(CUSTOM_UI_THEME_PREFERENCE)
              .withConfigValue(
                  new UiThemePreference()
                      .withCustomLogoConfig(
                          new LogoConfiguration()
                              .withCustomLogoUrlPath("")
                              .withCustomFaviconUrlPath("")
                              .withCustomMonogramUrlPath(""))
                      .withCustomTheme(
                          new ThemeConfiguration()
                              .withPrimaryColor("")
                              .withSuccessColor("")
                              .withErrorColor("")
                              .withWarningColor("")
                              .withInfoColor("")));
      systemRepository.createNewSetting(setting);
    }

    // Initialise Login Configuration
    // Initialise Logo Setting
    Settings storedLoginConf = systemRepository.getConfigWithKey(LOGIN_CONFIGURATION.toString());
    if (storedLoginConf == null) {
      // Only in case a config doesn't exist in DB we insert it
      Settings setting =
          new Settings()
              .withConfigType(LOGIN_CONFIGURATION)
              .withConfigValue(
                  new LoginConfiguration()
                      .withMaxLoginFailAttempts(3)
                      .withAccessBlockTime(600)
                      .withJwtTokenExpiryTime(3600));
      systemRepository.createNewSetting(setting);
    }
  }

  public static <T> T getSetting(SettingsType settingName, Class<T> clazz) {
    try {
      String json = JsonUtils.pojoToJson(CACHE.get(settingName.toString()).getConfigValue());
      return JsonUtils.readValue(json, clazz);
    } catch (Exception ex) {
      LOG.error("Failed to fetch Settings . Setting {}", settingName, ex);
      throw new EntityNotFoundException("Setting not found");
    }
  }

  public static void cleanUp() {
    CACHE.invalidateAll();
    initialized = false;
  }

  public static void invalidateSettings(String settingsName) {
    try {
      CACHE.invalidate(settingsName);
    } catch (Exception ex) {
      LOG.error("Failed to invalidate cache for settings {}", settingsName, ex);
    }
  }

  static class SettingsLoader extends CacheLoader<String, Settings> {
    @Override
    public @NonNull Settings load(@CheckForNull String settingsName) {
      Settings fetchedSettings;
      switch (SettingsType.fromValue(settingsName)) {
        case EMAIL_CONFIGURATION -> {
          fetchedSettings = systemRepository.getEmailConfigInternal();
          LOG.info("Loaded Email Setting");
        }
        case SLACK_APP_CONFIGURATION -> {
          // Only if available
          fetchedSettings = systemRepository.getSlackApplicationConfigInternal();
          LOG.info("Loaded Slack Application Configuration");
        }
        case SLACK_BOT -> {
          // Only if available
          fetchedSettings = systemRepository.getSlackbotConfigInternal();
          LOG.info("Loaded Slack Bot Configuration");
        }
        case SLACK_INSTALLER -> {
          // Only if available
          fetchedSettings = systemRepository.getSlackInstallerConfigInternal();
          LOG.info("Loaded Slack Installer Configuration");
        }
        case SLACK_STATE -> {
          // Only if available
          fetchedSettings = systemRepository.getSlackStateConfigInternal();
          LOG.info("Loaded Slack state Configuration");
        }
        default -> {
          fetchedSettings = systemRepository.getConfigWithKey(settingsName);
          LOG.info("Loaded Setting {}", fetchedSettings.getConfigType());
        }
      }
      return fetchedSettings;
    }
  }
}
