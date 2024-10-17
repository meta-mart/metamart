package org.metamart.service.search.indexes;

import static org.metamart.service.Entity.QUERY;
import static org.metamart.service.search.EntityBuilderConstant.QUERY_NGRAM;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.metamart.schema.entity.data.Query;
import org.metamart.service.Entity;
import org.metamart.service.search.ParseTags;
import org.metamart.service.search.models.SearchSuggest;

public class QueryIndex implements SearchIndex {
  final Query query;

  public QueryIndex(Query query) {
    this.query = query;
  }

  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    if (query.getDisplayName() != null) {
      suggest.add(SearchSuggest.builder().input(query.getName()).weight(10).build());
    }
    return suggest;
  }

  @Override
  public Object getEntity() {
    return query;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    ParseTags parseTags = new ParseTags(Entity.getEntityTags(Entity.QUERY, query));
    Map<String, Object> commonAttributes = getCommonAttributesMap(query, Entity.QUERY);
    doc.putAll(commonAttributes);
    doc.put("tags", parseTags.getTags());
    doc.put("tier", parseTags.getTierTag());
    return doc;
  }

  public static Map<String, Float> getFields() {
    Map<String, Float> fields = SearchIndex.getDefaultFields();
    fields.put(QUERY, 10.0f);
    fields.put(QUERY_NGRAM, 1.0f);
    return fields;
  }
}
