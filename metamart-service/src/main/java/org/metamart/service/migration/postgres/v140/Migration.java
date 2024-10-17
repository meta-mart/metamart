package org.metamart.service.migration.postgres.v140;

import static org.metamart.service.migration.utils.v140.MigrationUtil.migrateGenericToWebhook;
import static org.metamart.service.migration.utils.v140.MigrationUtil.migrateTablePartition;
import static org.metamart.service.migration.utils.v140.MigrationUtil.migrateTestCaseResolution;

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
    // Migrate Table Partition
    migrateTablePartition(handle, collectionDAO);

    // Migrate Generic to Webhook
    migrateGenericToWebhook(collectionDAO);

    // Migrate Test case resolution status
    migrateTestCaseResolution(handle, collectionDAO);
  }
}
