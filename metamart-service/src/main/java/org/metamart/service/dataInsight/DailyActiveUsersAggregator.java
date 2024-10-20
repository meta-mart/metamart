package org.metamart.service.dataInsight;

import java.util.ArrayList;
import java.util.List;
import org.metamart.schema.dataInsight.type.DailyActiveUsers;

public abstract class DailyActiveUsersAggregator<A, H, B>
    implements DataInsightAggregatorInterface {
  private final A aggregations;

  protected DailyActiveUsersAggregator(A aggregations) {
    this.aggregations = aggregations;
  }

  @Override
  public List<Object> aggregate() {
    H histogramBucket = getHistogramBucket(this.aggregations);
    List<Object> data = new ArrayList<>();
    for (B bucket : getBuckets(histogramBucket)) {
      Long timestamp = getKeyAsEpochTimestamp(bucket);
      long activeUsers = getDocCount(bucket);

      data.add(new DailyActiveUsers().withTimestamp(timestamp).withActiveUsers((int) activeUsers));
    }
    return data;
  }

  protected abstract H getHistogramBucket(A aggregations);

  protected abstract List<? extends B> getBuckets(H histogramBucket);

  protected abstract long getKeyAsEpochTimestamp(B bucket);

  protected abstract Long getDocCount(B bucket);
}
