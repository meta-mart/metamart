package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.services.MessagingService;
import org.metamart.service.Entity;
import org.metamart.service.search.models.SearchSuggest;

public record MessagingServiceIndex(MessagingService messagingService) implements SearchIndex {

  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(messagingService.getName()).weight(5).build());
    suggest.add(
        SearchSuggest.builder().input(messagingService.getFullyQualifiedName()).weight(5).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return messagingService;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes =
        getCommonAttributesMap(messagingService, Entity.MESSAGING_SERVICE);
    doc.putAll(commonAttributes);
    return doc;
  }
}
