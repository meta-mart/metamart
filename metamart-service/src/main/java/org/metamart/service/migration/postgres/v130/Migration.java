package org.metamart.service.migration.postgres.v130;

import static org.metamart.service.migration.utils.v130.MigrationUtil.migrateMongoDBConnStr;

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
    String updateSqlQuery =
        "UPDATE  dbservice_entity de SET json = :json::jsonb "
            + "WHERE serviceType = 'MongoDB' "
            + "AND id = :id";
    migrateMongoDBConnStr(handle, updateSqlQuery);
  }
}
