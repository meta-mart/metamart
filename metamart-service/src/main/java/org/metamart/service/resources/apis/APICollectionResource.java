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

package org.metamart.service.resources.apis;

import static org.metamart.common.utils.CommonUtil.listOf;

import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
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
import org.metamart.schema.api.VoteRequest;
import org.metamart.schema.api.data.CreateAPICollection;
import org.metamart.schema.api.data.RestoreEntity;
import org.metamart.schema.entity.data.APICollection;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.schema.type.EntityHistory;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.MetadataOperation;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.APICollectionRepository;
import org.metamart.service.jdbi3.ListFilter;
import org.metamart.service.limits.Limits;
import org.metamart.service.resources.Collection;
import org.metamart.service.resources.EntityResource;
import org.metamart.service.security.Authorizer;
import org.metamart.service.util.ResultList;

@Path("/v1/apiCollections")
@Tag(
    name = "API Collections",
    description =
        "A `API Collection` is an optional way of grouping API Endpoints that belong to a API Service.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "apiCollections")
public class APICollectionResource extends EntityResource<APICollection, APICollectionRepository> {
  public static final String COLLECTION_PATH = "v1/apiCollections/";
  static final String FIELDS = "owners,apiEndpoints,tags,extension,domain,sourceHash";

  @Override
  public APICollection addHref(UriInfo uriInfo, APICollection apiCollection) {
    super.addHref(uriInfo, apiCollection);
    Entity.withHref(uriInfo, apiCollection.getService());
    return apiCollection;
  }

  @Override
  protected List<MetadataOperation> getEntitySpecificOperations() {
    addViewOperation("apiEndpoints", MetadataOperation.VIEW_BASIC);
    return listOf(MetadataOperation.VIEW_USAGE, MetadataOperation.EDIT_USAGE);
  }

  public APICollectionResource(Authorizer authorizer, Limits limits) {
    super(Entity.API_COLLCECTION, authorizer, limits);
  }

  public static class APICollectionList extends ResultList<APICollection> {
    /* Required for serde */
  }

  @GET
  @Operation(
      operationId = "listAPICollections",
      summary = "List API Collections",
      description =
          "Get a list of API Collections, optionally filtered by `service` it belongs to. Use `fields` "
              + "parameter to get only necessary fields. Use cursor-based pagination to limit the number "
              + "entries in the list using `limit` and `before` or `after` query params.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of API Collections",
            content =
                @Content(
                    mediaType = "application/json",
                    schema =
                        @Schema(implementation = APICollectionResource.APICollectionList.class)))
      })
  public ResultList<APICollection> list(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldsParam,
      @Parameter(
              description = "Filter APICollection by service name",
              schema = @Schema(type = "string", example = "Users API"))
          @QueryParam("service")
          String serviceParam,
      @Parameter(
              description =
                  "Limit the number APICollections returned. (1 to 1000000, default = 10)")
          @DefaultValue("10")
          @QueryParam("limit")
          @Min(0)
          @Max(1000000)
          int limitParam,
      @Parameter(
              description = "Returns list of API Collections before this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("before")
          String before,
      @Parameter(
              description = "Returns list of API Collections after this cursor",
              schema = @Schema(type = "string"))
          @QueryParam("after")
          String after,
      @Parameter(
              description = "Include all, deleted, or non-deleted entities.",
              schema = @Schema(implementation = Include.class))
          @QueryParam("include")
          @DefaultValue("non-deleted")
          Include include) {
    ListFilter filter = new ListFilter(include).addQueryParam("service", serviceParam);
    return super.listInternal(
        uriInfo, securityContext, fieldsParam, filter, limitParam, before, after);
  }

  @GET
  @Path("/{id}/versions")
  @Operation(
      operationId = "listAllAPICollectionVersion",
      summary = "List API Collection versions",
      description = "Get a list of all the versions of a API Collection identified by `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of API Collection versions",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EntityHistory.class)))
      })
  public EntityHistory listVersions(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the API Collection", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return super.listVersionsInternal(securityContext, id);
  }

  @GET
  @Path("/{id}")
  @Operation(
      operationId = "getAPICollectionByID",
      summary = "Get a API Collection by Id",
      description = "Get a API Collection by `Id`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The API Collection",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class))),
        @ApiResponse(
            responseCode = "404",
            description = "APICollection for instance {id} is not found")
      })
  public APICollection get(
      @Context UriInfo uriInfo,
      @Parameter(description = "Id of the APICollection", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Context SecurityContext securityContext,
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
    return getInternal(uriInfo, securityContext, id, fieldsParam, include);
  }

  @GET
  @Path("/name/{fqn}")
  @Operation(
      operationId = "getAPICollectionByFQN",
      summary = "Get a APICollection by fully qualified name",
      description = "Get a APICollection by `fullyQualifiedName`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The APICollection",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class))),
        @ApiResponse(
            responseCode = "404",
            description = "APICollection for instance {fqn} is not found")
      })
  public APICollection getByName(
      @Context UriInfo uriInfo,
      @Parameter(
              description = "Fully qualified name of the APICollection",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Context SecurityContext securityContext,
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
    return getByNameInternal(uriInfo, securityContext, fqn, fieldsParam, include);
  }

  @GET
  @Path("/{id}/versions/{version}")
  @Operation(
      operationId = "getSpecificAPICollectionVersion",
      summary = "Get a version of the APICollection",
      description = "Get a version of the APICollection by given `Id`",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "APICollection",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class))),
        @ApiResponse(
            responseCode = "404",
            description = "APICollection for instance {id} and version {version} is not found")
      })
  public APICollection getVersion(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the APICollection", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id,
      @Parameter(
              description = "APICollection version number in the form `major`.`minor`",
              schema = @Schema(type = "string", example = "0.1 or 1.1"))
          @PathParam("version")
          String version) {
    return super.getVersionInternal(securityContext, id, version);
  }

  @POST
  @Operation(
      operationId = "createAPICollection",
      summary = "Create a APICollection",
      description = "Create a APICollection under an existing `service`.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The API Collection",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class))),
        @ApiResponse(responseCode = "400", description = "Bad request")
      })
  public Response create(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateAPICollection create) {
    APICollection apiCollection =
        getAPICollection(create, securityContext.getUserPrincipal().getName());
    return create(uriInfo, securityContext, apiCollection);
  }

  @PATCH
  @Path("/{id}")
  @Operation(
      operationId = "patchAPICollection",
      summary = "Update a API Collection by Id",
      description = "Update an existing API Collection using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the API Collection", schema = @Schema(type = "UUID"))
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
      operationId = "patchAPICollection",
      summary = "Update a APICollection by name.",
      description = "Update an existing APICollection using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Name of the API Collection", schema = @Schema(type = "string"))
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

  @PUT
  @Operation(
      operationId = "createOrUpdateAPICollection",
      summary = "Create or update API Collection",
      description =
          "Create a API Collection, if it does not exist or update an existing API Collection.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "The updated API Collection",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class)))
      })
  public Response createOrUpdate(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid CreateAPICollection create) {
    APICollection apiCollection =
        getAPICollection(create, securityContext.getUserPrincipal().getName());
    return createOrUpdate(uriInfo, securityContext, apiCollection);
  }

  @DELETE
  @Path("/{id}")
  @Operation(
      operationId = "deleteAPICollection",
      summary = "Delete a API Collection by Id",
      description =
          "Delete a API Collection by `Id`. API Collection can only be deleted if it has no tables.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "API Collection for instance {id} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Recursively delete this entity and it's children. (default `false`)")
          @DefaultValue("false")
          @QueryParam("recursive")
          boolean recursive,
      @Parameter(description = "Hard delete the entity. (default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(description = "Id of the APICollection", schema = @Schema(type = "UUID"))
          @PathParam("id")
          UUID id) {
    return delete(uriInfo, securityContext, id, recursive, hardDelete);
  }

  @PUT
  @Path("/{id}/vote")
  @Operation(
      operationId = "updateVoteForAPICollection",
      summary = "Update Vote for a API Collection",
      description = "Update vote for a API Collection",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "OK",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ChangeEvent.class))),
        @ApiResponse(
            responseCode = "404",
            description = "API Collection for instance {id} is not found")
      })
  public Response updateVote(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Id of the Entity", schema = @Schema(type = "UUID")) @PathParam("id")
          UUID id,
      @Valid VoteRequest request) {
    return repository
        .updateVote(securityContext.getUserPrincipal().getName(), id, request)
        .toResponse();
  }

  @DELETE
  @Path("/name/{fqn}")
  @Operation(
      operationId = "deleteAPICollectionByFQN",
      summary = "Delete a API Collection by fully qualified name",
      description =
          "Delete a API Collection by `fullyQualifiedName`. API Collection can only be deleted if it has no API Endpoints.",
      responses = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(
            responseCode = "404",
            description = "APIColletion for instance {fqn} is not found")
      })
  public Response delete(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "Hard delete the entity. (default = `false`)")
          @QueryParam("hardDelete")
          @DefaultValue("false")
          boolean hardDelete,
      @Parameter(
              description = "Recursively delete this entity and it's children. (default `false`)")
          @QueryParam("recursive")
          @DefaultValue("false")
          boolean recursive,
      @Parameter(
              description = "Fully qualified name of the APICollection",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn) {
    return deleteByName(uriInfo, securityContext, fqn, recursive, hardDelete);
  }

  @PUT
  @Path("/restore")
  @Operation(
      operationId = "restore",
      summary = "Restore a soft deleted API Collection.",
      description = "Restore a soft deleted API Collection.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully restored the API Collection. ",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = APICollection.class)))
      })
  public Response restore(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Valid RestoreEntity restore) {
    return restoreEntity(uriInfo, securityContext, restore.getId());
  }

  private APICollection getAPICollection(CreateAPICollection create, String user) {
    return repository
        .copy(new APICollection(), create, user)
        .withService(getEntityReference(Entity.API_SERVICE, create.getService()))
        .withEndpointURL(create.getEndpointURL())
        .withApiEndpoints(getEntityReferences(Entity.API_ENDPOINT, create.getApiEndpoints()))
        .withSourceHash(create.getSourceHash());
  }
}
