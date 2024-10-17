package org.metamart.service.search.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.services.PipelineService;
import org.metamart.service.Entity;
import org.metamart.service.search.models.SearchSuggest;

public record PipelineServiceIndex(PipelineService pipelineService) implements SearchIndex {
  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(SearchSuggest.builder().input(pipelineService.getName()).weight(5).build());
    suggest.add(
        SearchSuggest.builder().input(pipelineService.getFullyQualifiedName()).weight(5).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return pipelineService;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    Map<String, Object> commonAttributes =
        getCommonAttributesMap(pipelineService, Entity.PIPELINE_SERVICE);
    doc.putAll(commonAttributes);
    return doc;
  }
}
