package org.metamart.service.resources.automations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.metamart.service.util.TestUtils.ADMIN_AUTH_HEADERS;
import static org.metamart.service.util.TestUtils.assertListNotNull;
import static org.metamart.service.util.TestUtils.assertListNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.metamart.schema.api.services.DatabaseConnection;
import org.metamart.schema.entity.automations.CreateWorkflow;
import org.metamart.schema.entity.automations.TestServiceConnectionRequest;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.entity.automations.WorkflowStatus;
import org.metamart.schema.entity.automations.WorkflowType;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.services.connections.database.MysqlConnection;
import org.metamart.schema.services.connections.database.common.basicAuth;
import org.metamart.service.Entity;
import org.metamart.service.resources.EntityResourceTest;
import org.metamart.service.resources.automations.WorkflowResource.WorkflowList;
import org.metamart.service.util.ResultList;

public class WorkflowResourceTest extends EntityResourceTest<Workflow, CreateWorkflow> {

  public WorkflowResourceTest() {
    super(
        Entity.WORKFLOW,
        Workflow.class,
        WorkflowList.class,
        "automations/workflows",
        WorkflowResource.FIELDS);
  }

  @Override
  public CreateWorkflow createRequest(String name) {
    return new CreateWorkflow()
        .withName(name)
        .withDescription(name)
        .withWorkflowType(WorkflowType.TEST_CONNECTION)
        .withRequest(
            new TestServiceConnectionRequest()
                .withServiceType(ServiceType.DATABASE)
                .withConnectionType("Mysql")
                .withConnection(
                    new DatabaseConnection()
                        .withConfig(
                            new MysqlConnection()
                                .withHostPort("mysql:3306")
                                .withUsername("metamart_user")
                                .withAuthType(
                                    new basicAuth().withPassword("metamart_password")))));
  }

  @Test
  void get_listWorkflowsFiltered(TestInfo test) throws IOException {
    CreateWorkflow createWorkflowTest =
        createRequest(test.getDisplayName()).withStatus(WorkflowStatus.SUCCESSFUL);
    createAndCheckEntity(createWorkflowTest, ADMIN_AUTH_HEADERS);

    CreateWorkflow createWorkflowTest2 =
        createRequest(test.getDisplayName() + "2").withStatus(WorkflowStatus.RUNNING);
    createAndCheckEntity(createWorkflowTest2, ADMIN_AUTH_HEADERS);

    // Filter by status
    Map<String, String> params = new HashMap<>();
    params.put("workflowStatus", WorkflowStatus.SUCCESSFUL.value());
    ResultList<Workflow> resList = listEntities(params, ADMIN_AUTH_HEADERS);
    assertEquals(1, resList.getData().size());
  }

  @Override
  public void validateCreatedEntity(
      Workflow createdEntity, CreateWorkflow request, Map<String, String> authHeaders) {
    assertEquals(request.getName(), createdEntity.getName());
    assertEquals(request.getWorkflowType(), createdEntity.getWorkflowType());
    assertNotNull(createdEntity.getRequest());
    assertNotNull(createdEntity.getMetaMartServerConnection());
  }

  @Override
  public void compareEntities(
      Workflow expected, Workflow updated, Map<String, String> authHeaders) {
    assertEquals(expected.getName(), updated.getName());
    assertEquals(expected.getWorkflowType(), updated.getWorkflowType());
    assertEquals(expected.getStatus(), updated.getStatus());
  }

  @Override
  public Workflow validateGetWithDifferentFields(Workflow entity, boolean byName)
      throws HttpResponseException {
    String fields = "";
    entity =
        byName
            ? getEntityByName(entity.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(entity.getId(), null, ADMIN_AUTH_HEADERS);
    assertListNull(entity.getOwners());
    fields = "owners";
    entity =
        byName
            ? getEntityByName(entity.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(entity.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNotNull(entity.getOwners());
    return entity;
  }

  @Override
  public void assertFieldChange(String fieldName, Object expected, Object actual) {
    assertCommonFieldChange(fieldName, expected, actual);
  }
}
