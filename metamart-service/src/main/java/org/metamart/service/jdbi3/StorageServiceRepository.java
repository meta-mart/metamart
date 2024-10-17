package org.metamart.service.jdbi3;

import org.metamart.schema.entity.services.ServiceType;
import org.metamart.schema.entity.services.StorageService;
import org.metamart.schema.type.StorageConnection;
import org.metamart.service.Entity;
import org.metamart.service.resources.services.storage.StorageServiceResource;

public class StorageServiceRepository
    extends ServiceEntityRepository<StorageService, StorageConnection> {
  public StorageServiceRepository() {
    super(
        StorageServiceResource.COLLECTION_PATH,
        Entity.STORAGE_SERVICE,
        Entity.getCollectionDAO().storageServiceDAO(),
        StorageConnection.class,
        "",
        ServiceType.STORAGE);
    supportsSearch = true;
  }
}
