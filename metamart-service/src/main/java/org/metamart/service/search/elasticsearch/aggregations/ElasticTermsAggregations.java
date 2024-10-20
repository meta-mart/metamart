package org.metamart.service.search.elasticsearch.aggregations;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;

import es.org.elasticsearch.search.aggregations.AggregationBuilder;
import es.org.elasticsearch.search.aggregations.AggregationBuilders;
import es.org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import es.org.elasticsearch.search.aggregations.bucket.terms.IncludeExclude;
import es.org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.metamart.service.search.SearchAggregationNode;

@Setter
@Getter
public class ElasticTermsAggregations implements ElasticAggregations {
  static final String aggregationType = "terms";
  AggregationBuilder elasticAggregationBuilder;

  @Override
  public void createAggregation(SearchAggregationNode node) {
    String[] includes = null;
    int size = -1;
    Map<String, String> params = node.getValue();
    String includesStr = params.get("include");
    if (!nullOrEmpty(includesStr)) includes = includesStr.split(",");
    String sizeStr = params.get("size");
    if (!nullOrEmpty(sizeStr)) size = Integer.parseInt(params.get("size"));
    TermsAggregationBuilder termsAggregationBuilder =
        AggregationBuilders.terms(node.getName()).field(params.get("field"));

    if (size > 0) termsAggregationBuilder.size(size);
    if (!nullOrEmpty(includes)) {
      IncludeExclude includeExclude = new IncludeExclude(includes, null);
      termsAggregationBuilder.includeExclude(includeExclude);
    }
    setElasticAggregationBuilder(termsAggregationBuilder);
  }

  @Override
  public void setSubAggregation(PipelineAggregationBuilder aggregation) {
    if (elasticAggregationBuilder != null) {
      elasticAggregationBuilder.subAggregation(aggregation);
    }
  }

  @Override
  public void setSubAggregation(AggregationBuilder aggregation) {
    if (elasticAggregationBuilder != null) {
      elasticAggregationBuilder.subAggregation(aggregation);
    }
  }
}
