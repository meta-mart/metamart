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

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.health.conf.HealthConfiguration;
import io.dropwizard.health.core.HealthCheckBundle;
import io.dropwizard.jdbi3.JdbiFactory;
import io.dropwizard.jersey.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.errors.LoggingExceptionMapper;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.federecio.dropwizard.swagger.SwaggerBundle;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;
import io.socket.engineio.server.EngineIoServerOptions;
import io.socket.engineio.server.JettyWebSocketHandler;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.EnumSet;
import java.util.Optional;
import javax.naming.ConfigurationException;
import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Response;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.http.pathmap.ServletPathSpec;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.server.NativeWebSocketServletContainerInitializer;
import org.eclipse.jetty.websocket.server.WebSocketUpgradeFilter;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ServerProperties;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.SqlStatements;
import org.jdbi.v3.sqlobject.SqlObjects;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.api.security.AuthorizerConfiguration;
import org.metamart.schema.api.security.ClientType;
import org.metamart.schema.configuration.LimitsConfiguration;
import org.metamart.schema.service.configuration.elasticsearch.ElasticSearchConfiguration;
import org.metamart.schema.services.connections.metadata.AuthProvider;
import org.metamart.service.apps.ApplicationHandler;
import org.metamart.service.apps.scheduler.AppScheduler;
import org.metamart.service.config.OMWebBundle;
import org.metamart.service.config.OMWebConfiguration;
import org.metamart.service.events.EventFilter;
import org.metamart.service.events.EventPubSub;
import org.metamart.service.events.scheduled.EventSubscriptionScheduler;
import org.metamart.service.events.scheduled.PipelineServiceStatusJobHandler;
import org.metamart.service.exception.CatalogGenericExceptionMapper;
import org.metamart.service.exception.ConstraintViolationExceptionMapper;
import org.metamart.service.exception.JsonMappingExceptionMapper;
import org.metamart.service.exception.OMErrorPageHandler;
import org.metamart.service.fernet.Fernet;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.jdbi3.EntityRepository;
import org.metamart.service.jdbi3.MigrationDAO;
import org.metamart.service.jdbi3.locator.ConnectionAwareAnnotationSqlLocator;
import org.metamart.service.jdbi3.locator.ConnectionType;
import org.metamart.service.limits.DefaultLimits;
import org.metamart.service.limits.Limits;
import org.metamart.service.migration.Migration;
import org.metamart.service.migration.MigrationValidationClient;
import org.metamart.service.migration.api.MigrationWorkflow;
import org.metamart.service.monitoring.EventMonitor;
import org.metamart.service.monitoring.EventMonitorConfiguration;
import org.metamart.service.monitoring.EventMonitorFactory;
import org.metamart.service.monitoring.EventMonitorPublisher;
import org.metamart.service.resources.CollectionRegistry;
import org.metamart.service.resources.databases.DatasourceConfig;
import org.metamart.service.resources.settings.SettingsCache;
import org.metamart.service.search.SearchRepository;
import org.metamart.service.secrets.SecretsManagerFactory;
import org.metamart.service.secrets.masker.EntityMaskerFactory;
import org.metamart.service.security.AuthCallbackServlet;
import org.metamart.service.security.AuthLoginServlet;
import org.metamart.service.security.AuthLogoutServlet;
import org.metamart.service.security.AuthRefreshServlet;
import org.metamart.service.security.AuthenticationCodeFlowHandler;
import org.metamart.service.security.Authorizer;
import org.metamart.service.security.NoopAuthorizer;
import org.metamart.service.security.NoopFilter;
import org.metamart.service.security.auth.AuthenticatorHandler;
import org.metamart.service.security.auth.BasicAuthenticator;
import org.metamart.service.security.auth.LdapAuthenticator;
import org.metamart.service.security.auth.NoopAuthenticator;
import org.metamart.service.security.jwt.JWTTokenGenerator;
import org.metamart.service.security.saml.OMMicrometerHttpFilter;
import org.metamart.service.security.saml.SamlAssertionConsumerServlet;
import org.metamart.service.security.saml.SamlLoginServlet;
import org.metamart.service.security.saml.SamlLogoutServlet;
import org.metamart.service.security.saml.SamlMetadataServlet;
import org.metamart.service.security.saml.SamlSettingsHolder;
import org.metamart.service.security.saml.SamlTokenRefreshServlet;
import org.metamart.service.socket.FeedServlet;
import org.metamart.service.socket.MetaMartAssetServlet;
import org.metamart.service.socket.SocketAddressFilter;
import org.metamart.service.socket.WebSocketManager;
import org.metamart.service.util.MicrometerBundleSingleton;
import org.metamart.service.util.incidentSeverityClassifier.IncidentSeverityClassifierInterface;
import org.metamart.service.util.jdbi.DatabaseAuthenticationProviderFactory;
import org.metamart.service.util.jdbi.OMSqlLogger;
import org.pac4j.core.util.CommonHelper;
import org.quartz.SchedulerException;

/** Main catalog application */
@Slf4j
public class MetaMartApplication extends Application<MetaMartApplicationConfig> {
  private Authorizer authorizer;
  private AuthenticatorHandler authenticatorHandler;
  private Limits limits;

  protected Jdbi jdbi;

  @Override
  public void run(MetaMartApplicationConfig catalogConfig, Environment environment)
      throws ClassNotFoundException,
          IllegalAccessException,
          InstantiationException,
          NoSuchMethodException,
          InvocationTargetException,
          IOException,
          ConfigurationException,
          CertificateException,
          KeyStoreException,
          NoSuchAlgorithmException {
    validateConfiguration(catalogConfig);

    // Instantiate incident severity classifier
    IncidentSeverityClassifierInterface.createInstance();

    // init for dataSourceFactory
    DatasourceConfig.initialize(catalogConfig.getDataSourceFactory().getDriverClass());

    // Initialize HTTP and JDBI timers
    MicrometerBundleSingleton.initLatencyEvents(catalogConfig);

    jdbi = createAndSetupJDBI(environment, catalogConfig.getDataSourceFactory());
    Entity.setCollectionDAO(getDao(jdbi));

    initializeSearchRepository(catalogConfig.getElasticSearchConfiguration());
    // Initialize the MigrationValidationClient, used in the Settings Repository
    MigrationValidationClient.initialize(jdbi.onDemand(MigrationDAO.class), catalogConfig);
    // as first step register all the repositories
    Entity.initializeRepositories(catalogConfig, jdbi);

    // Configure the Fernet instance
    Fernet.getInstance().setFernetKey(catalogConfig);

    // Init Settings Cache after repositories
    SettingsCache.initialize(catalogConfig);

    initializeWebsockets(catalogConfig, environment);

    // init Secret Manager
    SecretsManagerFactory.createSecretsManager(
        catalogConfig.getSecretsManagerConfiguration(), catalogConfig.getClusterName());

    // init Entity Masker
    EntityMaskerFactory.createEntityMasker();

    // Instantiate JWT Token Generator
    JWTTokenGenerator.getInstance().init(catalogConfig.getJwtTokenConfiguration());

    // Set the Database type for choosing correct queries from annotations
    jdbi.getConfig(SqlObjects.class)
        .setSqlLocator(
            new ConnectionAwareAnnotationSqlLocator(
                catalogConfig.getDataSourceFactory().getDriverClass()));

    // Validate flyway Migrations
    validateMigrations(jdbi, catalogConfig);

    // Register Authorizer
    registerAuthorizer(catalogConfig, environment);

    // Register Authenticator
    registerAuthenticator(catalogConfig);

    // Register Limits
    registerLimits(catalogConfig);

    // Unregister dropwizard default exception mappers
    ((DefaultServerFactory) catalogConfig.getServerFactory())
        .setRegisterDefaultExceptionMappers(false);
    environment.jersey().property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    environment.jersey().register(MultiPartFeature.class);

    // Exception Mappers
    registerExceptionMappers(environment);

    // Health Check
    registerHealthCheck(environment);

    // start event hub before registering publishers
    EventPubSub.start();

    ApplicationHandler.initialize(catalogConfig);
    registerResources(catalogConfig, environment, jdbi);

    // Register Event Handler
    registerEventFilter(catalogConfig, environment);
    environment.lifecycle().manage(new ManagedShutdown());
    // Register Event publishers
    registerEventPublisher(catalogConfig);

    // start authorizer after event publishers
    // authorizer creates admin/bot users, ES publisher should start before to index users created
    // by authorizer
    authorizer.init(catalogConfig);

    // authenticationHandler Handles auth related activities
    authenticatorHandler.init(catalogConfig);

    registerMicrometerFilter(environment, catalogConfig.getEventMonitorConfiguration());

    registerSamlServlets(catalogConfig, environment);

    // Asset Servlet Registration
    registerAssetServlet(catalogConfig.getWebConfiguration(), environment);

    // Handle Pipeline Service Client Status job
    PipelineServiceStatusJobHandler pipelineServiceStatusJobHandler =
        PipelineServiceStatusJobHandler.create(
            catalogConfig.getPipelineServiceClientConfiguration(), catalogConfig.getClusterName());
    pipelineServiceStatusJobHandler.addPipelineServiceStatusJob();

    // Register Auth Handlers
    registerAuthServlets(catalogConfig, environment);
  }

  private void registerAuthServlets(MetaMartApplicationConfig config, Environment environment) {
    if (config.getAuthenticationConfiguration() != null
        && config
            .getAuthenticationConfiguration()
            .getClientType()
            .equals(ClientType.CONFIDENTIAL)) {
      CommonHelper.assertNotNull(
          "OidcConfiguration", config.getAuthenticationConfiguration().getOidcConfiguration());

      // Set up a Session Manager
      MutableServletContextHandler contextHandler = environment.getApplicationContext();
      if (contextHandler.getSessionHandler() == null) {
        contextHandler.setSessionHandler(new SessionHandler());
      }

      AuthenticationCodeFlowHandler authenticationCodeFlowHandler =
          new AuthenticationCodeFlowHandler(
              config.getAuthenticationConfiguration(), config.getAuthorizerConfiguration());

      // Register Servlets
      ServletRegistration.Dynamic authLogin =
          environment
              .servlets()
              .addServlet("oauth_login", new AuthLoginServlet(authenticationCodeFlowHandler));
      authLogin.addMapping("/api/v1/auth/login");
      ServletRegistration.Dynamic authCallback =
          environment
              .servlets()
              .addServlet("auth_callback", new AuthCallbackServlet(authenticationCodeFlowHandler));
      authCallback.addMapping("/callback");

      ServletRegistration.Dynamic authLogout =
          environment
              .servlets()
              .addServlet("auth_logout", new AuthLogoutServlet(authenticationCodeFlowHandler));
      authLogout.addMapping("/api/v1/auth/logout");

      ServletRegistration.Dynamic refreshServlet =
          environment
              .servlets()
              .addServlet("auth_refresh", new AuthRefreshServlet(authenticationCodeFlowHandler));
      refreshServlet.addMapping("/api/v1/auth/refresh");
    }
  }

  protected void initializeSearchRepository(ElasticSearchConfiguration esConfig) {
    // initialize Search Repository, all repositories use SearchRepository this line should always
    // before initializing repository
    SearchRepository searchRepository = new SearchRepository(esConfig);
    Entity.setSearchRepository(searchRepository);
  }

  private void registerHealthCheck(Environment environment) {
    environment
        .healthChecks()
        .register("MetaMartServerHealthCheck", new MetaMartServerHealthCheck());
  }

  private void registerExceptionMappers(Environment environment) {
    environment.jersey().register(CatalogGenericExceptionMapper.class);
    // Override constraint violation mapper to catch Json validation errors
    environment.jersey().register(new ConstraintViolationExceptionMapper());
    // Restore dropwizard default exception mappers
    environment.jersey().register(new LoggingExceptionMapper<>() {});
    environment.jersey().register(new JsonProcessingExceptionMapper(true));
    environment.jersey().register(new EarlyEofExceptionMapper());
    environment.jersey().register(JsonMappingExceptionMapper.class);
  }

  private void registerMicrometerFilter(
      Environment environment, EventMonitorConfiguration eventMonitorConfiguration) {
    FilterRegistration.Dynamic micrometerFilter =
        environment.servlets().addFilter("OMMicrometerHttpFilter", new OMMicrometerHttpFilter());
    micrometerFilter.addMappingForUrlPatterns(
        EnumSet.allOf(DispatcherType.class), true, eventMonitorConfiguration.getPathPattern());
  }

  private void registerAssetServlet(OMWebConfiguration webConfiguration, Environment environment) {
    // Handle Asset Using Servlet
    MetaMartAssetServlet assetServlet =
        new MetaMartAssetServlet("/assets", "/", "index.html", webConfiguration);
    String pathPattern = "/" + '*';
    environment.servlets().addServlet("static", assetServlet).addMapping(pathPattern);
  }

  protected CollectionDAO getDao(Jdbi jdbi) {
    return jdbi.onDemand(CollectionDAO.class);
  }

  private void registerSamlServlets(
      MetaMartApplicationConfig catalogConfig, Environment environment)
      throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
    if (catalogConfig.getAuthenticationConfiguration() != null
        && catalogConfig.getAuthenticationConfiguration().getProvider().equals(AuthProvider.SAML)) {

      // Set up a Session Manager
      MutableServletContextHandler contextHandler = environment.getApplicationContext();
      if (contextHandler.getSessionHandler() == null) {
        contextHandler.setSessionHandler(new SessionHandler());
      }

      SamlSettingsHolder.getInstance().initDefaultSettings(catalogConfig);
      ServletRegistration.Dynamic samlRedirectServlet =
          environment.servlets().addServlet("saml_login", new SamlLoginServlet());
      samlRedirectServlet.addMapping("/api/v1/saml/login");
      ServletRegistration.Dynamic samlReceiverServlet =
          environment
              .servlets()
              .addServlet(
                  "saml_acs",
                  new SamlAssertionConsumerServlet(catalogConfig.getAuthorizerConfiguration()));
      samlReceiverServlet.addMapping("/api/v1/saml/acs");
      ServletRegistration.Dynamic samlMetadataServlet =
          environment.servlets().addServlet("saml_metadata", new SamlMetadataServlet());
      samlMetadataServlet.addMapping("/api/v1/saml/metadata");

      ServletRegistration.Dynamic samlRefreshServlet =
          environment.servlets().addServlet("saml_refresh_token", new SamlTokenRefreshServlet());
      samlRefreshServlet.addMapping("/api/v1/saml/refresh");

      ServletRegistration.Dynamic samlLogoutServlet =
          environment
              .servlets()
              .addServlet(
                  "saml_logout_token",
                  new SamlLogoutServlet(
                      catalogConfig.getAuthenticationConfiguration(),
                      catalogConfig.getAuthorizerConfiguration()));
      samlLogoutServlet.addMapping("/api/v1/saml/logout");
    }
  }

  private Jdbi createAndSetupJDBI(Environment environment, DataSourceFactory dbFactory) {
    // Check for db auth providers.
    DatabaseAuthenticationProviderFactory.get(dbFactory.getUrl())
        .ifPresent(
            databaseAuthenticationProvider -> {
              String token =
                  databaseAuthenticationProvider.authenticate(
                      dbFactory.getUrl(), dbFactory.getUser(), dbFactory.getPassword());
              dbFactory.setPassword(token);
            });

    Jdbi jdbiInstance = new JdbiFactory().build(environment, dbFactory, "database");
    jdbiInstance.setSqlLogger(new OMSqlLogger());
    // Set the Database type for choosing correct queries from annotations
    jdbiInstance
        .getConfig(SqlObjects.class)
        .setSqlLocator(new ConnectionAwareAnnotationSqlLocator(dbFactory.getDriverClass()));
    jdbiInstance.getConfig(SqlStatements.class).setUnusedBindingAllowed(true);

    return jdbiInstance;
  }

  @SneakyThrows
  @Override
  public void initialize(Bootstrap<MetaMartApplicationConfig> bootstrap) {
    bootstrap.setConfigurationSourceProvider(
        new SubstitutingSourceProvider(
            bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
    bootstrap.addBundle(
        new SwaggerBundle<>() {
          @Override
          protected SwaggerBundleConfiguration getSwaggerBundleConfiguration(
              MetaMartApplicationConfig catalogConfig) {
            return catalogConfig.getSwaggerBundleConfig();
          }
        });
    bootstrap.addBundle(
        new HealthCheckBundle<>() {
          @Override
          protected HealthConfiguration getHealthConfiguration(
              final MetaMartApplicationConfig configuration) {
            return configuration.getHealthConfiguration();
          }
        });
    bootstrap.addBundle(MicrometerBundleSingleton.getInstance());
    bootstrap.addBundle(
        new OMWebBundle<>() {
          @Override
          public OMWebConfiguration getWebConfiguration(
              final MetaMartApplicationConfig configuration) {
            return configuration.getWebConfiguration();
          }
        });
    super.initialize(bootstrap);
  }

  private void validateMigrations(Jdbi jdbi, MetaMartApplicationConfig conf)
      throws IOException {
    LOG.info("Validating Flyway migrations");
    Optional<String> lastMigrated = Migration.lastMigrated(jdbi);
    String maxMigration = Migration.lastMigrationFile(conf.getMigrationConfiguration());
    if (lastMigrated.isEmpty()) {
      throw new IllegalStateException(
          "Could not validate Flyway migrations in the database. Make sure you have run `./bootstrap/metamart-ops.sh migrate` at least once.");
    }
    if (lastMigrated.get().compareTo(maxMigration) < 0) {
      throw new IllegalStateException(
          "There are pending migrations to be run on the database."
              + " Please backup your data and run `./bootstrap/metamart-ops.sh migrate`."
              + " You can find more information on upgrading MetaMart at"
              + " https://docs.meta-mart.org/deployment/upgrade ");
    }

    LOG.info("Validating native migrations");
    ConnectionType connectionType =
        ConnectionType.from(conf.getDataSourceFactory().getDriverClass());
    MigrationWorkflow migrationWorkflow =
        new MigrationWorkflow(
            jdbi,
            conf.getMigrationConfiguration().getNativePath(),
            connectionType,
            conf.getMigrationConfiguration().getExtensionPath(),
            conf.getPipelineServiceClientConfiguration(),
            conf.getAuthenticationConfiguration(),
            false);
    migrationWorkflow.loadMigrations();
    migrationWorkflow.validateMigrationsForServer();
  }

  private void validateConfiguration(MetaMartApplicationConfig catalogConfig)
      throws ConfigurationException {
    if (catalogConfig.getAuthorizerConfiguration().getBotPrincipals() != null) {
      throw new ConfigurationException(
          "'botPrincipals' configuration is deprecated. Please remove it from "
              + "'metamart.yaml and restart the server");
    }
    if (catalogConfig.getPipelineServiceClientConfiguration().getAuthConfig() != null) {
      LOG.warn(
          "'authProvider' and 'authConfig' from the 'pipelineServiceClientConfiguration' option are deprecated and will be removed in future releases.");
    }
  }

  private void registerAuthorizer(
      MetaMartApplicationConfig catalogConfig, Environment environment)
      throws NoSuchMethodException,
          ClassNotFoundException,
          IllegalAccessException,
          InvocationTargetException,
          InstantiationException {
    AuthorizerConfiguration authorizerConf = catalogConfig.getAuthorizerConfiguration();
    AuthenticationConfiguration authenticationConfiguration =
        catalogConfig.getAuthenticationConfiguration();
    // to authenticate request while opening websocket connections
    if (authorizerConf != null) {
      authorizer =
          Class.forName(authorizerConf.getClassName())
              .asSubclass(Authorizer.class)
              .getConstructor()
              .newInstance();
      String filterClazzName = authorizerConf.getContainerRequestFilter();
      ContainerRequestFilter filter;
      if (!StringUtils.isEmpty(filterClazzName)) {
        filter =
            Class.forName(filterClazzName)
                .asSubclass(ContainerRequestFilter.class)
                .getConstructor(AuthenticationConfiguration.class, AuthorizerConfiguration.class)
                .newInstance(authenticationConfiguration, authorizerConf);
        LOG.info("Registering ContainerRequestFilter: {}", filter.getClass().getCanonicalName());
        environment.jersey().register(filter);
      }
    } else {
      LOG.info("Authorizer config not set, setting noop authorizer");
      authorizer = new NoopAuthorizer();
      ContainerRequestFilter filter = new NoopFilter(authenticationConfiguration, null);
      environment.jersey().register(filter);
    }
  }

  private void registerAuthenticator(MetaMartApplicationConfig catalogConfig) {
    AuthenticationConfiguration authenticationConfiguration =
        catalogConfig.getAuthenticationConfiguration();
    switch (authenticationConfiguration.getProvider()) {
      case BASIC -> authenticatorHandler = new BasicAuthenticator();
      case LDAP -> authenticatorHandler = new LdapAuthenticator();
      default ->
      // For all other types, google, okta etc. auth is handled externally
      authenticatorHandler = new NoopAuthenticator();
    }
  }

  private void registerLimits(MetaMartApplicationConfig serverConfig)
      throws NoSuchMethodException,
          ClassNotFoundException,
          IllegalAccessException,
          InvocationTargetException,
          InstantiationException {
    LimitsConfiguration limitsConfiguration = serverConfig.getLimitsConfiguration();
    if (limitsConfiguration != null && limitsConfiguration.getEnable()) {
      limits =
          Class.forName(limitsConfiguration.getClassName())
              .asSubclass(Limits.class)
              .getConstructor()
              .newInstance();
    } else {
      LOG.info("Limits config not set, setting DefaultLimits");
      limits = new DefaultLimits();
    }
    limits.init(serverConfig, jdbi);
  }

  private void registerEventFilter(
      MetaMartApplicationConfig catalogConfig, Environment environment) {
    if (catalogConfig.getEventHandlerConfiguration() != null) {
      ContainerResponseFilter eventFilter = new EventFilter(catalogConfig);
      environment.jersey().register(eventFilter);
    }
  }

  private void registerEventPublisher(MetaMartApplicationConfig metaMartApplicationConfig) {

    if (metaMartApplicationConfig.getEventMonitorConfiguration() != null) {
      final EventMonitor eventMonitor =
          EventMonitorFactory.createEventMonitor(
              metaMartApplicationConfig.getEventMonitorConfiguration(),
              metaMartApplicationConfig.getClusterName());
      EventMonitorPublisher eventMonitorPublisher =
          new EventMonitorPublisher(
              metaMartApplicationConfig.getEventMonitorConfiguration(), eventMonitor);
      EventPubSub.addEventHandler(eventMonitorPublisher);
    }
  }

  private void registerResources(
      MetaMartApplicationConfig config, Environment environment, Jdbi jdbi) {
    CollectionRegistry.initialize();
    CollectionRegistry.getInstance()
        .registerResources(jdbi, environment, config, authorizer, authenticatorHandler, limits);
    environment.jersey().register(new JsonPatchProvider());
    OMErrorPageHandler eph = new OMErrorPageHandler(config.getWebConfiguration());
    eph.addErrorPage(Response.Status.NOT_FOUND.getStatusCode(), "/");
    environment.getApplicationContext().setErrorHandler(eph);
  }

  private void initializeWebsockets(
      MetaMartApplicationConfig catalogConfig, Environment environment) {
    SocketAddressFilter socketAddressFilter;
    String pathSpec = "/api/v1/push/feed/*";
    if (catalogConfig.getAuthorizerConfiguration() != null) {
      socketAddressFilter =
          new SocketAddressFilter(
              catalogConfig.getAuthenticationConfiguration(),
              catalogConfig.getAuthorizerConfiguration());
    } else {
      socketAddressFilter = new SocketAddressFilter();
    }

    EngineIoServerOptions eioOptions = EngineIoServerOptions.newFromDefault();
    eioOptions.setAllowedCorsOrigins(null);
    WebSocketManager.WebSocketManagerBuilder.build(eioOptions);
    environment.getApplicationContext().setContextPath("/");
    environment
        .getApplicationContext()
        .addFilter(
            new FilterHolder(socketAddressFilter), pathSpec, EnumSet.of(DispatcherType.REQUEST));
    environment.getApplicationContext().addServlet(new ServletHolder(new FeedServlet()), pathSpec);
    // Upgrade connection to websocket from Http
    try {
      WebSocketUpgradeFilter.configure(environment.getApplicationContext());
      NativeWebSocketServletContainerInitializer.configure(
          environment.getApplicationContext(),
          (context, container) ->
              container.addMapping(
                  new ServletPathSpec(pathSpec),
                  (servletUpgradeRequest, servletUpgradeResponse) ->
                      new JettyWebSocketHandler(
                          WebSocketManager.getInstance().getEngineIoServer())));
    } catch (ServletException ex) {
      LOG.error("Websocket Upgrade Filter error : " + ex.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    MetaMartApplication metaMartApplication = new MetaMartApplication();
    metaMartApplication.run(args);
  }

  public static class ManagedShutdown implements Managed {

    @Override
    public void start() {
      LOG.info("Starting the application");
    }

    @Override
    public void stop() throws InterruptedException, SchedulerException {
      LOG.info("Cache with Id Stats {}", EntityRepository.CACHE_WITH_ID.stats());
      LOG.info("Cache with name Stats {}", EntityRepository.CACHE_WITH_NAME.stats());
      EventPubSub.shutdown();
      AppScheduler.shutDown();
      EventSubscriptionScheduler.shutDown();
      LOG.info("Stopping the application");
    }
  }
}
