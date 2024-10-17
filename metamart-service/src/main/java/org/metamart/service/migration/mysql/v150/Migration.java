package org.metamart.service.migration.mysql.v150;

import static org.metamart.service.migration.utils.v150.MigrationUtil.createSystemDICharts;
import static org.metamart.service.migration.utils.v150.MigrationUtil.deleteLegacyDataInsightPipelines;
import static org.metamart.service.migration.utils.v150.MigrationUtil.migrateAutomatorOwner;
import static org.metamart.service.migration.utils.v150.MigrationUtil.migratePolicies;
import static org.metamart.service.migration.utils.v150.MigrationUtil.migrateTestCaseDimension;
import static org.metamart.service.migration.utils.v150.MigrationUtil.updateDataInsightsApplication;

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
    migratePolicies(handle, collectionDAO);
    migrateTestCaseDimension(handle, collectionDAO);
    createSystemDICharts();
    deleteLegacyDataInsightPipelines(pipelineServiceClient);
    updateDataInsightsApplication();
    migrateAutomatorOwner(handle, collectionDAO);
  }
}
