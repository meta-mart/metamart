package org.metamart.service.migration.mysql.v116;

import static org.metamart.service.migration.utils.V112.MigrationUtil.lowerCaseUserNameAndEmail;
import static org.metamart.service.migration.utils.V114.MigrationUtil.fixTestSuites;

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
    fixTestSuites(collectionDAO);
    lowerCaseUserNameAndEmail(collectionDAO);
  }
}
