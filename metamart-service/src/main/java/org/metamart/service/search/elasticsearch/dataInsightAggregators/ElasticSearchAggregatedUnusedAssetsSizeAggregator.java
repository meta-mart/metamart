package org.metamart.service.search.elasticsearch.dataInsightAggregators;

import es.org.elasticsearch.search.aggregations.Aggregations;
import es.org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import es.org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import es.org.elasticsearch.search.aggregations.metrics.Sum;
import java.time.ZonedDateTime;
import java.util.List;
import org.metamart.service.dataInsight.AggregatedUnusedAssetsSizeAggregator;

public class ElasticSearchAggregatedUnusedAssetsSizeAggregator
    extends AggregatedUnusedAssetsSizeAggregator<Aggregations, Histogram, Bucket, Sum> {
  public ElasticSearchAggregatedUnusedAssetsSizeAggregator(Aggregations aggregations) {
    super(aggregations);
  }

  @Override
  protected Histogram getHistogramBucket(Aggregations aggregations) {
    return aggregations.get(TIMESTAMP);
  }

  @Override
  protected List<? extends Bucket> getBuckets(Histogram histogramBucket) {
    return histogramBucket.getBuckets();
  }

  @Override
  protected long getKeyAsEpochTimestamp(Bucket bucket) {
    return ((ZonedDateTime) bucket.getKey()).toInstant().toEpochMilli();
  }

  @Override
  protected Sum getAggregations(Bucket bucket, String key) {
    return bucket.getAggregations().get(key);
  }

  @Override
  protected Double getValue(Sum aggregations) {
    return aggregations != null ? aggregations.getValue() : null;
  }
}
