package org.metamart.service.jdbi3;

import static org.metamart.service.Entity.TEST_DEFINITION;

import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.metamart.common.utils.CommonUtil;
import org.metamart.schema.tests.TestDefinition;
import org.metamart.service.Entity;
import org.metamart.service.resources.dqtests.TestDefinitionResource;
import org.metamart.service.util.EntityUtil;

public class TestDefinitionRepository extends EntityRepository<TestDefinition> {
  public TestDefinitionRepository() {
    super(
        TestDefinitionResource.COLLECTION_PATH,
        TEST_DEFINITION,
        TestDefinition.class,
        Entity.getCollectionDAO().testDefinitionDAO(),
        "",
        "");
  }

  @Override
  public void setFields(TestDefinition entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void clearFields(TestDefinition entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void prepare(TestDefinition entity, boolean update) {
    // validate test platforms
    if (CommonUtil.nullOrEmpty(entity.getTestPlatforms())) {
      throw new IllegalArgumentException("testPlatforms must not be empty");
    }
  }

  @Override
  public void storeEntity(TestDefinition entity, boolean update) {
    store(entity, update);
  }

  @Override
  public void storeRelationships(TestDefinition entity) {
    // No relationships to store beyond what is stored in the super class
  }

  @Override
  public EntityUpdater getUpdater(
      TestDefinition original, TestDefinition updated, Operation operation) {
    return new TestDefinitionUpdater(original, updated, operation);
  }

  public class TestDefinitionUpdater extends EntityUpdater {
    public TestDefinitionUpdater(
        TestDefinition original, TestDefinition updated, Operation operation) {
      super(original, updated, operation);
    }

    @Transaction
    @Override
    public void entitySpecificUpdate() {
      recordChange("testPlatforms", original.getTestPlatforms(), updated.getTestPlatforms());
      recordChange(
          "supportedDataTypes", original.getSupportedDataTypes(), updated.getSupportedDataTypes());
      recordChange(
          "parameterDefinition",
          original.getParameterDefinition(),
          updated.getParameterDefinition());
    }
  }
}
