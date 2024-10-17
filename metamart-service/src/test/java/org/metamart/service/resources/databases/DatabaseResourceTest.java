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

package org.metamart.service.resources.databases;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.apache.commons.lang.StringEscapeUtils.escapeCsv;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.metamart.common.utils.CommonUtil.listOf;
import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.csv.CsvUtil.recordToString;
import static org.metamart.csv.EntityCsv.entityNotFound;
import static org.metamart.csv.EntityCsvTest.assertRows;
import static org.metamart.csv.EntityCsvTest.assertSummary;
import static org.metamart.csv.EntityCsvTest.createCsv;
import static org.metamart.csv.EntityCsvTest.getFailedRecord;
import static org.metamart.csv.EntityCsvTest.getSuccessRecord;
import static org.metamart.service.util.EntityUtil.getFqn;
import static org.metamart.service.util.TestUtils.ADMIN_AUTH_HEADERS;
import static org.metamart.service.util.TestUtils.assertListNotEmpty;
import static org.metamart.service.util.TestUtils.assertListNotNull;
import static org.metamart.service.util.TestUtils.assertListNull;
import static org.metamart.service.util.TestUtils.assertResponseContains;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.metamart.csv.EntityCsv;
import org.metamart.schema.api.data.CreateDatabase;
import org.metamart.schema.api.data.CreateDatabaseSchema;
import org.metamart.schema.entity.data.Database;
import org.metamart.schema.entity.data.DatabaseSchema;
import org.metamart.schema.type.ApiStatus;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.csv.CsvImportResult;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.DatabaseRepository.DatabaseCsv;
import org.metamart.service.jdbi3.DatabaseSchemaRepository.DatabaseSchemaCsv;
import org.metamart.service.resources.EntityResourceTest;
import org.metamart.service.resources.databases.DatabaseResource.DatabaseList;
import org.metamart.service.util.FullyQualifiedName;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.ResultList;
import org.metamart.service.util.TestUtils;

@Slf4j
public class DatabaseResourceTest extends EntityResourceTest<Database, CreateDatabase> {
  public DatabaseResourceTest() {
    super(
        Entity.DATABASE, Database.class, DatabaseList.class, "databases", DatabaseResource.FIELDS);
    supportedNameCharacters = "_'+#- .()$" + EntityResourceTest.RANDOM_STRING_GENERATOR.generate(1);
  }

  @Test
  void post_databaseFQN_as_admin_200_OK(TestInfo test) throws IOException {
    // Create database with different optional fields
    CreateDatabase create =
        createRequest(test).withService(SNOWFLAKE_REFERENCE.getFullyQualifiedName());
    Database db = createAndCheckEntity(create, ADMIN_AUTH_HEADERS);
    String expectedFQN =
        FullyQualifiedName.build(SNOWFLAKE_REFERENCE.getFullyQualifiedName(), create.getName());
    assertEquals(expectedFQN, db.getFullyQualifiedName());
  }

  @Test
  void post_databaseWithoutRequiredService_4xx(TestInfo test) {
    CreateDatabase create = createRequest(test).withService(null);
    assertResponseContains(
        () -> createEntity(create, ADMIN_AUTH_HEADERS), BAD_REQUEST, "service must not be null");
  }

  @Test
  void post_databaseWithDifferentService_200_ok(TestInfo test) throws IOException {
    EntityReference[] differentServices = {
      MYSQL_REFERENCE, REDSHIFT_REFERENCE, BIGQUERY_REFERENCE, SNOWFLAKE_REFERENCE
    };

    // Create database for each service and test APIs
    for (EntityReference service : differentServices) {
      createAndCheckEntity(
          createRequest(test).withService(service.getFullyQualifiedName()), ADMIN_AUTH_HEADERS);

      // List databases by filtering on service name and ensure right databases in the response
      Map<String, String> queryParams = new HashMap<>();
      queryParams.put("service", service.getName());

      ResultList<Database> list = listEntities(queryParams, ADMIN_AUTH_HEADERS);
      for (Database db : list.getData()) {
        assertEquals(service.getName(), db.getService().getName());
      }
    }
  }

  @Test
  @SneakyThrows
  void testImportInvalidCsv() {
    Database database = createEntity(createRequest("invalidCsv"), ADMIN_AUTH_HEADERS);
    String databaseName = database.getFullyQualifiedName();
    DatabaseSchemaResourceTest schemaTest = new DatabaseSchemaResourceTest();
    CreateDatabaseSchema createSchema =
        schemaTest.createRequest("s1").withDatabase(database.getFullyQualifiedName());
    schemaTest.createEntity(createSchema, ADMIN_AUTH_HEADERS);

    // Headers: name, displayName, description, owner, tags, retentionPeriod, sourceUrl, domain
    // Update databaseSchema with invalid tags field
    String resultsHeader = recordToString(EntityCsv.getResultHeaders(DatabaseCsv.HEADERS));
    String record = "s1,dsp1,dsc1,,Tag.invalidTag,,,,,";
    String csv = createCsv(DatabaseCsv.HEADERS, listOf(record), null);
    CsvImportResult result = importCsv(databaseName, csv, false);
    assertSummary(result, ApiStatus.PARTIAL_SUCCESS, 2, 1, 1);
    String[] expectedRows =
        new String[] {
          resultsHeader, getFailedRecord(record, entityNotFound(4, "tag", "Tag.invalidTag"))
        };
    assertRows(result, expectedRows);

    //  invalid tag it will give error.
    record = "non-existing,dsp1,dsc1,,Tag.invalidTag,,,,,";
    csv = createCsv(DatabaseSchemaCsv.HEADERS, listOf(record), null);
    result = importCsv(databaseName, csv, false);
    assertSummary(result, ApiStatus.PARTIAL_SUCCESS, 2, 1, 1);
    expectedRows =
        new String[] {
          resultsHeader, getFailedRecord(record, entityNotFound(4, "tag", "Tag.invalidTag"))
        };
    assertRows(result, expectedRows);

    // databaseSchema will be created if it does not exist
    String schemaFqn = FullyQualifiedName.add(database.getFullyQualifiedName(), "non-existing");
    record = "non-existing,dsp1,dsc1,,,,,,,";
    csv = createCsv(DatabaseSchemaCsv.HEADERS, listOf(record), null);
    result = importCsv(databaseName, csv, false);
    assertSummary(result, ApiStatus.SUCCESS, 2, 2, 0);
    expectedRows = new String[] {resultsHeader, getSuccessRecord(record, "Entity created")};
    assertRows(result, expectedRows);
    DatabaseSchema createdSchema = schemaTest.getEntityByName(schemaFqn, "id", ADMIN_AUTH_HEADERS);
    assertEquals(schemaFqn, createdSchema.getFullyQualifiedName());
  }

  @Test
  void testImportExport() throws IOException {
    String user1 = USER1.getName();
    Database database = createEntity(createRequest("importExportTest"), ADMIN_AUTH_HEADERS);
    DatabaseSchemaResourceTest schemaTest = new DatabaseSchemaResourceTest();
    CreateDatabaseSchema createSchema =
        schemaTest.createRequest("s1").withDatabase(database.getFullyQualifiedName());
    schemaTest.createEntity(createSchema, ADMIN_AUTH_HEADERS);

    // Headers: name, displayName, description, owner, tags, glossaryTerms, tiers, retentionPeriod,
    // sourceUrl, domain
    // Update terms with change in description
    String record =
        String.format(
            "s1,dsp1,new-dsc1,user:%s,,,Tier.Tier1,P23DT23H,http://test.com,%s",
            user1, escapeCsv(DOMAIN.getFullyQualifiedName()));

    // Update created entity with changes
    importCsvAndValidate(
        database.getFullyQualifiedName(), DatabaseCsv.HEADERS, null, listOf(record));
  }

  @Override
  public Database validateGetWithDifferentFields(Database database, boolean byName)
      throws HttpResponseException {
    // Add a schema if it already does not exist
    if (nullOrEmpty(database.getDatabaseSchemas())) {
      DatabaseSchemaResourceTest databaseSchemaResourceTest = new DatabaseSchemaResourceTest();
      CreateDatabaseSchema create =
          databaseSchemaResourceTest
              .createRequest("schema", "", "", null)
              .withDatabase(getFqn(database));
      databaseSchemaResourceTest.createEntity(create, ADMIN_AUTH_HEADERS);
    }

    String fields = "";
    database =
        byName
            ? getEntityByName(database.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(database.getId(), fields, ADMIN_AUTH_HEADERS);
    assertListNotNull(database.getService(), database.getServiceType());
    assertListNull(
        database.getOwners(),
        database.getDatabaseSchemas(),
        database.getUsageSummary(),
        database.getLocation());

    fields = "owners,databaseSchemas,usageSummary,location,tags";
    database =
        byName
            ? getEntityByName(database.getFullyQualifiedName(), fields, ADMIN_AUTH_HEADERS)
            : getEntity(database.getId(), fields, ADMIN_AUTH_HEADERS);

    assertListNotNull(database.getService(), database.getServiceType());
    // Fields usageSummary and location are not set during creation - tested elsewhere
    TestUtils.validateEntityReferences(database.getDatabaseSchemas(), true);
    assertListNotEmpty(database.getDatabaseSchemas());
    // Checks for other owner, tags, and followers is done in the base class
    return database;
  }

  @Override
  public CreateDatabase createRequest(String name) {
    return new CreateDatabase().withName(name).withService(getContainer().getFullyQualifiedName());
  }

  @Override
  public EntityReference getContainer() {
    return SNOWFLAKE_REFERENCE;
  }

  @Override
  public EntityReference getContainer(Database entity) {
    return entity.getService();
  }

  @Override
  public void validateCreatedEntity(
      Database database, CreateDatabase createRequest, Map<String, String> authHeaders) {
    // Validate service
    assertNotNull(database.getServiceType());
    assertReference(createRequest.getService(), database.getService());
    assertEquals(
        FullyQualifiedName.build(database.getService().getName(), database.getName()),
        database.getFullyQualifiedName());
  }

  @Override
  public void compareEntities(
      Database expected, Database updated, Map<String, String> authHeaders) {
    assertReference(expected.getService(), updated.getService());
    assertEquals(
        FullyQualifiedName.build(updated.getService().getName(), updated.getName()),
        updated.getFullyQualifiedName());
  }

  @Override
  public void assertFieldChange(String fieldName, Object expected, Object actual) {
    if (fieldName.endsWith("owners") && (expected != null && actual != null)) {
      @SuppressWarnings("unchecked")
      List<EntityReference> expectedOwners =
          expected instanceof List
              ? (List<EntityReference>) expected
              : JsonUtils.readObjects(expected.toString(), EntityReference.class);
      List<EntityReference> actualOwners =
          JsonUtils.readObjects(actual.toString(), EntityReference.class);
      assertOwners(expectedOwners, actualOwners);
    } else {
      assertCommonFieldChange(fieldName, expected, actual);
    }
  }
}
