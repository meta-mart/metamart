package org.metamart.service.migration.mysql.v160;

import static org.metamart.service.migration.utils.v160.MigrationUtil.addAppExtensionName;
import static org.metamart.service.migration.utils.v160.MigrationUtil.migrateServiceTypesAndConnections;

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
    addAppExtensionName(handle, collectionDAO, authenticationConfiguration, false);
    migrateServiceTypesAndConnections(handle, false);
  }
}
