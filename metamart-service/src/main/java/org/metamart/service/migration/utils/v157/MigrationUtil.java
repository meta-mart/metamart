package org.metamart.service.migration.utils.v157;

import static org.metamart.service.Entity.TEST_CASE;
import static org.metamart.service.Entity.TEST_DEFINITION;

import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.tests.TestCaseParameterValue;
import org.metamart.schema.tests.TestDefinition;
import org.metamart.schema.type.Relationship;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.util.JsonUtils;

@Slf4j
public class MigrationUtil {
  private static final String TABLE_DIFF = "tableDiff";

  public static void migrateTableDiffParams(
      Handle handle,
      CollectionDAO daoCollection,
      AuthenticationConfiguration config,
      boolean postgres) {
    int pageSize = 1000;
    int offset = 0;
    while (true) {
      List<String> jsons = daoCollection.testCaseDAO().listAfterWithOffset(pageSize, offset);
      if (jsons.isEmpty()) {
        break;
      }
      offset += pageSize;
      for (String json : jsons) {
        TestCase testCase = JsonUtils.readValue(json, TestCase.class);
        TestDefinition td = getTestDefinition(daoCollection, testCase);
        if (Objects.nonNull(td) && Objects.equals(td.getName(), TABLE_DIFF)) {
          LOG.debug("Adding caseSensitiveColumns=true table diff test case: {}", testCase.getId());
          testCase
              .getParameterValues()
              .add(new TestCaseParameterValue().withName("caseSensitiveColumns").withValue("true"));
          daoCollection.testCaseDAO().update(testCase);
        }
      }
    }
  }

  public static TestDefinition getTestDefinition(CollectionDAO dao, TestCase testCase) {
    List<CollectionDAO.EntityRelationshipRecord> records =
        dao.relationshipDAO()
            .findFrom(
                testCase.getId(), TEST_CASE, Relationship.CONTAINS.ordinal(), TEST_DEFINITION);
    if (records.size() > 1) {
      throw new RuntimeException(
          "Multiple test definitions found for test case: " + testCase.getId());
    }
    if (records.isEmpty()) {
      return null;
    }
    return dao.testDefinitionDAO().findEntityById(records.get(0).getId());
  }
}
