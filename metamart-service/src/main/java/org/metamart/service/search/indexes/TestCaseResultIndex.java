package org.metamart.service.search.indexes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.tests.TestDefinition;
import org.metamart.schema.tests.type.TestCaseResult;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.resources.feeds.MessageParser;
import org.metamart.service.search.SearchIndexUtils;
import org.metamart.service.util.JsonUtils;

public record TestCaseResultIndex(TestCaseResult testCaseResult) implements SearchIndex {
  private static final Set<String> excludeFields = Set.of("changeDescription", "failedRowsSample");

  @Override
  public Object getEntity() {
    return testCaseResult;
  }

  @Override
  public void removeNonIndexableFields(Map<String, Object> esDoc) {
    SearchIndex.super.removeNonIndexableFields(esDoc);
    List<Map<String, Object>> testSuites = (List<Map<String, Object>>) esDoc.get("testSuites");
    Map<String, Object> testDefinition = (Map<String, Object>) esDoc.get("testDefinition");
    Map<String, Object> testCase = (Map<String, Object>) esDoc.get("testCase");
    if (testSuites != null) {
      for (Map<String, Object> testSuite : testSuites) {
        SearchIndexUtils.removeNonIndexableFields(testSuite, excludeFields);
      }
    }
    if (testCase != null) SearchIndexUtils.removeNonIndexableFields(testCase, excludeFields);
    if (testDefinition != null)
      SearchIndexUtils.removeNonIndexableFields(testDefinition, excludeFields);
  }

  @Override
  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> esDoc) {
    TestCase testCase =
        Entity.getEntityByName(Entity.TEST_CASE, testCaseResult.getTestCaseFQN(), "*", Include.ALL);
    TestDefinition testDefinition =
        Entity.getEntityByName(
            Entity.TEST_DEFINITION,
            testCase.getTestDefinition().getFullyQualifiedName(),
            "*",
            Include.ALL);
    // we set testSuites and testSuite at the root for cascade deletion purposes
    Map<String, Object> testCaseMap = JsonUtils.getMap(testCase);
    esDoc.put("testSuites", testCaseMap.get("testSuites"));
    esDoc.put("testSuite", testCaseMap.get("testSuite"));
    testCaseMap
        .keySet()
        .removeAll(
            Set.of(
                "testSuites",
                "testSuite",
                "testCaseResult",
                "testDefinition")); // remove testCase fields not needed
    esDoc.put("testCase", testCaseMap);
    esDoc.put("@timestamp", testCaseResult.getTimestamp());
    esDoc.put("testDefinition", JsonUtils.getMap(testDefinition));
    setParentRelationships(testCase, esDoc);
    return esDoc;
  }

  private void setParentRelationships(TestCase testCase, Map<String, Object> esDoc) {
    // denormalize the parent relationships for search
    MessageParser.EntityLink entityLink = MessageParser.EntityLink.parse(testCase.getEntityLink());
    String entityType = entityLink.getEntityType();
    if (entityType.equals(Entity.TABLE)) {
      // Can move this to a switch statement if we have more entity types
      setTableEntityParentRelations(entityLink, esDoc);
    }
  }

  private void setTableEntityParentRelations(
      MessageParser.EntityLink entityLink, Map<String, Object> esDoc) {
    Table table = Entity.getEntityByName(Entity.TABLE, entityLink.getEntityFQN(), "*", Include.ALL);
    EntityReference databaseSchemaReference = table.getDatabaseSchema();
    EntityReference databaseReference = table.getDatabase();
    EntityReference serviceReference = table.getService();
    EntityReference tableReference = table.getEntityReference();
    esDoc.put("database", databaseReference);
    esDoc.put("databaseSchema", databaseSchemaReference);
    esDoc.put("service", serviceReference);
    esDoc.put("table", tableReference);
  }

  public static Map<String, Float> getFields() {
    Map<String, Float> fields = new HashMap<>();
    fields.put("testCase.FullyQualifiedName", 10.0f);
    fields.put("testCase.displayName", 15.0f);
    fields.put("testCase.name", 10.0f);
    fields.put("testCase.description", 5.0f);
    return fields;
  }
}
