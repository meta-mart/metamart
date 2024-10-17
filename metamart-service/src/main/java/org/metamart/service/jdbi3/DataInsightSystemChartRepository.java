package org.metamart.service.jdbi3;

import static org.metamart.service.Entity.DATA_INSIGHT_CUSTOM_CHART;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.metamart.schema.dataInsight.custom.DataInsightCustomChart;
import org.metamart.schema.dataInsight.custom.DataInsightCustomChartResultList;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.search.SearchClient;
import org.metamart.service.util.EntityUtil;

public class DataInsightSystemChartRepository extends EntityRepository<DataInsightCustomChart> {
  public static final String COLLECTION_PATH = "/v1/analytics/dataInsights/system/charts";
  private static final SearchClient searchClient = Entity.getSearchRepository().getSearchClient();
  public static final String TIMESTAMP_FIELD = "@timestamp";

  public static final String DI_SEARCH_INDEX = "di-data-assets-*";

  public static final String FORMULA_FUNC_REGEX =
      "\\b(count|sum|min|max|avg|unique)+\\((k='([^']*)')?,?\\s*(q='([^']*)')?\\)?";

  public static final String NUMERIC_VALIDATION_REGEX = "[\\d\\.+-\\/\\*\\(\\)\s]+";

  public DataInsightSystemChartRepository() {
    super(
        COLLECTION_PATH,
        DATA_INSIGHT_CUSTOM_CHART,
        DataInsightCustomChart.class,
        Entity.getCollectionDAO().dataInsightCustomChartDAO(),
        "",
        "");
  }

  @Override
  public void setFields(DataInsightCustomChart entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void clearFields(DataInsightCustomChart entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void prepare(DataInsightCustomChart entity, boolean update) {
    /* Nothing to do */
  }

  @Override
  public void storeEntity(DataInsightCustomChart entity, boolean update) {
    store(entity, update);
  }

  @Override
  public void storeRelationships(DataInsightCustomChart entity) {
    // No relationships to store beyond what is stored in the super class
  }

  public DataInsightCustomChartResultList getPreviewData(
      DataInsightCustomChart chart, long startTimestamp, long endTimestamp, String filter)
      throws IOException {
    if (chart.getChartDetails() != null && filter != null) {
      ((LinkedHashMap<String, Object>) chart.getChartDetails()).put("filter", filter);
    }
    return getPreviewData(chart, startTimestamp, endTimestamp);
  }

  public DataInsightCustomChartResultList getPreviewData(
      DataInsightCustomChart chart, long startTimestamp, long endTimestamp) throws IOException {
    return searchClient.buildDIChart(chart, startTimestamp, endTimestamp);
  }

  public Map<String, DataInsightCustomChartResultList> listChartData(
      String chartNames, long startTimestamp, long endTimestamp, String filter) throws IOException {
    HashMap<String, DataInsightCustomChartResultList> result = new HashMap<>();
    if (chartNames == null) {
      return result;
    }

    for (String chartName : chartNames.split(",")) {
      DataInsightCustomChart chart =
          Entity.getEntityByName(DATA_INSIGHT_CUSTOM_CHART, chartName, "", Include.NON_DELETED);

      if (chart != null) {
        if (chart.getChartDetails() != null && filter != null) {
          ((LinkedHashMap<String, Object>) chart.getChartDetails()).put("filter", filter);
        }
        DataInsightCustomChartResultList data =
            searchClient.buildDIChart(chart, startTimestamp, endTimestamp);
        result.put(chartName, data);
      }
    }
    return result;
  }
}
