package org.metamart.service.migration.postgres.v153;

import static org.metamart.service.migration.utils.v153.MigrationUtil.updateEmailTemplates;

import lombok.SneakyThrows;
import org.metamart.service.migration.api.MigrationProcessImpl;
import org.metamart.service.migration.utils.MigrationFile;

public class Migration extends MigrationProcessImpl {

  public Migration(MigrationFile migrationFile) {
    super(migrationFile);
  }

  @Override
  @SneakyThrows
  public void runDataMigration() {
    updateEmailTemplates(collectionDAO);
  }
}
