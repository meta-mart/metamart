package org.metamart.service.apps.scheduler;

import org.metamart.service.jdbi3.CollectionDAO;

public class OmAppJobListener extends AbstractOmAppJobListener {
  protected OmAppJobListener(CollectionDAO dao) {
    super(dao);
  }
}
