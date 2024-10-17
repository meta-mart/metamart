package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.tests.TestSuite;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.search.ParseTags;
import org.metamart.service.search.SearchIndexUtils;
import org.metamart.service.search.models.SearchSuggest;

public record TestSuiteIndex(TestSuite testSuite) implements SearchIndex {
  @Override
  public Object getEntity() {
    return testSuite;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(testSuite.getFullyQualifiedName()).weight(5).build());
    suggest.add(SearchSuggest.builder().input(testSuite.getName()).weight(10).build());
    doc.put(
        "fqnParts",
        getFQNParts(
            testSuite.getFullyQualifiedName(),
            suggest.stream().map(SearchSuggest::getInput).toList()));
    doc.put("suggest", suggest);
    doc.put("entityType", Entity.TEST_SUITE);
    doc.put("owners", getEntitiesWithDisplayName(testSuite.getOwners()));
    doc.put("followers", SearchIndexUtils.parseFollowers(testSuite.getFollowers()));
    ParseTags parseTags = new ParseTags(Entity.getEntityTags(Entity.TEST_SUITE, testSuite));
    doc.put("tags", parseTags.getTags());
    setParentRelationships(doc, testSuite);
    return doc;
  }

  private void setParentRelationships(Map<String, Object> doc, TestSuite testSuite) {
    // denormalize the parent relationships for search
    EntityReference entityReference = testSuite.getExecutableEntityReference();
    if (entityReference == null) return;
    addTestSuiteParentEntityRelations(entityReference, doc);
  }

  static void addTestSuiteParentEntityRelations(
      EntityReference testSuiteRef, Map<String, Object> doc) {
    if (testSuiteRef.getType().equals(Entity.TABLE)) {
      Table table = Entity.getEntity(testSuiteRef, "", Include.ALL);
      doc.put("table", table.getEntityReference());
      doc.put("database", table.getDatabase());
      doc.put("databaseSchema", table.getDatabaseSchema());
      doc.put("service", table.getService());
    }
  }
}
