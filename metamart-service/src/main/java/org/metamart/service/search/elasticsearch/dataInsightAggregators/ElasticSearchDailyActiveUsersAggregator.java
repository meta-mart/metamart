package org.metamart.service.search.elasticsearch.dataInsightAggregators;

import es.org.elasticsearch.search.aggregations.Aggregations;
import es.org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import java.time.ZonedDateTime;
import java.util.List;
import org.metamart.service.dataInsight.DailyActiveUsersAggregator;

public class ElasticSearchDailyActiveUsersAggregator
    extends DailyActiveUsersAggregator<Aggregations, Histogram, Histogram.Bucket> {
  public ElasticSearchDailyActiveUsersAggregator(Aggregations aggregations) {
    super(aggregations);
  }

  @Override
  protected Histogram getHistogramBucket(Aggregations aggregations) {
    return aggregations.get(TIMESTAMP);
  }

  @Override
  protected List<? extends Histogram.Bucket> getBuckets(Histogram histogramBucket) {
    return histogramBucket.getBuckets();
  }

  @Override
  protected long getKeyAsEpochTimestamp(Histogram.Bucket bucket) {
    return ((ZonedDateTime) bucket.getKey()).toInstant().toEpochMilli();
  }

  @Override
  protected Long getDocCount(Histogram.Bucket bucket) {
    return bucket.getDocCount();
  }
}
