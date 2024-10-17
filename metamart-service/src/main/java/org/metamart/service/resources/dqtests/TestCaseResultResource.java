package org.metamart.service.resources.dqtests;

import static org.metamart.service.Entity.TEST_CASE;

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
import java.util.Optional;
import java.util.Set;
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
import org.metamart.schema.api.tests.CreateTestCaseResult;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.tests.type.TestCaseResult;
import org.metamart.schema.tests.type.TestCaseStatus;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.MetadataOperation;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.TestCaseResultRepository;
import org.metamart.service.resources.Collection;
import org.metamart.service.resources.EntityTimeSeriesResource;
import org.metamart.service.resources.feeds.MessageParser;
import org.metamart.service.search.SearchListFilter;
import org.metamart.service.search.SearchSortFilter;
import org.metamart.service.security.Authorizer;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.security.policyevaluator.ResourceContextInterface;
import org.metamart.service.security.policyevaluator.TestCaseResourceContext;
import org.metamart.service.util.EntityUtil;
import org.metamart.service.util.RestUtil;
import org.metamart.service.util.ResultList;

@Slf4j
@Path("/v1/dataQuality/testCases/testCaseResults")
@Tag(
    name = "Test Case Results",
    description =
        "Test case results are the results of running a test case on a dataset. "
            + "This resource provides APIs to manage test case results.")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Collection(name = "TestCaseResults")
public class TestCaseResultResource
    extends EntityTimeSeriesResource<TestCaseResult, TestCaseResultRepository> {
  static final String FIELDS = "testCase,testDefinition";

  public TestCaseResultResource(Authorizer authorizer) {
    super(Entity.TEST_CASE_RESULT, authorizer);
  }

  public static class TestCaseResultList extends ResultList<TestCaseResult> {
    /* Required for serde */
  }

  @POST
  @Path("/{fqn}")
  @Operation(
      operationId = "addTestCaseResult",
      summary = "Add test case result data to a testCase",
      description = "Add test case result data to the testCase.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully created the TestCase. ",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestCaseResult.class)))
      })
  public Response addTestCaseResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the test case",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Valid CreateTestCaseResult createTestCaseResults) {
    ResourceContextInterface resourceContext = TestCaseResourceContext.builder().name(fqn).build();
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.EDIT_TESTS);
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.addTestCaseResult(
        securityContext.getUserPrincipal().getName(), uriInfo, fqn, createTestCaseResults);
  }

  @GET
  @Path("/{fqn}")
  @Operation(
      operationId = "listTestCaseResults",
      summary = "List of test case results for a given test case",
      description =
          "Get a list of all the test case results for the given testCase FQN, optionally filtered by  `startTimestamp` and `endTimestamp`. "
              + "Use cursor-based pagination to limit the number of "
              + "entries in the list using `limit` and `before` or `after` query params.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of testCase results",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestCaseResultList.class)))
      })
  public ResultList<TestCaseResult> list(
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the test case",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Parameter(
              description = "Filter testCase results after the given start timestamp",
              schema = @Schema(type = "number"))
          @QueryParam("startTs")
          Long startTs,
      @Parameter(
              description = "Filter testCase results before the given end timestamp",
              schema = @Schema(type = "number"))
          @QueryParam("endTs")
          Long endTs) {
    ResourceContextInterface resourceContext = TestCaseResourceContext.builder().name(fqn).build();
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.EDIT_TESTS);
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.getTestCaseResults(fqn, startTs, endTs);
  }

  @GET
  @Path("/search/list")
  @Operation(
      operationId = "listTestCaseResultsFromSearchService",
      summary = "List test case results using search service",
      description =
          "List test case results from the search service. "
              + "Use `startTimestamp` and `endTimestamp` to filter the results by timestamp. "
              + "Use `testCaseFQN` to filter the results by test case fully qualified name. "
              + "Use `testCaseStatus` to filter the results by test case status. "
              + "Use `fields` to get only necessary fields. ",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of test case results",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestCaseResultList.class)))
      })
  public ResultList<TestCaseResult> listTestCaseResultsFromSearch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldParams,
      @Parameter(
              description =
                  "Limit the number tests case results returned. (1 to 1000000, default = 10)")
          @DefaultValue("10")
          @QueryParam("limit")
          @Min(0)
          @Max(1000000)
          int limit,
      @Parameter(
              description = "Returns list of tests after this offset",
              schema = @Schema(type = "string"))
          @QueryParam("offset")
          @DefaultValue("0")
          @Min(0)
          int offset,
      @Parameter(
              description = "Start timestamp to list test case from",
              schema = @Schema(type = "number"))
          @QueryParam("startTimestamp")
          Long startTimestamp,
      @Parameter(
              description = "End timestamp to list test case from",
              schema = @Schema(type = "number"))
          @QueryParam("endTimestamp")
          Long endTimestamp,
      @Parameter(
              description = "Status of the test case -- one of Success, Failed, Aborted, Queued",
              schema = @Schema(implementation = TestCaseStatus.class))
          @QueryParam("testCaseStatus")
          TestCaseStatus testCaseStatus,
      @Parameter(
              description = "FullyQualifiedName of the test case",
              schema = @Schema(type = "string"))
          @QueryParam("testCaseFQN")
          String testCaseFQN,
      @Parameter(
              description = "Test Suite Id the test case belongs to",
              schema = @Schema(type = "string"))
          @QueryParam("testSuiteId")
          String testSuiteId,
      @Parameter(
              description = "Entity FQN the test case belongs to",
              schema = @Schema(type = "string"))
          @QueryParam("entityFQN")
          String entityFQN,
      @Parameter(
              description =
                  "Get the latest test case result for each test case -- requires `testSuiteId`. Offset and limit are ignored",
              schema =
                  @Schema(
                      type = "boolean",
                      example = "false",
                      allowableValues = {"true", "false"}))
          @QueryParam("latest")
          @DefaultValue("false")
          String latest,
      @Parameter(
              description = "Filter for test case result by type (e.g. column, table, all)",
              schema =
                  @Schema(
                      type = "string",
                      example = "all",
                      allowableValues = {"column", "table", "all"}))
          @QueryParam("testCaseType")
          @DefaultValue("all")
          String type,
      @Parameter(
              description =
                  "Filter for test case by data quality dimension (e.g. MetaMart, dbt, etc.)",
              schema =
                  @Schema(
                      type = "string",
                      allowableValues = {
                        "Completeness",
                        "Accuracy",
                        "Consistency",
                        "Validity",
                        "Uniqueness",
                        "Integrity",
                        "SQL"
                      }))
          @QueryParam("dataQualityDimension")
          String dataQualityDimension,
      @Parameter(
              description = "search query term to use in list",
              schema = @Schema(type = "string"))
          @QueryParam("q")
          String q)
      throws IOException {
    if (latest.equals("true") && (testSuiteId == null && entityFQN == null)) {
      throw new IllegalArgumentException("latest=true requires testSuiteId");
    }
    EntityUtil.Fields fields = repository.getFields(fieldParams);
    SearchListFilter searchListFilter = new SearchListFilter();
    Optional.ofNullable(startTimestamp)
        .ifPresent(ts -> searchListFilter.addQueryParam("startTimestamp", ts.toString()));
    Optional.ofNullable(endTimestamp)
        .ifPresent(ts -> searchListFilter.addQueryParam("endTimestamp", ts.toString()));
    Optional.ofNullable(testCaseStatus)
        .ifPresent(tcs -> searchListFilter.addQueryParam("testCaseStatus", tcs.toString()));
    Optional.ofNullable(testCaseFQN)
        .ifPresent(tcf -> searchListFilter.addQueryParam("testCaseFQN", tcf));
    Optional.ofNullable(testSuiteId)
        .ifPresent(tsi -> searchListFilter.addQueryParam("testSuiteId", tsi));
    Optional.ofNullable(entityFQN).ifPresent(ef -> searchListFilter.addQueryParam("entityFQN", ef));
    Optional.ofNullable(type).ifPresent(t -> searchListFilter.addQueryParam("testCaseType", t));
    Optional.ofNullable(dataQualityDimension)
        .ifPresent(dqd -> searchListFilter.addQueryParam("dataQualityDimension", dqd));

    ResourceContextInterface resourceContextInterface = getResourceContext(testCaseFQN);
    // Override OperationContext to change the entity to table
    // and operation from VIEW_ALL to VIEW_TESTS
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.VIEW_TESTS);

    if (latest.equals("true")) {
      return listLatestFromSearch(
          securityContext,
          fields,
          searchListFilter,
          "testCaseFQN.keyword",
          q,
          operationContext,
          resourceContextInterface);
    }
    return listInternalFromSearch(
        securityContext,
        fields,
        searchListFilter,
        limit,
        offset,
        new SearchSortFilter("timestamp", "desc", null, null),
        q,
        operationContext,
        resourceContextInterface);
  }

  @GET
  @Path("/search/latest")
  @Operation(
      operationId = "latestTestCaseResultsFromSearchService",
      summary = "Latest test case results using search service",
      description =
          "Get latest test case results from the search service. "
              + "Use `testCaseFQN` to filter the results by test case fully qualified name. "
              + "Use `testCaseStatus` to filter the results by test case status. "
              + "Use `fields` to get only necessary fields. ",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "List of test case results",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestCaseResultList.class)))
      })
  public TestCaseResult latestTestCaseResultFromSearch(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fields requested in the returned resource",
              schema = @Schema(type = "string", example = FIELDS))
          @QueryParam("fields")
          String fieldParams,
      @Parameter(
              description = "Status of the test case -- one of Success, Failed, Aborted, Queued",
              schema = @Schema(implementation = TestCaseStatus.class))
          @QueryParam("testCaseStatus")
          TestCaseStatus testCaseStatus,
      @Parameter(
              description = "FullyQualifiedName of the test case",
              schema = @Schema(type = "string"))
          @QueryParam("testCaseFQN")
          String testCaseFQN,
      @Parameter(
              description = "FullyQualifiedName of the test case",
              schema = @Schema(type = "string"))
          @QueryParam("testSuiteId")
          String testSuiteId,
      @Parameter(
              description = "search query term to use in list",
              schema = @Schema(type = "string"))
          @QueryParam("q")
          String q)
      throws IOException {
    EntityUtil.Fields fields = new EntityUtil.Fields(Set.of(""), fieldParams);
    SearchListFilter searchListFilter = new SearchListFilter();
    Optional.ofNullable(testCaseStatus)
        .ifPresent(tcs -> searchListFilter.addQueryParam("testCaseStatus", tcs.toString()));
    Optional.ofNullable(testCaseFQN)
        .ifPresent(tcf -> searchListFilter.addQueryParam("testCaseFQN", tcf));
    Optional.ofNullable(testSuiteId)
        .ifPresent(tsi -> searchListFilter.addQueryParam("testSuiteId", tsi));

    ResourceContextInterface resourceContextInterface = getResourceContext(testCaseFQN);
    // Override OperationContext to change the entity to table
    // and operation from VIEW_ALL to VIEW_TESTS
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.VIEW_TESTS);

    return super.latestInternalFromSearch(
        securityContext, fields, searchListFilter, q, operationContext, resourceContextInterface);
  }

  @PATCH
  @Path("/{fqn}/{timestamp}")
  @Operation(
      operationId = "patchTestCaseResult",
      summary = "Update a test case result",
      description = "Update an existing test case using JsonPatch.",
      externalDocs =
          @ExternalDocumentation(
              description = "JsonPatch RFC",
              url = "https://tools.ietf.org/html/rfc6902"))
  @Consumes(MediaType.APPLICATION_JSON_PATCH_JSON)
  public Response patchTestCaseResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(description = "fqn of the test case", schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Parameter(description = "Timestamp of the testCase result", schema = @Schema(type = "long"))
          @PathParam("timestamp")
          Long timestamp,
      @RequestBody(
              description = "JsonPatch with array of operations",
              content =
                  @Content(
                      mediaType = MediaType.APPLICATION_JSON_PATCH_JSON,
                      examples = {
                        @ExampleObject("[{op:remove, path:/a},{op:add, path: /b, value: val}]")
                      }))
          JsonPatch patch) {
    ResourceContextInterface resourceContext = TestCaseResourceContext.builder().name(fqn).build();
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.EDIT_TESTS);
    authorizer.authorize(securityContext, operationContext, resourceContext);
    RestUtil.PatchResponse<TestCaseResult> patchResponse =
        repository.patchTestCaseResults(
            fqn, timestamp, patch, securityContext.getUserPrincipal().getName());
    return patchResponse.toResponse();
  }

  @DELETE
  @Path("/{fqn}/{timestamp}")
  @Operation(
      operationId = "DeleteTestCaseResult",
      summary = "Delete test case result",
      description = "Delete testCase result for a testCase.",
      responses = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully deleted the TestCaseResult",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TestCase.class)))
      })
  public Response deleteTestCaseResult(
      @Context UriInfo uriInfo,
      @Context SecurityContext securityContext,
      @Parameter(
              description = "Fully qualified name of the test case",
              schema = @Schema(type = "string"))
          @PathParam("fqn")
          String fqn,
      @Parameter(description = "Timestamp of the testCase result", schema = @Schema(type = "long"))
          @PathParam("timestamp")
          Long timestamp) {
    ResourceContextInterface resourceContext = TestCaseResourceContext.builder().name(fqn).build();
    OperationContext operationContext =
        new OperationContext(Entity.TABLE, MetadataOperation.EDIT_TESTS);
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.deleteTestCaseResult(fqn, timestamp).toResponse();
  }

  private ResourceContextInterface getResourceContext(String testCaseFQN) {
    // We get the resource context for the test case based on the entity linked to it
    if (testCaseFQN == null) {
      return TestCaseResourceContext.builder().build();
    }

    TestCase testCase = Entity.getEntityByName(TEST_CASE, testCaseFQN, "", Include.ALL);
    String entityLink = testCase.getEntityLink();
    ResourceContextInterface resourceContext;
    if (entityLink != null) {
      MessageParser.EntityLink entityLinkParsed = MessageParser.EntityLink.parse(entityLink);
      resourceContext = TestCaseResourceContext.builder().entityLink(entityLinkParsed).build();
    } else {
      resourceContext = TestCaseResourceContext.builder().build();
    }
    return resourceContext;
  }
}
