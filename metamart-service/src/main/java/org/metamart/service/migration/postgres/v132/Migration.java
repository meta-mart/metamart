package org.metamart.service.migration.postgres.v132;

import static org.metamart.service.migration.utils.v132.MigrationUtil.migrateDbtConfigType;

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
    String getDbtPipelinesQuery =
        "SELECT * from ingestion_pipeline_entity ipe WHERE json #>> '{pipelineType}' = 'dbt'";
    String updateSqlQuery =
        "UPDATE ingestion_pipeline_entity ipe SET json = :json::jsonb "
            + "WHERE json #>> '{pipelineType}' = 'dbt'"
            + "AND id = :id";
    migrateDbtConfigType(handle, updateSqlQuery, getDbtPipelinesQuery);
  }
}
