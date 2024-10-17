package org.metamart.service.migration.mysql.v133;

import static org.metamart.service.migration.utils.v131.MigrationUtil.migrateCronExpression;

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
    migrateCronExpression(collectionDAO);
  }
}
