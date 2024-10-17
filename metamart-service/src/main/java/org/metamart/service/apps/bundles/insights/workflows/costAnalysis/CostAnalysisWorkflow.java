package org.metamart.service.apps.bundles.insights.workflows.costAnalysis;

import static org.metamart.service.apps.bundles.insights.DataInsightsApp.REPORT_DATA_TYPE_KEY;
import static org.metamart.service.apps.bundles.insights.utils.TimestampUtils.TIMESTAMP_KEY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.analytics.AggregatedCostAnalysisReportData;
import org.metamart.schema.analytics.DataAssetMetrics;
import org.metamart.schema.analytics.RawCostAnalysisReportData;
import org.metamart.schema.analytics.ReportData;
import org.metamart.schema.api.services.CreateDatabaseService;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.entity.services.DatabaseService;
import org.metamart.schema.system.StepStats;
import org.metamart.schema.type.LifeCycle;
import org.metamart.service.Entity;
import org.metamart.service.apps.bundles.insights.DataInsightsApp;
import org.metamart.service.apps.bundles.insights.processors.CreateReportDataProcessor;
import org.metamart.service.apps.bundles.insights.sinks.ReportDataSink;
import org.metamart.service.apps.bundles.insights.utils.TimestampUtils;
import org.metamart.service.apps.bundles.insights.workflows.WorkflowStats;
import org.metamart.service.apps.bundles.insights.workflows.costAnalysis.processors.AggregatedCostAnalysisReportDataAggregator;
import org.metamart.service.apps.bundles.insights.workflows.costAnalysis.processors.AggregatedCostAnalysisReportDataProcessor;
import org.metamart.service.apps.bundles.insights.workflows.costAnalysis.processors.DatabaseServiceTablesProcessor;
import org.metamart.service.apps.bundles.insights.workflows.costAnalysis.processors.RawCostAnalysisReportDataProcessor;
import org.metamart.service.exception.SearchIndexException;
import org.metamart.service.jdbi3.ListFilter;
import org.metamart.service.jdbi3.ReportDataRepository;
import org.metamart.service.jdbi3.TableRepository;
import org.metamart.service.util.ResultList;
import org.metamart.service.workflows.searchIndex.PaginatedEntitiesSource;

@Slf4j
public class CostAnalysisWorkflow {
  public static final String AGGREGATED_COST_ANALYSIS_DATA_MAP_KEY =
      "aggregatedCostAnalysisDataMap";
  @Getter private final int batchSize;
  @Getter private final Long startTimestamp;
  @Getter private final Long endTimestamp;
  private final int retentionDays = 30;
  @Getter private final List<PaginatedEntitiesSource> sources = new ArrayList<>();

  public record CostAnalysisTableData(
      Table table, Optional<LifeCycle> oLifeCycle, Optional<Double> oSize) {}

  public record AggregatedCostAnalysisData(
      Double totalSize,
      Double totalCount,
      DataAssetMetrics unusedDataAssets,
      DataAssetMetrics frequentlyUsedDataAssets) {}

  @Getter private DatabaseServiceTablesProcessor databaseServiceTablesProcessor;
  @Getter private RawCostAnalysisReportDataProcessor rawCostAnalysisReportDataProcessor;

  @Getter
  private AggregatedCostAnalysisReportDataProcessor aggregatedCostAnalysisReportDataProcessor;

  @Getter private final WorkflowStats workflowStats = new WorkflowStats("CostAnalysisWorkflow");

  public CostAnalysisWorkflow(
      Long timestamp, int batchSize, Optional<DataInsightsApp.Backfill> backfill) {
    this.endTimestamp =
        TimestampUtils.getEndOfDayTimestamp(TimestampUtils.subtractDays(timestamp, 1));
    this.startTimestamp = TimestampUtils.getStartOfDayTimestamp(endTimestamp);

    // TODO: Implement Backfill by using DataAsset Version.
    //    if (backfill.isPresent()) {
    //      Long oldestPossibleTimestamp = TimestampUtils.getStartOfDayTimestamp(timestamp -
    // TimestampUtils.subtractDays(timestamp, retentionDays));
    //      this.endTimestamp =
    // TimestampUtils.getEndOfDayTimestamp(Collections.max(List.of(TimestampUtils.getTimestampFromDateString(backfill.get().endDate()))));
    //      this.startTimestamp = TimestampUtils.getStartOfDayTimestamp(Collections.max(
    //
    //     List.of(TimestampUtils.getTimestampFromDateString(backfill.get().endDate()),
    //     oldestPossibleTimestamp)));
    //
    //      if (oldestPossibleTimestamp.equals(TimestampUtils.getStartOfDayTimestamp(endTimestamp)))
    // {
    //        LOG.warn("CostAnalysis Backfill won't happen because the set date is before the limit
    // of {}", oldestPossibleTimestamp);
    //      }
    //    } else {
    //      this.endTimestamp =
    // TimestampUtils.getEndOfDayTimestamp(TimestampUtils.subtractDays(timestamp, 1));
    //      this.startTimestamp = TimestampUtils.getStartOfDayTimestamp(endTimestamp);
    //    }

    this.batchSize = batchSize;
  }

  private void initialize() throws SearchIndexException {
    PaginatedEntitiesSource databaseServices =
        new PaginatedEntitiesSource(Entity.DATABASE_SERVICE, batchSize, List.of("*"));
    int total = 0;

    while (!databaseServices.isDone()) {
      ResultList<DatabaseService> resultList =
          filterDatabaseServices(databaseServices.readNext(null));
      if (!resultList.getData().isEmpty()) {
        for (DatabaseService databaseService : resultList.getData()) {
          ListFilter filter = new ListFilter(null);
          filter.addQueryParam("database", databaseService.getFullyQualifiedName());

          sources.add(
              new PaginatedEntitiesSource(Entity.TABLE, batchSize, List.of("*"), filter)
                  .withName(
                      String.format(
                          "[CostAnalysisWorkflow] %s", databaseService.getFullyQualifiedName())));
          total +=
              ((TableRepository) Entity.getEntityRepository(Entity.TABLE))
                  .getDao()
                  .listCount(filter);
        }
      }
    }

    databaseServiceTablesProcessor = new DatabaseServiceTablesProcessor(total);

    rawCostAnalysisReportDataProcessor = new RawCostAnalysisReportDataProcessor(total);
    aggregatedCostAnalysisReportDataProcessor =
        new AggregatedCostAnalysisReportDataProcessor(total);
  }

  public void process() throws SearchIndexException {
    initialize();
    Map<String, Object> contextData = new HashMap<>();

    // Delete the records of the days we are going to process
    // TODO: It might be good to delete and process one day at a time
    Long pointerTimestamp = TimestampUtils.getStartOfDayTimestamp(endTimestamp);
    while (pointerTimestamp >= startTimestamp) {
      deleteReportDataRecordsAtDate(
          pointerTimestamp, ReportData.ReportDataType.AGGREGATED_COST_ANALYSIS_REPORT_DATA);
      pointerTimestamp = TimestampUtils.subtractDays(pointerTimestamp, 1);
    }

    // Delete the Raw Records since we only keep the last Snapshot.
    // TODO: When implementing backfill, we should only save the last Date for the Raw Reports.
    deleteReportDataRecords(ReportData.ReportDataType.RAW_COST_ANALYSIS_REPORT_DATA);

    for (PaginatedEntitiesSource source : sources) {
      // TODO: Could the size of the Maps be an issue?
      List<RawCostAnalysisReportData> rawCostAnalysisReportDataList = new ArrayList<>();
      Map<String, Map<String, Map<String, AggregatedCostAnalysisData>>>
          aggregatedCostAnalysisDataMap = new HashMap<>();

      contextData.put(TIMESTAMP_KEY, startTimestamp);
      contextData.put(AGGREGATED_COST_ANALYSIS_DATA_MAP_KEY, aggregatedCostAnalysisDataMap);

      Optional<String> initialProcessorError = Optional.empty();

      while (!source.isDone()) {
        try {
          ResultList<? extends EntityInterface> resultList = source.readNext(null);
          List<CostAnalysisTableData> costAnalysisTableData =
              databaseServiceTablesProcessor.process(resultList, contextData);
          rawCostAnalysisReportDataList.addAll(
              rawCostAnalysisReportDataProcessor.process(costAnalysisTableData, contextData));
          aggregatedCostAnalysisReportDataProcessor.process(costAnalysisTableData, contextData);
          source.updateStats(resultList.getData().size(), 0);
        } catch (SearchIndexException ex) {
          source.updateStats(
              ex.getIndexingError().getSuccessCount(), ex.getIndexingError().getFailedCount());
          String errorMessage =
              String.format("Failed processing Data from %s: ", source.getName(), ex);
          initialProcessorError = Optional.of(errorMessage);
          workflowStats.addFailure(errorMessage);
        } finally {
          updateWorkflowStats(source.getName(), source.getStats());
        }
      }

      if (initialProcessorError.isPresent()) {
        continue;
      }

      Optional<String> processRawCostAnalysisError =
          processRawCostAnalysisReportData(rawCostAnalysisReportDataList, contextData);

      processRawCostAnalysisError.ifPresent(LOG::debug);

      Optional<String> processAggregatedCostAnalysisError =
          processAggregatedCostAnalysisReportData(aggregatedCostAnalysisDataMap, contextData);

      processAggregatedCostAnalysisError.ifPresent(LOG::debug);
    }
  }

  private Optional<String> processRawCostAnalysisReportData(
      List<RawCostAnalysisReportData> rawCostAnalysisReportDataList,
      Map<String, Object> contextData) {
    Optional<String> error = Optional.empty();

    contextData.put(REPORT_DATA_TYPE_KEY, ReportData.ReportDataType.RAW_COST_ANALYSIS_REPORT_DATA);
    CreateReportDataProcessor createReportdataProcessor =
        new CreateReportDataProcessor(
            rawCostAnalysisReportDataList.size(),
            "[CostAnalysisWorkflow] Raw Cost Analysis Report Data Processor");

    Optional<List<ReportData>> rawCostAnalysisReportData = Optional.empty();

    try {
      rawCostAnalysisReportData =
          Optional.of(
              createReportdataProcessor.process(rawCostAnalysisReportDataList, contextData));
    } catch (SearchIndexException ex) {
      error =
          Optional.of(
              String.format(
                  "Failed Processing Raw Cost Analysis Report Data: %s", ex.getMessage()));
      workflowStats.addFailure(error.get());
    } finally {
      updateWorkflowStats(
          createReportdataProcessor.getName(), createReportdataProcessor.getStats());
    }

    if (rawCostAnalysisReportData.isPresent()) {
      ReportDataSink reportDataSink =
          new ReportDataSink(
              rawCostAnalysisReportData.get().size(),
              "[CostAnalysisWorkflow] Raw Cost Analysis Report Data " + "Sink");
      try {
        reportDataSink.write(rawCostAnalysisReportData.get(), contextData);
      } catch (SearchIndexException ex) {
        error =
            Optional.of(
                String.format("Failed Sinking Raw Cost Analysis Report Data: %s", ex.getMessage()));
        workflowStats.addFailure(error.get());
      } finally {
        updateWorkflowStats(reportDataSink.getName(), reportDataSink.getStats());
      }
    }

    return error;
  }

  private Optional<String> processAggregatedCostAnalysisReportData(
      Map<String, Map<String, Map<String, AggregatedCostAnalysisData>>>
          aggregatedCostAnalysisDataMap,
      Map<String, Object> contextData) {
    Optional<String> error = Optional.empty();

    contextData.put(
        REPORT_DATA_TYPE_KEY, ReportData.ReportDataType.AGGREGATED_COST_ANALYSIS_REPORT_DATA);
    AggregatedCostAnalysisReportDataAggregator aggregatedCostAnalysisReportDataAggregator =
        new AggregatedCostAnalysisReportDataAggregator(aggregatedCostAnalysisDataMap.size());

    Optional<List<AggregatedCostAnalysisReportData>> aggregatedCostAnalysisReportDataList =
        Optional.empty();

    try {
      aggregatedCostAnalysisReportDataList =
          Optional.of(
              aggregatedCostAnalysisReportDataAggregator.process(
                  aggregatedCostAnalysisDataMap, contextData));
    } catch (SearchIndexException ex) {
      error =
          Optional.of(
              String.format("Failed Aggregating Cost Analysis Report Data: %s", ex.getMessage()));
      workflowStats.addFailure(error.get());
    } finally {
      updateWorkflowStats(
          aggregatedCostAnalysisReportDataAggregator.getName(),
          aggregatedCostAnalysisReportDataAggregator.getStats());
    }

    if (aggregatedCostAnalysisReportDataList.isPresent()) {
      CreateReportDataProcessor createReportdataProcessor =
          new CreateReportDataProcessor(
              aggregatedCostAnalysisReportDataList.get().size(),
              "[CostAnalysisWorkflow] Aggregated Cost Analysis Report Data Processor");
      Optional<List<ReportData>> aggregatedCostAnalysisReportData = Optional.empty();

      try {
        aggregatedCostAnalysisReportData =
            Optional.of(
                createReportdataProcessor.process(
                    aggregatedCostAnalysisReportDataList.get(), contextData));
      } catch (SearchIndexException ex) {
        error =
            Optional.of(
                String.format(
                    "Failed Processing Aggregated Cost Analysis Report Data: %s", ex.getMessage()));
        workflowStats.addFailure(error.get());
      } finally {
        updateWorkflowStats(
            createReportdataProcessor.getName(), createReportdataProcessor.getStats());
      }

      if (aggregatedCostAnalysisReportData.isPresent()) {
        ReportDataSink reportDataSink =
            new ReportDataSink(
                aggregatedCostAnalysisReportData.get().size(),
                "[CostAnalysisWorkflow] Aggregated Cost Analysis Report Data Sink");
        try {
          reportDataSink.write(aggregatedCostAnalysisReportData.get(), contextData);
        } catch (SearchIndexException ex) {
          error =
              Optional.of(
                  String.format(
                      "Failed Sinking Aggregated Cost Analysis Report Data: %s", ex.getMessage()));
          workflowStats.addFailure(error.get());
        } finally {
          updateWorkflowStats(reportDataSink.getName(), reportDataSink.getStats());
        }
      }
    }

    return error;
  }

  private ResultList<DatabaseService> filterDatabaseServices(
      ResultList<? extends EntityInterface> resultList) {
    return new ResultList<>(
        resultList.getData().stream()
            .map(object -> (DatabaseService) object)
            .filter(this::databaseServiceSupportsProfilerAndUsage)
            .toList());
  }

  private boolean databaseServiceSupportsProfilerAndUsage(DatabaseService databaseService) {
    return List.of(
            CreateDatabaseService.DatabaseServiceType.BigQuery,
            CreateDatabaseService.DatabaseServiceType.Redshift,
            CreateDatabaseService.DatabaseServiceType.Snowflake)
        .contains(databaseService.getServiceType());
  }

  private void deleteReportDataRecordsAtDate(
      Long timestamp, ReportData.ReportDataType reportDataType) {
    String timestampString = TimestampUtils.timestampToString(timestamp, "yyyy-MM-dd");
    ((ReportDataRepository) Entity.getEntityTimeSeriesRepository(Entity.ENTITY_REPORT_DATA))
        .deleteReportDataAtDate(reportDataType, timestampString);
  }

  private void deleteReportDataRecords(ReportData.ReportDataType reportDataType) {
    ((ReportDataRepository) Entity.getEntityTimeSeriesRepository(Entity.ENTITY_REPORT_DATA))
        .deleteReportData(reportDataType);
  }

  private void updateWorkflowStats(String stepName, StepStats newStepStats) {
    workflowStats.updateWorkflowStepStats(stepName, newStepStats);

    int currentSuccess =
        workflowStats.getWorkflowStepStats().values().stream()
            .mapToInt(StepStats::getSuccessRecords)
            .sum();
    int currentFailed =
        workflowStats.getWorkflowStepStats().values().stream()
            .mapToInt(StepStats::getFailedRecords)
            .sum();

    workflowStats.updateWorkflowStats(currentSuccess, currentFailed);
  }
}
