package org.metamart.service.jdbi3;

import org.metamart.schema.entity.services.ApiService;
import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.type.ApiConnection;
import org.metamart.service.Entity;
import org.metamart.service.resources.services.apiservices.APIServiceResource;

public class APIServiceRepository extends ServiceEntityRepository<ApiService, ApiConnection> {
  public APIServiceRepository() {
    super(
        APIServiceResource.COLLECTION_PATH,
        Entity.API_SERVICE,
        Entity.getCollectionDAO().apiServiceDAO(),
        ApiConnection.class,
        "",
        ServiceType.API);
    supportsSearch = true;
  }
}
