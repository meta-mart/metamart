package org.metamart.service.apps.bundles.test;

import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.entity.app.App;
import org.metamart.service.apps.AbstractNativeApplication;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.search.SearchRepository;

@Slf4j
@SuppressWarnings("unused")
public class NoOpTestApplication extends AbstractNativeApplication {

  public NoOpTestApplication(CollectionDAO collectionDAO, SearchRepository searchRepository) {
    super(collectionDAO, searchRepository);
  }

  @Override
  public void init(App app) {
    super.init(app);
    LOG.info("NoOpTestApplication is initialized");
  }
}
