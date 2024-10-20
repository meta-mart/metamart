package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.data.DatabaseSchema;
import org.metamart.service.Entity;
import org.metamart.service.search.models.SearchSuggest;

public record DatabaseSchemaIndex(DatabaseSchema databaseSchema) implements SearchIndex {

  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(databaseSchema.getName()).weight(5).build());
    suggest.add(
        SearchSuggest.builder().input(databaseSchema.getFullyQualifiedName()).weight(5).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return databaseSchema;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes =
        getCommonAttributesMap(databaseSchema, Entity.DATABASE_SCHEMA);
    doc.putAll(commonAttributes);
    return doc;
  }
}
