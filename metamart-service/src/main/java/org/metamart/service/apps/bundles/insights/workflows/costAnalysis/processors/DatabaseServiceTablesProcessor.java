package org.metamart.service.apps.bundles.insights.workflows.costAnalysis.processors;

import static org.metamart.service.workflows.searchIndex.ReindexingUtil.getUpdatedStats;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.glassfish.jersey.internal.util.ExceptionUtils;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.system.IndexingError;
import org.metamart.schema.system.StepStats;
import org.metamart.schema.type.AccessDetails;
import org.metamart.schema.type.LifeCycle;
import org.metamart.schema.type.TableProfile;
import org.metamart.service.Entity;
import org.metamart.service.apps.bundles.insights.workflows.costAnalysis.CostAnalysisWorkflow;
import org.metamart.service.exception.SearchIndexException;
import org.metamart.service.jdbi3.TableRepository;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.ResultList;
import org.metamart.service.workflows.interfaces.Processor;

@Slf4j
public class DatabaseServiceTablesProcessor
    implements Processor<
        List<CostAnalysisWorkflow.CostAnalysisTableData>, ResultList<? extends EntityInterface>> {
  private final StepStats stats = new StepStats();

  public DatabaseServiceTablesProcessor(int total) {
    this.stats.withTotalRecords(total).withSuccessRecords(0).withFailedRecords(0);
  }

  @Override
  public List<CostAnalysisWorkflow.CostAnalysisTableData> process(
      ResultList<? extends EntityInterface> input, Map<String, Object> contextData)
      throws SearchIndexException {
    List<CostAnalysisWorkflow.CostAnalysisTableData> costAnalysisTableDataList = new ArrayList<>();
    try {
      for (EntityInterface entity : input.getData()) {
        Table table = (Table) entity;
        Optional<LifeCycle> oTableLifeCycle = Optional.empty();
        Optional<Double> oSize = Optional.empty();

        // Get LifeCycle information if it was accessed.
        Optional<LifeCycle> oTableLifeCycleData = Optional.ofNullable(table.getLifeCycle());
        if (oTableLifeCycleData.isPresent()) {
          Optional<AccessDetails> oAccessed =
              Optional.ofNullable(oTableLifeCycleData.get().getAccessed());

          if (oAccessed.isPresent()) {
            oTableLifeCycle = oTableLifeCycleData;
          }
        }

        // Get Size data.
        // TODO: Does the DataInsightsProcess have access to PII?
        Table tableProfileData =
            ((TableRepository) Entity.getEntityRepository(Entity.TABLE))
                .getLatestTableProfile(table.getFullyQualifiedName(), true);

        Optional<TableProfile> oTableProfile = Optional.ofNullable(tableProfileData.getProfile());

        if (oTableProfile.isPresent()) {
          oSize = Optional.ofNullable(oTableProfile.get().getSizeInByte());
        }

        if (oTableLifeCycle.isPresent() || oSize.isPresent()) {
          costAnalysisTableDataList.add(
              new CostAnalysisWorkflow.CostAnalysisTableData(table, oTableLifeCycle, oSize));
        }
      }
      updateStats(input.getData().size(), 0);
    } catch (Exception e) {
      IndexingError error =
          new IndexingError()
              .withErrorSource(IndexingError.ErrorSource.PROCESSOR)
              .withSubmittedCount(input.getData().size())
              .withFailedCount(input.getData().size())
              .withSuccessCount(0)
              .withMessage(
                  String.format(
                      "Database Service Tables Processor Encounter Failure: %s", e.getMessage()))
              .withStackTrace(ExceptionUtils.exceptionStackTraceAsString(e));
      LOG.debug(
          "[DatabaseServiceTAblesProcessor] Failed. Details: {}", JsonUtils.pojoToJson(error));
      updateStats(0, input.getData().size());
      throw new SearchIndexException(error);
    }
    return costAnalysisTableDataList;
  }

  @Override
  public void updateStats(int currentSuccess, int currentFailed) {
    getUpdatedStats(stats, currentSuccess, currentFailed);
  }

  @Override
  public StepStats getStats() {
    return stats;
  }
}
