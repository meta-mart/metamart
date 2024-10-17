package org.metamart.service.migration.utils.V117;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jdbi.v3.core.Handle;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.jdbi3.ListFilter;
import org.metamart.service.jdbi3.TableRepository;
import org.metamart.service.jdbi3.TestCaseRepository;
import org.metamart.service.resources.databases.DatasourceConfig;
import org.metamart.service.resources.feeds.MessageParser;
import org.metamart.service.util.EntityUtil;

public class MigrationUtil {
  private MigrationUtil() {
    /* Cannot create object  util class*/
  }

  private static final String MYSQL_LIST_TABLE_FQNS =
      "SELECT JSON_UNQUOTE(JSON_EXTRACT(json, '$.fullyQualifiedName')) FROM table_entity";
  private static final String POSTGRES_LIST_TABLE_FQNS =
      "SELECT json #>> '{fullyQualifiedName}' FROM table_entity";

  public static void fixTestCases(Handle handle, CollectionDAO collectionDAO) {
    TestCaseRepository testCaseRepository =
        (TestCaseRepository) Entity.getEntityRepository(Entity.TEST_CASE);
    TableRepository tableRepository = (TableRepository) Entity.getEntityRepository(Entity.TABLE);
    List<TestCase> testCases =
        testCaseRepository.listAll(
            new EntityUtil.Fields(Set.of("id")), new ListFilter(Include.ALL));

    List<String> fqnList;
    if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
      fqnList = handle.createQuery(MYSQL_LIST_TABLE_FQNS).mapTo(String.class).list();
    } else {
      fqnList = handle.createQuery(POSTGRES_LIST_TABLE_FQNS).mapTo(String.class).list();
    }
    Map<String, String> tableMap = new HashMap<>();
    for (String fqn : fqnList) {
      tableMap.put(fqn.toLowerCase(), fqn);
    }

    for (TestCase testCase : testCases) {
      // Create New Executable Test Suites
      MessageParser.EntityLink entityLink =
          MessageParser.EntityLink.parse(testCase.getEntityLink());
      String fqn = entityLink.getEntityFQN();
      Table table = tableRepository.findByNameOrNull(fqn, Include.ALL);
      if (table == null) {
        String findTableFQN = tableMap.get(fqn.toLowerCase());
        MessageParser.EntityLink newEntityLink =
            new MessageParser.EntityLink(entityLink.getEntityType(), findTableFQN);
        testCase.setEntityLink(newEntityLink.getLinkString());
        testCase.setEntityFQN(findTableFQN);
        collectionDAO.testCaseDAO().update(testCase);
      }
    }
  }
}
