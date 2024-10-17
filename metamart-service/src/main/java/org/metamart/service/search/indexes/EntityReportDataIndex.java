package org.metamart.service.search.indexes;

import java.util.Map;
import org.metamart.schema.analytics.ReportData;
import org.metamart.service.util.JsonUtils;

public record EntityReportDataIndex(ReportData reportData) implements SearchIndex {

  @Override
  public Object getEntity() {
    return reportData;
  }

  @Override
  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> esDoc) {
    Map<String, Object> doc = JsonUtils.getMap(reportData);
    doc.put("entityType", "entityReportData");
    return doc;
  }
}
