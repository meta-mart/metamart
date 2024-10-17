package org.metamart.service.apps.bundles.insights.workflows.dataAssets;

import static org.metamart.schema.system.IndexingError.ErrorSource.READER;
import static org.metamart.service.apps.bundles.insights.DataInsightsApp.getDataStreamName;
import static org.metamart.service.apps.bundles.insights.utils.TimestampUtils.END_TIMESTAMP_KEY;
import static org.metamart.service.apps.bundles.insights.utils.TimestampUtils.START_TIMESTAMP_KEY;
import static org.metamart.service.workflows.searchIndex.ReindexingUtil.ENTITY_TYPE_KEY;
import static org.metamart.service.workflows.searchIndex.ReindexingUtil.getTotalRequestToProcess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.service.configuration.elasticsearch.ElasticSearchConfiguration;
import org.metamart.schema.system.IndexingError;
import org.metamart.schema.system.StepStats;
import org.metamart.service.apps.bundles.insights.DataInsightsApp;
import org.metamart.service.apps.bundles.insights.utils.TimestampUtils;
import org.metamart.service.apps.bundles.insights.workflows.WorkflowStats;
import org.metamart.service.apps.bundles.insights.workflows.dataAssets.processors.DataInsightsElasticSearchProcessor;
import org.metamart.service.apps.bundles.insights.workflows.dataAssets.processors.DataInsightsEntityEnricherProcessor;
import org.metamart.service.apps.bundles.insights.workflows.dataAssets.processors.DataInsightsOpenSearchProcessor;
import org.metamart.service.exception.SearchIndexException;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.search.SearchRepository;
import org.metamart.service.search.elasticsearch.ElasticSearchIndexSink;
import org.metamart.service.search.opensearch.OpenSearchIndexSink;
import org.metamart.service.util.ResultList;
import org.metamart.service.workflows.interfaces.Processor;
import org.metamart.service.workflows.interfaces.Sink;
import org.metamart.service.workflows.interfaces.Source;
import org.metamart.service.workflows.searchIndex.PaginatedEntitiesSource;

@Slf4j
public class DataAssetsWorkflow {
  public static final String DATA_STREAM_KEY = "DataStreamKey";
  private final int retentionDays = 30;
  private final Long startTimestamp;
  private final Long endTimestamp;
  private final int batchSize;
  private final SearchRepository searchRepository;
  private final CollectionDAO collectionDAO;
  private final List<PaginatedEntitiesSource> sources = new ArrayList<>();
  private final Set<String> entityTypes;

  private DataInsightsEntityEnricherProcessor entityEnricher;
  private Processor entityProcessor;
  private Sink searchIndexSink;
  @Getter private final WorkflowStats workflowStats = new WorkflowStats("DataAssetsWorkflow");

  public DataAssetsWorkflow(
      Long timestamp,
      int batchSize,
      Optional<DataInsightsApp.Backfill> backfill,
      Set<String> entityTypes,
      CollectionDAO collectionDAO,
      SearchRepository searchRepository) {
    if (backfill.isPresent()) {
      Long oldestPossibleTimestamp =
          TimestampUtils.getStartOfDayTimestamp(
              TimestampUtils.subtractDays(timestamp, retentionDays));

      this.endTimestamp =
          TimestampUtils.getEndOfDayTimestamp(
              Collections.max(
                  List.of(TimestampUtils.getTimestampFromDateString(backfill.get().endDate()))));
      this.startTimestamp =
          TimestampUtils.getStartOfDayTimestamp(
              Collections.max(
                  List.of(
                      TimestampUtils.getTimestampFromDateString(backfill.get().startDate()),
                      oldestPossibleTimestamp)));

      if (oldestPossibleTimestamp.equals(TimestampUtils.getStartOfDayTimestamp(endTimestamp))) {
        LOG.warn(
            "Backfill won't happen because the set date is before the limit of {}",
            oldestPossibleTimestamp);
      }
    } else {
      this.endTimestamp = TimestampUtils.getEndOfDayTimestamp(timestamp);
      this.startTimestamp =
          TimestampUtils.getStartOfDayTimestamp(TimestampUtils.subtractDays(timestamp, 1));
    }

    this.batchSize = batchSize;
    this.searchRepository = searchRepository;
    this.collectionDAO = collectionDAO;
    this.entityTypes = entityTypes;
  }

  private void initialize() {
    int totalRecords = getTotalRequestToProcess(entityTypes, collectionDAO);

    entityTypes.forEach(
        entityType -> {
          List<String> fields = List.of("*");
          PaginatedEntitiesSource source =
              new PaginatedEntitiesSource(entityType, batchSize, fields)
                  .withName(String.format("[DataAssetsWorkflow] %s", entityType));
          sources.add(source);
        });

    this.entityEnricher = new DataInsightsEntityEnricherProcessor(totalRecords);
    if (searchRepository.getSearchType().equals(ElasticSearchConfiguration.SearchType.OPENSEARCH)) {
      this.entityProcessor = new DataInsightsOpenSearchProcessor(totalRecords);
      this.searchIndexSink =
          new OpenSearchIndexSink(
              searchRepository,
              totalRecords,
              searchRepository.getElasticSearchConfiguration().getPayLoadSize());
    } else {
      this.entityProcessor = new DataInsightsElasticSearchProcessor(totalRecords);
      this.searchIndexSink =
          new ElasticSearchIndexSink(
              searchRepository,
              totalRecords,
              searchRepository.getElasticSearchConfiguration().getPayLoadSize());
    }
  }

  public void process() throws SearchIndexException {
    initialize();
    Map<String, Object> contextData = new HashMap<>();

    contextData.put(START_TIMESTAMP_KEY, startTimestamp);
    contextData.put(END_TIMESTAMP_KEY, endTimestamp);

    for (PaginatedEntitiesSource source : sources) {
      deleteDataBeforeInserting(getDataStreamName(source.getEntityType()));
      contextData.put(DATA_STREAM_KEY, getDataStreamName(source.getEntityType()));
      contextData.put(ENTITY_TYPE_KEY, source.getEntityType());

      while (!source.isDone()) {
        try {
          processEntity(source.readNext(null), contextData, source);
        } catch (SearchIndexException ex) {
          source.updateStats(
              ex.getIndexingError().getSuccessCount(), ex.getIndexingError().getFailedCount());
          String errorMessage =
              String.format("Failed processing Data from %s: %s", source.getName(), ex);
          workflowStats.addFailure(errorMessage);
        } finally {
          updateWorkflowStats(source.getName(), source.getStats());
        }
      }
    }
  }

  private void processEntity(
      ResultList<? extends EntityInterface> resultList,
      Map<String, Object> contextData,
      Source<?> paginatedSource)
      throws SearchIndexException {
    if (!resultList.getData().isEmpty()) {
      searchIndexSink.write(
          entityProcessor.process(entityEnricher.process(resultList, contextData), contextData),
          contextData);
      if (!resultList.getErrors().isEmpty()) {
        throw new SearchIndexException(
            new IndexingError()
                .withErrorSource(READER)
                .withLastFailedCursor(paginatedSource.getLastFailedCursor())
                .withSubmittedCount(paginatedSource.getBatchSize())
                .withSuccessCount(resultList.getData().size())
                .withFailedCount(resultList.getErrors().size())
                .withMessage(
                    String.format(
                        "Issues in Reading A Batch For Entities: %s", resultList.getErrors()))
                .withFailedEntities(resultList.getErrors()));
      }
      paginatedSource.updateStats(resultList.getData().size(), 0);
    }
  }

  private void deleteDataBeforeInserting(String dataStreamName) throws SearchIndexException {
    try {
      searchRepository
          .getSearchClient()
          .deleteByQuery(
              dataStreamName,
              String.format(
                  "{\"@timestamp\": {\"gte\": %s, \"lte\": %s}}", startTimestamp, endTimestamp));
    } catch (Exception rx) {
      throw new SearchIndexException(new IndexingError().withMessage(rx.getMessage()));
    }
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
