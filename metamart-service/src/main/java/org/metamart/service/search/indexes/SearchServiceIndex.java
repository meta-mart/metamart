package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.services.SearchService;
import org.metamart.service.Entity;
import org.metamart.service.search.models.SearchSuggest;

public record SearchServiceIndex(SearchService searchService) implements SearchIndex {

  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(searchService.getName()).weight(5).build());
    suggest.add(
        SearchSuggest.builder().input(searchService.getFullyQualifiedName()).weight(5).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return searchService;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes =
        getCommonAttributesMap(searchService, Entity.SEARCH_SERVICE);
    doc.putAll(commonAttributes);
    return doc;
  }
}
