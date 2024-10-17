package org.metamart.service.jdbi3;

import org.metamart.schema.entity.services.SearchService;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.type.SearchConnection;
import org.metamart.service.Entity;
import org.metamart.service.resources.services.storage.StorageServiceResource;

public class SearchServiceRepository
    extends ServiceEntityRepository<SearchService, SearchConnection> {
  public SearchServiceRepository() {
    super(
        StorageServiceResource.COLLECTION_PATH,
        Entity.SEARCH_SERVICE,
        Entity.getCollectionDAO().searchServiceDAO(),
        SearchConnection.class,
        "",
        ServiceType.SEARCH);
    supportsSearch = true;
  }
}
