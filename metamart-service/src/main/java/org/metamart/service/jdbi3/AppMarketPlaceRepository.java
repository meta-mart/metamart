package org.metamart.service.jdbi3;

import org.metamart.schema.entity.app.AppMarketPlaceDefinition;
import org.metamart.service.Entity;
import org.metamart.service.resources.apps.AppMarketPlaceResource;
import org.metamart.service.util.EntityUtil;

public class AppMarketPlaceRepository extends EntityRepository<AppMarketPlaceDefinition> {

  public AppMarketPlaceRepository() {
    super(
        AppMarketPlaceResource.COLLECTION_PATH,
        Entity.APP_MARKET_PLACE_DEF,
        AppMarketPlaceDefinition.class,
        Entity.getCollectionDAO().applicationMarketPlaceDAO(),
        "",
        "");
    supportsSearch = false;
    quoteFqn = true;
  }

  @Override
  public void setFields(AppMarketPlaceDefinition entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void clearFields(AppMarketPlaceDefinition entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void prepare(AppMarketPlaceDefinition entity, boolean update) {}

  @Override
  public void storeEntity(AppMarketPlaceDefinition entity, boolean update) {
    store(entity, update);
  }

  @Override
  public void storeRelationships(AppMarketPlaceDefinition entity) {}
}
