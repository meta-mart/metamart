package org.metamart.service.resources.services.metadata;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.Entity.ADMIN_USER_NAME;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.json.JsonPatch;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
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
import org.metamart.schema.api.data.RestoreEntity;
import org.metamart.schema.api.services.CreateMetadataService;
import org.metamart.schema.entity.services.MetadataConnection;
import org.metamart.schema.entity.services.MetadataService;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.entity.services.connections.TestConnectionResult;
import org.metamart.schema.service.configuration.elasticsearch.ElasticSearchConfiguration;
import org.metamart.schema.services.connections.metadata.ComponentConfig;
import org.metamart.schema.services.connections.metadata.ElasticsSearch;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.schema.type.EntityHistory;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.MetadataOperation;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.jdbi3.MetadataServiceRepository;
import org.metamart.service.limits.Limits;
import org.metamart.service.resources.Collection;
import org.metamart.service.resources.services.ServiceEntityResource;
import org.metamart.service.security.Authorizer;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.MetaMartConnectionBuilder;
import org.metamart.service.util.ResultList;

@Slf4j
@Path("/v1/services/metadataServices")
@Tag(
    name = "Metadata Services",
    description =
        "APIs related to creating and managing other Metadata Services that "
            + "MetaMart integrates with such as `Apache Atlas`, `Amundsen`, etc.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "metadataServices", order = 8) // init before IngestionPipelineService
public class MetadataServiceResource
    extends ServiceEntityResource<MetadataService, MetadataServiceRepository, MetadataConnection> {
  public static final String METAMART_SERVICE = "MetaMart";
  public static final String COLLECTION_PATH = "v1/services/metadataServices/";
  public static final String FIELDS = "pipelines,owners,tags";

  @Override
  public void initialize(MetaMartApplicationConfig config) throws IOException {
    registerMetadataServices(config);
  }

  private void registerMetadataServices(MetaMartApplicationConfig config) throws IOException {
    List<MetadataService> servicesList =
        repository.getEntitiesFromSeedData(".*json/data/metadataService/MetamartService.json$");
    if (!nullOrEmpty(servicesList)) {
      MetadataService metaMartService = servicesList.get(0);
      metaMartService.setId(UUID.randomUUID());
      metaMartService.setUpdatedBy(ADMIN_USER_NAME);
      metaMartService.setUpdatedAt(System.currentTimeMillis());
      if (config.getElasticSearchConfiguration() != null) {
        MetaMartConnection metaMartServerConnection =
            new MetaMartConnectionBuilder(config)
                .build()
                .withElasticsSearch(
                    getElasticSearchConnectionSink(config.getElasticSearchConfiguration()));
        MetadataConnection metadataConnection =
            new MetadataConnection().withConfig(metaMartServerConnection);
        metaMartService.setConnection(metadataConnection);
      } else {
        LOG.error("[MetadataService] Missing Elastic Search Config.");
      }
      repository.setFullyQualifiedName(metaMartService);
      repository.createOrUpdate(null, metaMartService);
    } else {
      throw new IOException("Failed to initialize MetaMart Service.");
    }
  }

  @Override
  public MetadataService addHref(UriInfo uriInfo, MetadataService service) {
    super.addHref(uriInfo, service);
    Entity.withHref(uriInfo, service.getOwners());
    return service;
  }

  public MetadataServiceResource(Authorizer authorizer, Limits limits) {
    super(Entity.METADATA_SERVICE, authorizer, limits, ServiceType.METADATA);
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("pipelines", MetadataOperation.VIEW_BASIC);
    return null;
  }

  public static class MetadataServiceList extends ResultList<MetadataService> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listMetadataServices",
      summary = "List metadata services",
      description = "Get a list of metadata services.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of Metadata Service instances",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(
                            implementation = MetadataServiceResource.MetadataServiceList.class)))
      })
  public ResultList<MetadataService> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @DefaultValue("10") @Min(0) @Max(1000000) @QueryParam("limit") int limitParam,
      @Parameter(
              description = "Returns list of metadata services before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of metadata services after this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("after")
          String after,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    return listInternal(
        uriInfo, securityContext, fieldsParam, include, null, limitParam, before, after);
  }

  @GET
  @Path("/{id}")
  @Operation(
      operationId = "getMetadataServiceByID",
      summary = "Get a metadata service by Id",
      description = "Get a Metadata Service by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata Service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Metadata Service for instance {id} is not found")
      })
  public MetadataService get(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the metadata service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    MetadataService metadataService =
        getInternal(uriInfo, securityContext, id, fieldsParam, include);
    return decryptOrNullify(securityContext, metadataService);
  }

  @GET
  @Path("/name/{name}")
  @Operation(
      operationId = "getMetadataServiceByFQN",
      summary = "Get a metadata service by name",
      description = "Get a Metadata Service by the service `name`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata Service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Metadata Service for instance {name} is not found")
      })
  public MetadataService getByName(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Name of the metadata service", schema = @Schema(type = "string"))
          @PathParam("name")
          String name,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    MetadataService metadataService =
        getByNameInternal(uriInfo, securityContext, name, fieldsParam, include);
    return decryptOrNullify(securityContext, metadataService);
  }

  @PUT
  @Path("/{id}/testConnectionResult")
  @Operation(
      operationId = "addTestConnectionResult",
      summary = "Add test connection result",
      description = "Add test connection result to the service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully updated the service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class)))
      })
  public MetadataService addTestConnectionResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Valid TestConnectionResult testConnectionResult) {
    OperationContext operationContext = new OperationContext(entityType, MetadataOperation.CREATE);
    authorizer.authorize(securityContext, operationContext, getResourceContextById(id));
    MetadataService service = repository.addTestConnectionResult(id, testConnectionResult);
    return decryptOrNullify(securityContext, service);
  }

  @GET
  @Path("/{id}/versions")
  @Operation(
      operationId = "listAllMetadataServiceVersion",
      summary = "List metadata service versions",
      description = "Get a list of all the versions of a Metadata Service identified by `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of Metadata Service versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the metadata service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    EntityHistory entityHistory = super.listVersionsInternal(securityContext, id);

    List<Object> versions =
        entityHistory.getVersions().stream()
            .map(
                json -> {
                  try {
                    MetadataService metadataService =
                        JsonUtils.readValue((String) json, MetadataService.class);
                    return JsonUtils.pojoToJson(decryptOrNullify(securityContext, metadataService));
                  } catch (Exception e) {
                    return json;
                  }
                })
            .collect(Collectors.toList());
    entityHistory.setVersions(versions);
    return entityHistory;
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getSpecificMetadataServiceVersion",
      summary = "Get a version of the metadata service",
      description = "Get a version of the Metadata Service by given `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata Service",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Metadata Service for instance {id} and version {version} is not found")
      })
  public MetadataService getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the metadata service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "Metadata Service version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    MetadataService metadataService = super.getVersionInternal(securityContext, id, version);
    return decryptOrNullify(securityContext, metadataService);
  }

  @POST
  @Operation(
      operationId = "createMetadataService",
      summary = "Create metadata service",
      description = "Create a new Metadata Service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata Service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMetadataService create) {
    MetadataService service =
        getMetadataService(create, securityContext.getUserPrincipal().getName());
    Response response = create(uriInfo, securityContext, service);
    decryptOrNullify(securityContext, (MetadataService) response.getEntity());
    return response;
  }

  @PUT
  @Operation(
      operationId = "createOrUpdateMetadataService",
      summary = "Update metadata service",
      description = "Update an existing or create a new Metadata Service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Metadata Service instance",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateMetadataService update) {
    MetadataService service =
        getMetadataService(update, securityContext.getUserPrincipal().getName());
    Response response = createOrUpdate(uriInfo, securityContext, unmask(service));
    decryptOrNullify(securityContext, (MetadataService) response.getEntity());
    return response;
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchMetadataService",
      summary = "Update a metadata service",
      description = "Update an existing Metadata service using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the metadata service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                      }))
          JsonPatch patch) {
    return patchInternal(uriInfo, securityContext, id, patch);
  }

  @PATCH
  @Path("/name/{fqn}")
  @Operation(
      operationId = "patchMetadataService",
      summary = "Update a metadata service using name.",
      description = "Update an existing Metadata service using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Name of the metadata service", schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                      }))
          JsonPatch patch) {
    return patchInternal(uriInfo, securityContext, fqn, patch);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteMetadataService",
      summary = "Delete a metadata service by Id",
      description =
          "Delete a metadata services. If some service belong the service, it can't be deleted.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MetadataService service for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Recursively delete this entity and it's children. (Default `false`)")
          @DefaultValue("false")
          @QueryParam("recursive")
          boolean recursive,
      @Parameter(description = "Hard delete the entity. (Default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(description = "Id of the metadata service", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, recursive, hardDelete);
  }

  @DELETE
  @Path("/name/{name}")
  @Operation(
      operationId = "deleteMetadataServiceByName",
      summary = "Delete a metadata service by name",
      description =
          "Delete a metadata services by `name`. If some service belong the service, it can't be deleted.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "MetadataService service for instance {name} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Hard delete the entity. (Default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(
              description = "Recursively delete this entity and it's children. (Default `false`)")
          @QueryParam("recursive")
          @DefaultValue("false")
          boolean recursive,
      @Parameter(description = "Name of the metadata service", schema = @Schema(type = "string"))
          @PathParam("name")
          String name) {
    return deleteByName(uriInfo, securityContext, name, recursive, hardDelete);
  }

  @PUT
  @Path("/restore")
  @Operation(
      operationId = "restore",
      summary = "Restore a soft deleted metadata service.",
      description = "Restore a soft deleted metadata service.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the MetadataService ",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = MetadataService.class)))
      })
  public Response restoreMetadataService(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid RestoreEntity restore) {
    return restoreEntity(uriInfo, securityContext, restore.getId());
  }

  private MetadataService getMetadataService(CreateMetadataService create, String user) {
    return repository
        .copy(new MetadataService(), create, user)
        .withServiceType(create.getServiceType())
        .withConnection(create.getConnection());
  }

  @Override
  protected MetadataService nullifyConnection(MetadataService service) {
    return service.withConnection(null);
  }

  @Override
  protected String extractServiceType(MetadataService service) {
    return service.getServiceType().value();
  }

  private ElasticsSearch getElasticSearchConnectionSink(ElasticSearchConfiguration esConfig)
      throws IOException {
    if (Objects.nonNull(esConfig)) {
      ElasticsSearch sink = new ElasticsSearch();
      ComponentConfig componentConfig = new ComponentConfig();
      sink.withType("elasticsearch")
          .withConfig(
              componentConfig
                  .withAdditionalProperty("es_host", esConfig.getHost())
                  .withAdditionalProperty("es_port", esConfig.getPort().toString())
                  .withAdditionalProperty("es_username", esConfig.getUsername())
                  .withAdditionalProperty("es_password", esConfig.getPassword())
                  .withAdditionalProperty("scheme", esConfig.getScheme()));
      return sink;
    } else {
      throw new IOException("Elastic Search Configuration Missing");
    }
  }
}
