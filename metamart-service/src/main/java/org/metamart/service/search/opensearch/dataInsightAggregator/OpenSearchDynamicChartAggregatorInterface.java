package org.metamart.service.search.opensearch.dataInsightAggregator;

import static org.metamart.service.search.opensearch.OpenSearchClient.X_CONTENT_REGISTRY;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.metamart.schema.dataInsight.custom.DataInsightCustomChart;
import org.metamart.schema.dataInsight.custom.DataInsightCustomChartResult;
import org.metamart.schema.dataInsight.custom.DataInsightCustomChartResultList;
import org.metamart.schema.dataInsight.custom.FormulaHolder;
import org.metamart.schema.dataInsight.custom.Function;
import org.metamart.service.jdbi3.DataInsightSystemChartRepository;
import org.metamart.service.security.policyevaluator.CompiledRule;
import org.springframework.expression.Expression;
import os.org.opensearch.action.search.SearchRequest;
import os.org.opensearch.action.search.SearchResponse;
import os.org.opensearch.common.xcontent.LoggingDeprecationHandler;
import os.org.opensearch.common.xcontent.XContentParser;
import os.org.opensearch.common.xcontent.XContentType;
import os.org.opensearch.index.query.QueryBuilder;
import os.org.opensearch.index.query.QueryBuilders;
import os.org.opensearch.search.aggregations.Aggregation;
import os.org.opensearch.search.aggregations.AggregationBuilders;
import os.org.opensearch.search.aggregations.bucket.filter.ParsedFilter;
import os.org.opensearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import os.org.opensearch.search.aggregations.bucket.histogram.Histogram;
import os.org.opensearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import os.org.opensearch.search.aggregations.metrics.ParsedCardinality;
import os.org.opensearch.search.aggregations.metrics.ParsedSingleValueNumericMetricsAggregation;
import os.org.opensearch.search.aggregations.metrics.ParsedValueCount;
import os.org.opensearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import os.org.opensearch.search.builder.SearchSourceBuilder;

public interface OpenSearchDynamicChartAggregatorInterface {
  long MILLISECONDS_IN_DAY = 24 * 60 * 60 * 1000;

  private static ValuesSourceAggregationBuilder getSubAggregationsByFunction(
      Function function, String field, int index) {
    return switch (function) {
      case COUNT -> AggregationBuilders.count(field + index).field(field);
      case SUM -> AggregationBuilders.sum(field + index).field(field);
      case AVG -> AggregationBuilders.avg(field + index).field(field);
      case MIN -> AggregationBuilders.min(field + index).field(field);
      case MAX -> AggregationBuilders.max(field + index).field(field);
      case UNIQUE -> AggregationBuilders.cardinality(field + index).field(field);
    };
  }

  static void getDateHistogramByFormula(
      String formula,
      QueryBuilder filter,
      DateHistogramAggregationBuilder dateHistogramAggregationBuilder,
      List<FormulaHolder> formulas) {
    Pattern pattern = Pattern.compile(DataInsightSystemChartRepository.FORMULA_FUNC_REGEX);
    Matcher matcher = pattern.matcher(formula);
    int index = 0;
    while (matcher.find()) {
      FormulaHolder holder = new FormulaHolder();
      holder.setFormula(matcher.group());
      holder.setFunction(Function.valueOf(matcher.group(1).toUpperCase()));
      String field;
      if (matcher.group(3) != null) {
        field = matcher.group(3);
      } else {
        field = "id.keyword";
      }
      ValuesSourceAggregationBuilder subAgg =
          getSubAggregationsByFunction(
              Function.valueOf(matcher.group(1).toUpperCase()), field, index);
      if (matcher.group(5) != null) {
        QueryBuilder queryBuilder;
        if (filter != null) {
          queryBuilder =
              QueryBuilders.boolQuery()
                  .must(QueryBuilders.queryStringQuery(matcher.group(5)))
                  .must(filter);
        } else {
          queryBuilder = QueryBuilders.queryStringQuery(matcher.group(5));
        }
        dateHistogramAggregationBuilder.subAggregation(
            AggregationBuilders.filter("filer" + index, queryBuilder).subAggregation(subAgg));
        holder.setQuery(matcher.group(5));
      } else {
        if (filter != null) {
          dateHistogramAggregationBuilder.subAggregation(
              AggregationBuilders.filter("filer" + index, filter).subAggregation(subAgg));
        } else {
          dateHistogramAggregationBuilder.subAggregation(subAgg);
        }
      }
      formulas.add(holder);
      index++;
    }
  }

  private List<DataInsightCustomChartResult> processMultiAggregations(
      List<Aggregation> aggregations, String formula, String group, List<FormulaHolder> holder) {
    List<DataInsightCustomChartResult> finalList = new ArrayList<>();

    List<List<DataInsightCustomChartResult>> results =
        processAggregationsInternal(aggregations, group);
    for (List<DataInsightCustomChartResult> result : results) {
      String formulaCopy = formula;
      if (holder.size() != result.size()) {
        continue;
      }
      boolean evaluate = true;
      Double day = null;
      for (int i = 0; i < holder.size(); i++) {
        if (result.get(i).getCount() == null) {
          evaluate = false;
          break;
        }
        day = result.get(i).getDay();
        formulaCopy =
            formulaCopy.replace(holder.get(i).getFormula(), result.get(i).getCount().toString());
      }
      if (evaluate
          && formulaCopy.matches(DataInsightSystemChartRepository.NUMERIC_VALIDATION_REGEX)
          && day != null) {
        Expression expression = CompiledRule.parseExpression(formulaCopy);
        Double value = (Double) expression.getValue();
        if (!value.isNaN() && !value.isInfinite()) {
          finalList.add(
              new DataInsightCustomChartResult().withCount(value).withGroup(group).withDay(day));
        }
      }
    }
    return finalList;
  }

  default void populateDateHistogram(
      Function function,
      String formula,
      String field,
      String filter,
      DateHistogramAggregationBuilder dateHistogramAggregationBuilder,
      List<FormulaHolder> formulas)
      throws IOException {
    if (formula != null) {
      if (filter != null && !filter.equals("{}")) {
        XContentParser filterParser =
            XContentType.JSON
                .xContent()
                .createParser(X_CONTENT_REGISTRY, LoggingDeprecationHandler.INSTANCE, filter);
        QueryBuilder queryFilter = SearchSourceBuilder.fromXContent(filterParser).query();
        getDateHistogramByFormula(formula, queryFilter, dateHistogramAggregationBuilder, formulas);
      } else {
        getDateHistogramByFormula(formula, null, dateHistogramAggregationBuilder, formulas);
      }
      return;
    }

    // process non formula date histogram
    ValuesSourceAggregationBuilder subAgg = getSubAggregationsByFunction(function, field, 0);
    if (filter != null && !filter.equals("{}")) {
      XContentParser filterParser =
          XContentType.JSON
              .xContent()
              .createParser(X_CONTENT_REGISTRY, LoggingDeprecationHandler.INSTANCE, filter);
      QueryBuilder queryFilter = SearchSourceBuilder.fromXContent(filterParser).query();
      dateHistogramAggregationBuilder.subAggregation(
          AggregationBuilders.filter("filer", queryFilter).subAggregation(subAgg));
    } else {
      dateHistogramAggregationBuilder.subAggregation(subAgg);
    }
  }

  SearchRequest prepareSearchRequest(
      @NotNull DataInsightCustomChart diChart, long start, long end, List<FormulaHolder> formulas)
      throws IOException;

  DataInsightCustomChartResultList processSearchResponse(
      @NotNull DataInsightCustomChart diChart,
      SearchResponse searchResponse,
      List<FormulaHolder> formulas);

  default List<DataInsightCustomChartResult> processAggregations(
      List<Aggregation> aggregations, String formula, String group, List<FormulaHolder> holder) {
    if (formula != null) {
      return processMultiAggregations(aggregations, formula, group, holder);
    }
    return processSingleAggregations(aggregations, group);
  }

  private List<DataInsightCustomChartResult> processSingleAggregations(
      List<Aggregation> aggregations, String group) {
    List<List<DataInsightCustomChartResult>> rawResultList =
        processAggregationsInternal(aggregations, group);
    List<DataInsightCustomChartResult> finalResult = new ArrayList<>();
    for (List<DataInsightCustomChartResult> diResultList : rawResultList) {
      finalResult.addAll(diResultList);
    }
    return finalResult;
  }

  private List<List<DataInsightCustomChartResult>> processAggregationsInternal(
      List<Aggregation> aggregations, String group) {
    List<List<DataInsightCustomChartResult>> results = new ArrayList<>();
    for (Aggregation arg : aggregations) {
      ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) arg;
      for (Histogram.Bucket bucket : parsedDateHistogram.getBuckets()) {
        List<DataInsightCustomChartResult> subResults = new ArrayList<>();
        for (Aggregation subAggr : bucket.getAggregations().asList()) {
          addByAggregationType(
              subAggr,
              subResults,
              (double) ((ZonedDateTime) bucket.getKey()).toInstant().toEpochMilli(),
              group);
        }
        results.add(subResults);
      }
    }
    return results;
  }

  private void addByAggregationType(
      Aggregation subAggr,
      List<DataInsightCustomChartResult> diChartResults,
      Double day,
      String group) {
    if (subAggr instanceof ParsedValueCount)
      addProcessedSubResult((ParsedValueCount) subAggr, diChartResults, day, group);
    else if (subAggr instanceof ParsedCardinality)
      addProcessedSubResult((ParsedCardinality) subAggr, diChartResults, day, group);
    else if (subAggr instanceof ParsedSingleValueNumericMetricsAggregation)
      addProcessedSubResult(
          (ParsedSingleValueNumericMetricsAggregation) subAggr, diChartResults, day, group);
    else if (subAggr instanceof ParsedFilter)
      addProcessedSubResult((ParsedFilter) subAggr, diChartResults, day, group);
  }

  private void addProcessedSubResult(
      ParsedValueCount aggregation,
      List<DataInsightCustomChartResult> diChartResults,
      Double day,
      String group) {
    Double value = Double.valueOf((double) aggregation.getValue());
    if (!Double.isInfinite(value) && !Double.isNaN(value)) {
      DataInsightCustomChartResult diChartResult =
          new DataInsightCustomChartResult().withCount(value).withDay(day).withGroup(group);
      diChartResults.add(diChartResult);
    }
  }

  private void addProcessedSubResult(
      ParsedCardinality aggregation,
      List<DataInsightCustomChartResult> diChartResults,
      Double day,
      String group) {
    Double value = Double.valueOf((double) aggregation.getValue());
    if (!Double.isInfinite(value) && !Double.isNaN(value)) {
      DataInsightCustomChartResult diChartResult =
          new DataInsightCustomChartResult().withCount(value).withDay(day).withGroup(group);
      diChartResults.add(diChartResult);
    }
  }

  private void addProcessedSubResult(
      ParsedSingleValueNumericMetricsAggregation aggregation,
      List<DataInsightCustomChartResult> diChartResults,
      Double day,
      String group) {
    Double value = aggregation.value();
    if (!Double.isInfinite(value) && !Double.isNaN(value)) {
      DataInsightCustomChartResult diChartResult =
          new DataInsightCustomChartResult().withCount(value).withDay(day).withGroup(group);
      diChartResults.add(diChartResult);
    }
  }

  private void addProcessedSubResult(
      ParsedFilter aggregation,
      List<DataInsightCustomChartResult> diChartResults,
      Double day,
      String group) {
    for (Aggregation agg : aggregation.getAggregations().asList()) {
      addByAggregationType(agg, diChartResults, day, group);
    }
  }
}
