package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.data.Database;
import org.metamart.service.Entity;
import org.metamart.service.search.models.SearchSuggest;

public record DatabaseIndex(Database database) implements SearchIndex {
  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(database.getName()).weight(5).build());
    suggest.add(SearchSuggest.builder().input(database.getFullyQualifiedName()).weight(5).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return database;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes = getCommonAttributesMap(database, Entity.DATABASE);
    doc.putAll(commonAttributes);
    return doc;
  }
}
