package org.metamart.service.migration.mysql.v117;

import static org.metamart.service.migration.utils.V114.MigrationUtil.fixTestSuites;
import static org.metamart.service.migration.utils.V117.MigrationUtil.fixTestCases;

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
    // testcases coming from dbt for case-sensitive services are not properly linked to a table
    fixTestCases(handle, collectionDAO);
    // Try again the 1.1.6 test suite migration
    fixTestSuites(collectionDAO);
  }
}
