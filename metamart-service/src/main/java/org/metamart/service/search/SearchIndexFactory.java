package org.metamart.service.search;

import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.analytics.ReportData;
import org.metamart.schema.entity.classification.Classification;
import org.metamart.schema.entity.classification.Tag;
import org.metamart.schema.entity.data.APICollection;
import org.metamart.schema.entity.data.APIEndpoint;
import org.metamart.schema.entity.data.Chart;
import org.metamart.schema.entity.data.Container;
import org.metamart.schema.entity.data.Dashboard;
import org.metamart.schema.entity.data.DashboardDataModel;
import org.metamart.schema.entity.data.Database;
import org.metamart.schema.entity.data.DatabaseSchema;
import org.metamart.schema.entity.data.Glossary;
import org.metamart.schema.entity.data.GlossaryTerm;
import org.metamart.schema.entity.data.Metric;
import org.metamart.schema.entity.data.MlModel;
import org.metamart.schema.entity.data.Pipeline;
import org.metamart.schema.entity.data.Query;
import org.metamart.schema.entity.data.StoredProcedure;
import org.metamart.schema.entity.data.Table;
import org.metamart.schema.entity.data.Topic;
import org.metamart.schema.entity.domains.DataProduct;
import org.metamart.schema.entity.domains.Domain;
import org.metamart.schema.entity.services.*;
import org.metamart.schema.entity.services.ApiService;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.teams.Team;
import org.metamart.schema.entity.teams.User;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.tests.TestSuite;
import org.metamart.schema.tests.type.TestCaseResolutionStatus;
import org.metamart.schema.tests.type.TestCaseResult;
import org.metamart.service.Entity;
import org.metamart.service.search.indexes.APICollectionIndex;
import org.metamart.service.search.indexes.APIEndpointIndex;
import org.metamart.service.search.indexes.APIServiceIndex;
import org.metamart.service.search.indexes.AggregatedCostAnalysisReportDataIndex;
import org.metamart.service.search.indexes.ChartIndex;
import org.metamart.service.search.indexes.ClassificationIndex;
import org.metamart.service.search.indexes.ContainerIndex;
import org.metamart.service.search.indexes.DashboardDataModelIndex;
import org.metamart.service.search.indexes.DashboardIndex;
import org.metamart.service.search.indexes.DashboardServiceIndex;
import org.metamart.service.search.indexes.DataProductIndex;
import org.metamart.service.search.indexes.DatabaseIndex;
import org.metamart.service.search.indexes.DatabaseSchemaIndex;
import org.metamart.service.search.indexes.DatabaseServiceIndex;
import org.metamart.service.search.indexes.DomainIndex;
import org.metamart.service.search.indexes.EntityReportDataIndex;
import org.metamart.service.search.indexes.GlossaryIndex;
import org.metamart.service.search.indexes.GlossaryTermIndex;
import org.metamart.service.search.indexes.IngestionPipelineIndex;
import org.metamart.service.search.indexes.MessagingServiceIndex;
import org.metamart.service.search.indexes.MetadataServiceIndex;
import org.metamart.service.search.indexes.MetricIndex;
import org.metamart.service.search.indexes.MlModelIndex;
import org.metamart.service.search.indexes.MlModelServiceIndex;
import org.metamart.service.search.indexes.PipelineIndex;
import org.metamart.service.search.indexes.PipelineServiceIndex;
import org.metamart.service.search.indexes.QueryIndex;
import org.metamart.service.search.indexes.RawCostAnalysisReportDataIndex;
import org.metamart.service.search.indexes.SearchEntityIndex;
import org.metamart.service.search.indexes.SearchIndex;
import org.metamart.service.search.indexes.SearchServiceIndex;
import org.metamart.service.search.indexes.StorageServiceIndex;
import org.metamart.service.search.indexes.StoredProcedureIndex;
import org.metamart.service.search.indexes.TableIndex;
import org.metamart.service.search.indexes.TagIndex;
import org.metamart.service.search.indexes.TeamIndex;
import org.metamart.service.search.indexes.TestCaseIndex;
import org.metamart.service.search.indexes.TestCaseResolutionStatusIndex;
import org.metamart.service.search.indexes.TestCaseResultIndex;
import org.metamart.service.search.indexes.TestSuiteIndex;
import org.metamart.service.search.indexes.TopicIndex;
import org.metamart.service.search.indexes.UserIndex;
import org.metamart.service.search.indexes.WebAnalyticEntityViewReportDataIndex;
import org.metamart.service.search.indexes.WebAnalyticUserActivityReportDataIndex;

@Slf4j
public class SearchIndexFactory {

  public SearchIndex buildIndex(String entityType, Object entity) {
    return switch (entityType) {
      case Entity.TABLE -> new TableIndex((Table) entity);
      case Entity.DASHBOARD -> new DashboardIndex((Dashboard) entity);
      case Entity.TOPIC -> new TopicIndex((Topic) entity);
      case Entity.PIPELINE -> new PipelineIndex((Pipeline) entity);
      case Entity.INGESTION_PIPELINE -> new IngestionPipelineIndex((IngestionPipeline) entity);
      case Entity.USER -> new UserIndex((User) entity);
      case Entity.TEAM -> new TeamIndex((Team) entity);
      case Entity.METRIC -> new MetricIndex((Metric) entity);
      case Entity.GLOSSARY -> new GlossaryIndex((Glossary) entity);
      case Entity.GLOSSARY_TERM -> new GlossaryTermIndex((GlossaryTerm) entity);
      case Entity.MLMODEL -> new MlModelIndex((MlModel) entity);
      case Entity.TAG -> new TagIndex((Tag) entity);
      case Entity.CLASSIFICATION -> new ClassificationIndex((Classification) entity);
      case Entity.QUERY -> new QueryIndex((Query) entity);
      case Entity.CONTAINER -> new ContainerIndex((Container) entity);
      case Entity.DATABASE -> new DatabaseIndex((Database) entity);
      case Entity.DATABASE_SCHEMA -> new DatabaseSchemaIndex((DatabaseSchema) entity);
      case Entity.TEST_CASE -> new TestCaseIndex((TestCase) entity);
      case Entity.TEST_SUITE -> new TestSuiteIndex((TestSuite) entity);
      case Entity.CHART -> new ChartIndex((Chart) entity);
      case Entity.DASHBOARD_DATA_MODEL -> new DashboardDataModelIndex((DashboardDataModel) entity);
      case Entity.API_COLLCECTION -> new APICollectionIndex((APICollection) entity);
      case Entity.API_ENDPOINT -> new APIEndpointIndex((APIEndpoint) entity);
      case Entity.DASHBOARD_SERVICE -> new DashboardServiceIndex((DashboardService) entity);
      case Entity.DATABASE_SERVICE -> new DatabaseServiceIndex((DatabaseService) entity);
      case Entity.MESSAGING_SERVICE -> new MessagingServiceIndex((MessagingService) entity);
      case Entity.MLMODEL_SERVICE -> new MlModelServiceIndex((MlModelService) entity);
      case Entity.SEARCH_SERVICE -> new SearchServiceIndex((SearchService) entity);
      case Entity.API_SERVICE -> new APIServiceIndex((ApiService) entity);
      case Entity.SEARCH_INDEX -> new SearchEntityIndex(
          (org.metamart.schema.entity.data.SearchIndex) entity);
      case Entity.PIPELINE_SERVICE -> new PipelineServiceIndex((PipelineService) entity);
      case Entity.STORAGE_SERVICE -> new StorageServiceIndex((StorageService) entity);
      case Entity.DOMAIN -> new DomainIndex((Domain) entity);
      case Entity.STORED_PROCEDURE -> new StoredProcedureIndex((StoredProcedure) entity);
      case Entity.DATA_PRODUCT -> new DataProductIndex((DataProduct) entity);
      case Entity.METADATA_SERVICE -> new MetadataServiceIndex((MetadataService) entity);
      case Entity.ENTITY_REPORT_DATA -> new EntityReportDataIndex((ReportData) entity);
      case Entity.WEB_ANALYTIC_ENTITY_VIEW_REPORT_DATA -> new WebAnalyticEntityViewReportDataIndex(
          (ReportData) entity);
      case Entity
          .WEB_ANALYTIC_USER_ACTIVITY_REPORT_DATA -> new WebAnalyticUserActivityReportDataIndex(
          (ReportData) entity);
      case Entity.RAW_COST_ANALYSIS_REPORT_DATA -> new RawCostAnalysisReportDataIndex(
          (ReportData) entity);
      case Entity.AGGREGATED_COST_ANALYSIS_REPORT_DATA -> new AggregatedCostAnalysisReportDataIndex(
          (ReportData) entity);
      case Entity.TEST_CASE_RESOLUTION_STATUS -> new TestCaseResolutionStatusIndex(
          (TestCaseResolutionStatus) entity);
      case Entity.TEST_CASE_RESULT -> new TestCaseResultIndex((TestCaseResult) entity);
      default -> buildExternalIndexes(entityType, entity);
    };
  }

  protected SearchIndex buildExternalIndexes(String entityType, Object entity) {
    throw new IllegalArgumentException(
        String.format(
            "Entity Type [%s] is not valid for Index Factory, Entity: %s", entityType, entity));
  }
}
