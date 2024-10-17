package org.metamart.service.search.security;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.metamart.service.search.queries.OMQueryBuilder;
import org.metamart.service.search.queries.QueryBuilderFactory;

public class ConditionCollector {
  private final QueryBuilderFactory queryBuilderFactory;
  private final List<OMQueryBuilder> mustQueries = new ArrayList<>();
  private final List<OMQueryBuilder> mustNotQueries = new ArrayList<>();
  private final List<OMQueryBuilder> shouldQueries = new ArrayList<>();

  @Getter @Setter private boolean matchNothing = false;

  public ConditionCollector(QueryBuilderFactory queryBuilderFactory) {
    this.queryBuilderFactory = queryBuilderFactory;
  }

  public void addMust(OMQueryBuilder query) {
    if (query != null && !query.isEmpty()) {
      if (query.isMatchNone()) {
        this.setMatchNothing(true);
      } else {
        mustQueries.add(query);
      }
    }
  }

  public void addShould(OMQueryBuilder query) {
    if (query != null && !query.isEmpty() && !query.isMatchNone()) {
      shouldQueries.add(query);
    }
  }

  public void addMustNot(OMQueryBuilder query) {
    if (query != null && !query.isEmpty()) {
      if (query.isMatchAll()) {
        this.setMatchNothing(true);
      } else {
        mustNotQueries.add(query);
      }
    }
  }

  public boolean isMatchAllQuery() {
    if (mustQueries.size() == 1 && shouldQueries.isEmpty() && mustNotQueries.isEmpty()) {
      OMQueryBuilder query = mustQueries.get(0);
      return query.isMatchAll();
    }
    return false;
  }

  public OMQueryBuilder buildFinalQuery() {
    if (matchNothing) {
      return queryBuilderFactory.matchNoneQuery();
    }

    if (mustQueries.size() == 1 && shouldQueries.isEmpty() && mustNotQueries.isEmpty()) {
      return mustQueries.get(0);
    }

    OMQueryBuilder combinedQuery = queryBuilderFactory.boolQuery();

    if (!mustQueries.isEmpty()) {
      combinedQuery.must(mustQueries);
    }

    if (!shouldQueries.isEmpty()) {
      combinedQuery.should(shouldQueries);
    }

    if (!mustNotQueries.isEmpty()) {
      combinedQuery.mustNot(mustNotQueries);
    }

    return combinedQuery.hasClauses() ? combinedQuery : null;
  }
}
