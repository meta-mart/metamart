package org.metamart.service.resources;

import java.io.IOException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import lombok.Getter;
import org.metamart.schema.EntityTimeSeriesInterface;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.jdbi3.EntityTimeSeriesRepository;
import org.metamart.service.search.SearchListFilter;
import org.metamart.service.search.SearchSortFilter;
import org.metamart.service.security.Authorizer;
import org.metamart.service.security.policyevaluator.OperationContext;
import org.metamart.service.security.policyevaluator.ResourceContextInterface;
import org.metamart.service.util.EntityUtil;
import org.metamart.service.util.ResultList;

public abstract class EntityTimeSeriesResource<
    T extends EntityTimeSeriesInterface, K extends EntityTimeSeriesRepository<T>> {
  protected final Class<T> entityClass;
  protected final String entityType;
  @Getter protected final K repository;
  protected final Authorizer authorizer;

  public EntityTimeSeriesResource(String entityType, Authorizer authorizer) {
    this.entityType = entityType;
    this.entityClass = (Class<T>) Entity.getEntityClassFromType(entityType);
    this.repository = (K) Entity.getEntityTimeSeriesRepository(entityType);
    this.authorizer = authorizer;
    Entity.registerTimeSeriesResourcePermissions(entityType);
  }

  public void initialize(MetaMartApplicationConfig config) {
    // Nothing to do in the default implementation
  }

  protected Response create(T entity, String extension, String recordFQN) {
    entity = repository.createNewRecord(entity, extension, recordFQN);
    return Response.ok(entity).build();
  }

  protected Response create(T entity, String recordFQN) {
    entity = repository.createNewRecord(entity, recordFQN);
    return Response.ok(entity).build();
  }

  protected ResultList<T> listInternalFromSearch(
      SecurityContext securityContext,
      EntityUtil.Fields fields,
      SearchListFilter searchListFilter,
      int limit,
      int offset,
      SearchSortFilter searchSortFilter,
      String q,
      OperationContext operationContext,
      ResourceContextInterface resourceContext)
      throws IOException {
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.listFromSearchWithOffset(
        fields, searchListFilter, limit, offset, searchSortFilter, q);
  }

  public ResultList<T> listLatestFromSearch(
      SecurityContext securityContext,
      EntityUtil.Fields fields,
      SearchListFilter searchListFilter,
      String groupBy,
      String q,
      OperationContext operationContext,
      ResourceContextInterface resourceContext)
      throws IOException {
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.listLatestFromSearch(fields, searchListFilter, groupBy, q);
  }

  protected T latestInternalFromSearch(
      SecurityContext securityContext,
      EntityUtil.Fields fields,
      SearchListFilter searchListFilter,
      String q,
      OperationContext operationContext,
      ResourceContextInterface resourceContext)
      throws IOException {
    authorizer.authorize(securityContext, operationContext, resourceContext);
    return repository.latestFromSearch(fields, searchListFilter, q);
  }
}
