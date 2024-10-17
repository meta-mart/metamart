package org.metamart.service.resources.system;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.json.JsonPatch;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.auth.EmailRequest;
import org.metamart.schema.settings.Settings;
import org.metamart.schema.settings.SettingsType;
import org.metamart.schema.system.ValidationResponse;
import org.metamart.schema.type.Include;
import org.metamart.schema.util.EntitiesCount;
import org.metamart.schema.util.ServicesCount;
import org.metamart.sdk.PipelineServiceClientInterface;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.clients.pipeline.PipelineServiceClientFactory;
import org.metamart.service.exception.UnhandledServerException;
import org.metamart.service.jdbi3.ListFilter;
import org.metamart.service.jdbi3.SystemRepository;
import org.metamart.service.resources.Collection;
import org.metamart.service.security.Authorizer;
import org.metamart.service.security.JwtFilter;
import org.metamart.service.util.ResultList;
import org.metamart.service.util.email.EmailUtil;

@Path("/v1/system")
@Tag(name = "System", description = "APIs related to System configuration and settings.")
@Hidden
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "system")
@Slf4j
public class SystemResource {
  public static final String COLLECTION_PATH = "/v1/system";
  private final SystemRepository systemRepository;
  private final Authorizer authorizer;
  private MetaMartApplicationConfig applicationConfig;
  private PipelineServiceClientInterface pipelineServiceClient;
  private JwtFilter jwtFilter;

  public SystemResource(Authorizer authorizer) {
    this.systemRepository = Entity.getSystemRepository();
    this.authorizer = authorizer;
  }

  public void initialize(MetaMartApplicationConfig config) {
    this.applicationConfig = config;
    this.pipelineServiceClient =
        PipelineServiceClientFactory.createPipelineServiceClient(
            config.getPipelineServiceClientConfiguration());

    this.jwtFilter =
        new JwtFilter(config.getAuthenticationConfiguration(), config.getAuthorizerConfiguration());
  }

  public static class SettingsList extends ResultList<Settings> {
    /* Required for serde */
  }

  @GET
  @Path("/settings")
  @Operation(
      operationId = "listSettings",
      summary = "List all settings",
      description = "Get a list of all MetaMart settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of Settings",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SettingsList.class)))
      })
  public ResultList<Settings> list(
      @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
    authorizer.authorizeAdmin(securityContext);
    return systemRepository.listAllConfigs();
  }

  @GET
  @Path("/settings/{name}")
  @Operation(
      operationId = "getSetting",
      summary = "Get a setting",
      description = "Get a MetaMart Settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Settings",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Settings.class)))
      })
  public Settings getSettingByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Name of the setting", schema = @Schema(type = "string"))
          @PathParam("name")
          String name) {
    authorizer.authorizeAdmin(securityContext);
    return systemRepository.getConfigWithKey(name);
  }

  @GET
  @Path("/settings/profilerConfiguration")
  @Operation(
      operationId = "getProfilerConfigurationSetting",
      summary = "Get profiler configuration setting",
      description = "Get a profiler configuration Settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Settings",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Settings.class)))
      })
  public Settings getProfilerConfigurationSetting(
      @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
    authorizer.authorizeAdminOrBot(securityContext);
    return systemRepository.getConfigWithKey(SettingsType.PROFILER_CONFIGURATION.value());
  }

  @PUT
  @Path("/settings")
  @Operation(
      operationId = "createOrUpdate",
      summary = "Update setting",
      description = "Update existing settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Settings",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Settings.class)))
      })
  public Response createOrUpdateSetting(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid Settings settingName) {
    authorizer.authorizeAdmin(securityContext);
    return systemRepository.createOrUpdate(settingName);
  }

  @PUT
  @Path("/email/test")
  @Operation(
      operationId = "sendTestEmail",
      summary = "Sends a Test Email",
      description = "Sends a Test Email with Provided Settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "EmailTest",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = String.class)))
      })
  public Response sendTestEmail(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid EmailRequest emailRequest) {
    if (nullOrEmpty(emailRequest.getEmail())) {
      throw new IllegalArgumentException("Email address is required.");
    }

    authorizer.authorizeAdmin(securityContext);

    try {
      EmailUtil.testConnection();
      EmailUtil.sendTestEmail(emailRequest.getEmail(), false);
    } catch (Exception ex) {
      LOG.error("Failed in sending mail. Message: {}", ex.getMessage(), ex);
      throw new UnhandledServerException(ex.getMessage());
    }

    return Response.status(Response.Status.OK).entity("Test Email Sent Successfully.").build();
  }

  @PATCH
  @Path("/settings/{settingName}")
  @Operation(
      operationId = "patchSetting",
      summary = "Patch a setting",
      description = "Update an existing Setting using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Key of the Setting", schema = @Schema(type = "string"))
          @PathParam("settingName")
          String settingName,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                      }))
          JsonPatch patch) {
    authorizer.authorizeAdmin(securityContext);
    return systemRepository.patchSetting(settingName, patch);
  }

  @PUT
  @Path("/restore/default/email")
  @Operation(
      operationId = "restoreEmailSettingToDefault",
      summary = "Restore Email to Default setting",
      description = "Restore Email to Default settings",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Settings",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = Settings.class)))
      })
  public Response restoreDefaultEmailSetting(
      @Context UriInfo uriInfo, @Context SecurityContext securityContext) {
    authorizer.authorizeAdmin(securityContext);
    return systemRepository.createOrUpdate(
        new Settings()
            .withConfigType(SettingsType.EMAIL_CONFIGURATION)
            .withConfigValue(applicationConfig.getSmtpSettings()));
  }

  @GET
  @Path("/entities/count")
  @Operation(
      operationId = "listEntitiesCount",
      summary = "List all entities counts",
      description = "Get a list of all entities count",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of Entities Count",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntitiesCount.class)))
      })
  public EntitiesCount listEntitiesCount(
      @Context UriInfo uriInfo,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    ListFilter filter = new ListFilter(include);
    return systemRepository.getAllEntitiesCount(filter);
  }

  @GET
  @Path("/services/count")
  @Operation(
      operationId = "listServicesCount",
      summary = "List all services counts",
      description = "Get a list of all entities count",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of Services Count",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ServicesCount.class)))
      })
  public ServicesCount listServicesCount(
      @Context UriInfo uriInfo,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    ListFilter filter = new ListFilter(include);
    return systemRepository.getAllServicesCount(filter);
  }

  @GET
  @Path("/status")
  @Operation(
      operationId = "validateDeployment",
      summary = "Validate the MetaMart deployment",
      description =
          "Check connectivity against your database, elasticsearch/opensearch, migrations,...",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "validation OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ServicesCount.class)))
      })
  public ValidationResponse validate() {
    return systemRepository.validateSystem(applicationConfig, pipelineServiceClient, jwtFilter);
  }
}
