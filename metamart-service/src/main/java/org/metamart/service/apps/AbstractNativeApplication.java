package org.metamart.service.apps;

import static org.metamart.service.apps.scheduler.AbstractOmAppJobListener.JOB_LISTENER_NAME;
import static org.metamart.service.apps.scheduler.AppScheduler.APP_NAME;
import static org.metamart.service.exception.CatalogExceptionMessage.NO_MANUAL_TRIGGER_ERR;
import static org.metamart.service.resources.apps.AppResource.SCHEDULED_TYPES;

import java.util.List;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.AppRuntime;
import org.metamart.schema.api.services.ingestionPipelines.CreateIngestionPipeline;
import org.metamart.schema.entity.app.App;
import org.metamart.schema.entity.app.AppRunRecord;
import org.metamart.schema.entity.app.AppType;
import org.metamart.schema.entity.app.ScheduleTimeline;
import org.metamart.schema.entity.app.ScheduleType;
import org.metamart.schema.entity.app.ScheduledExecutionContext;
import org.metamart.schema.entity.applications.configuration.ApplicationConfig;
import org.metamart.schema.entity.services.ingestionPipelines.AirflowConfig;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineType;
import org.metamart.schema.metadataIngestion.ApplicationPipeline;
import org.metamart.schema.metadataIngestion.SourceConfig;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.ProviderType;
import org.metamart.schema.type.Relationship;
import org.metamart.service.Entity;
import org.metamart.service.apps.scheduler.AppScheduler;
import org.metamart.service.apps.scheduler.OmAppJobListener;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.jdbi3.IngestionPipelineRepository;
import org.metamart.service.jdbi3.MetadataServiceRepository;
import org.metamart.service.search.SearchRepository;
import org.metamart.service.util.FullyQualifiedName;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.MetaMartConnectionBuilder;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.UnableToInterruptJobException;

@Getter
@Slf4j
public class AbstractNativeApplication implements NativeApplication {
  protected CollectionDAO collectionDAO;
  private App app;
  protected SearchRepository searchRepository;
  protected boolean isJobInterrupted = false;

  // Default service that contains external apps' Ingestion Pipelines
  private static final String SERVICE_NAME = "MetaMart";

  public AbstractNativeApplication(CollectionDAO collectionDAO, SearchRepository searchRepository) {
    this.collectionDAO = collectionDAO;
    this.searchRepository = searchRepository;
  }

  @Override
  public void init(App app) {
    this.app = app;
  }

  @Override
  public void install() {
    // If the app does not have any Schedule Return without scheduling
    if (Boolean.TRUE.equals(app.getDeleted())
        || (app.getAppSchedule() != null
            && app.getAppSchedule().getScheduleTimeline().equals(ScheduleTimeline.NONE))) {
      return;
    }
    if (app.getAppType().equals(AppType.Internal)
        && (SCHEDULED_TYPES.contains(app.getScheduleType()))) {
      try {
        ApplicationHandler.getInstance().removeOldJobs(app);
        ApplicationHandler.getInstance().migrateQuartzConfig(app);
        ApplicationHandler.getInstance().fixCorruptedInstallation(app);
      } catch (SchedulerException e) {
        throw AppException.byMessage(
            "ApplicationHandler",
            "SchedulerError",
            "Error while migrating application configuration: " + app.getName());
      }
      scheduleInternal();
    } else if (app.getAppType() == AppType.External
        && (SCHEDULED_TYPES.contains(app.getScheduleType()))) {
      scheduleExternal();
    }
  }

  @Override
  public void triggerOnDemand() {
    // Validate Native Application
    if (app.getScheduleType().equals(ScheduleType.ScheduledOrManual)) {
      AppRuntime runtime = getAppRuntime(app);
      validateServerExecutableApp(runtime);
      // Trigger the application
      AppScheduler.getInstance().triggerOnDemandApplication(app);
    } else {
      throw new IllegalArgumentException(NO_MANUAL_TRIGGER_ERR);
    }
  }

  public void scheduleInternal() {
    // Validate Native Application
    AppRuntime runtime = JsonUtils.convertValue(app.getRuntime(), ScheduledExecutionContext.class);
    validateServerExecutableApp(runtime);
    // Schedule New Application Run
    AppScheduler.getInstance().addApplicationSchedule(app);
  }

  public void scheduleExternal() {
    IngestionPipelineRepository ingestionPipelineRepository =
        (IngestionPipelineRepository) Entity.getEntityRepository(Entity.INGESTION_PIPELINE);

    try {
      bindExistingIngestionToApplication(ingestionPipelineRepository);
      updateAppConfig(ingestionPipelineRepository, this.getApp().getAppConfiguration());
    } catch (EntityNotFoundException ex) {
      ApplicationConfig config =
          JsonUtils.convertValue(this.getApp().getAppConfiguration(), ApplicationConfig.class);
      createAndBindIngestionPipeline(ingestionPipelineRepository, config);
    }
  }

  private void bindExistingIngestionToApplication(
      IngestionPipelineRepository ingestionPipelineRepository) {
    String fqn = FullyQualifiedName.add(SERVICE_NAME, this.getApp().getName());
    IngestionPipeline storedPipeline =
        ingestionPipelineRepository.getByName(
            null, fqn, ingestionPipelineRepository.getFields("id"));

    // Init Application Code for Some Initialization
    List<CollectionDAO.EntityRelationshipRecord> records =
        collectionDAO
            .relationshipDAO()
            .findTo(
                this.getApp().getId(),
                Entity.APPLICATION,
                Relationship.HAS.ordinal(),
                Entity.INGESTION_PIPELINE);

    if (records.isEmpty()) {
      // Add Ingestion Pipeline to Application
      collectionDAO
          .relationshipDAO()
          .insert(
              this.getApp().getId(),
              storedPipeline.getId(),
              Entity.APPLICATION,
              Entity.INGESTION_PIPELINE,
              Relationship.HAS.ordinal());
    }
  }

  private void updateAppConfig(IngestionPipelineRepository repository, Object appConfiguration) {
    String fqn = FullyQualifiedName.add(SERVICE_NAME, this.getApp().getName());
    IngestionPipeline updated = repository.findByName(fqn, Include.NON_DELETED);
    ApplicationPipeline appPipeline =
        JsonUtils.convertValue(updated.getSourceConfig().getConfig(), ApplicationPipeline.class);
    IngestionPipeline original = JsonUtils.deepCopy(updated, IngestionPipeline.class);
    updated.setSourceConfig(
        updated.getSourceConfig().withConfig(appPipeline.withAppConfig(appConfiguration)));
    repository.update(null, original, updated);
  }

  private void createAndBindIngestionPipeline(
      IngestionPipelineRepository ingestionPipelineRepository, ApplicationConfig config) {
    MetadataServiceRepository serviceEntityRepository =
        (MetadataServiceRepository) Entity.getEntityRepository(Entity.METADATA_SERVICE);
    EntityReference service =
        serviceEntityRepository
            .getByName(null, SERVICE_NAME, serviceEntityRepository.getFields("id"))
            .getEntityReference();

    CreateIngestionPipeline createPipelineRequest =
        new CreateIngestionPipeline()
            .withName(this.getApp().getName())
            .withDisplayName(this.getApp().getDisplayName())
            .withDescription(this.getApp().getDescription())
            .withPipelineType(PipelineType.APPLICATION)
            .withSourceConfig(
                new SourceConfig()
                    .withConfig(
                        new ApplicationPipeline()
                            .withSourcePythonClass(this.getApp().getSourcePythonClass())
                            .withAppConfig(config)
                            .withAppPrivateConfig(this.getApp().getPrivateConfiguration())))
            .withAirflowConfig(
                new AirflowConfig()
                    .withScheduleInterval(this.getApp().getAppSchedule().getCronExpression()))
            .withService(service);

    // Get Pipeline
    IngestionPipeline ingestionPipeline =
        getIngestionPipeline(
                createPipelineRequest, String.format("%sBot", this.getApp().getName()), "admin")
            .withProvider(ProviderType.USER);
    ingestionPipelineRepository.setFullyQualifiedName(ingestionPipeline);
    ingestionPipelineRepository.initializeEntity(ingestionPipeline);

    // Add Ingestion Pipeline to Application
    collectionDAO
        .relationshipDAO()
        .insert(
            this.getApp().getId(),
            ingestionPipeline.getId(),
            Entity.APPLICATION,
            Entity.INGESTION_PIPELINE,
            Relationship.HAS.ordinal());
  }

  @Override
  public void cleanup() {
    /* Not needed by default*/
  }

  protected void validateServerExecutableApp(AppRuntime context) {
    // Server apps are native
    if (!app.getAppType().equals(AppType.Internal)) {
      throw new IllegalArgumentException(
          "Application cannot be executed internally in Server. Please check if the App supports internal Server Execution.");
    }

    // Check OnDemand Execution is supported
    if (!(context != null && Boolean.TRUE.equals(context.getEnabled()))) {
      throw new IllegalArgumentException(
          "Applications does not support on demand execution or the context is not Internal.");
    }
  }

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {
    // This is the part of the code that is executed by the scheduler
    String appName = (String) jobExecutionContext.getJobDetail().getJobDataMap().get(APP_NAME);
    App jobApp = collectionDAO.applicationDAO().findEntityByName(appName);
    ApplicationHandler.getInstance().setAppRuntimeProperties(jobApp);
    // Initialise the Application
    this.init(jobApp);

    // Trigger
    this.startApp(jobExecutionContext);
  }

  @Override
  public void configure() {
    /* Not needed by default */
  }

  @Override
  public void raisePreviewMessage(App app) {
    throw AppException.byMessage(
        app.getName(),
        "Preview",
        "App is in Preview Mode. Enable it from the server configuration.");
  }

  public static AppRuntime getAppRuntime(App app) {
    return JsonUtils.convertValue(app.getRuntime(), ScheduledExecutionContext.class);
  }

  protected IngestionPipeline getIngestionPipeline(
      CreateIngestionPipeline create, String botName, String user) {
    IngestionPipelineRepository ingestionPipelineRepository =
        (IngestionPipelineRepository) Entity.getEntityRepository(Entity.INGESTION_PIPELINE);
    MetaMartConnection metaMartServerConnection =
        new MetaMartConnectionBuilder(
                ingestionPipelineRepository.getMetaMartApplicationConfig(), botName)
            .build();
    return ingestionPipelineRepository
        .copy(new IngestionPipeline(), create, user)
        .withPipelineType(create.getPipelineType())
        .withAirflowConfig(create.getAirflowConfig())
        .withMetaMartServerConnection(metaMartServerConnection)
        .withSourceConfig(create.getSourceConfig())
        .withLoggerLevel(create.getLoggerLevel())
        .withService(create.getService());
  }

  private OmAppJobListener getJobListener(JobExecutionContext jobExecutionContext)
      throws SchedulerException {
    return (OmAppJobListener)
        jobExecutionContext.getScheduler().getListenerManager().getJobListener(JOB_LISTENER_NAME);
  }

  @SneakyThrows
  protected AppRunRecord getJobRecord(JobExecutionContext jobExecutionContext) {
    OmAppJobListener listener = getJobListener(jobExecutionContext);
    return listener.getAppRunRecordForJob(jobExecutionContext);
  }

  @SneakyThrows
  protected void pushAppStatusUpdates(
      JobExecutionContext jobExecutionContext, AppRunRecord appRecord, boolean update) {
    OmAppJobListener listener = getJobListener(jobExecutionContext);
    listener.pushApplicationStatusUpdates(jobExecutionContext, appRecord, update);
  }

  @Override
  public void interrupt() throws UnableToInterruptJobException {
    LOG.info("Interrupting the job for app: {}", this.app.getName());
    isJobInterrupted = true;
  }
}
