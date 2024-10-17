package org.metamart.service.dataInsight;

import java.util.List;
import org.metamart.schema.dataInsight.DataInsightChartResult;

public interface DataInsightAggregatorInterface {
  String TIMESTAMP = "timestamp";

  default DataInsightChartResult process(DataInsightChartResult.DataInsightChartType chartType) {
    List<Object> data = this.aggregate();
    return new DataInsightChartResult().withData(data).withChartType(chartType);
  }

  List<Object> aggregate();
}
