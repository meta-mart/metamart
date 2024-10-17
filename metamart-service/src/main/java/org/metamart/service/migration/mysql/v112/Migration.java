package org.metamart.service.migration.mysql.v112;

import static org.metamart.service.migration.utils.V112.MigrationUtil.fixExecutableTestSuiteFQN;
import static org.metamart.service.migration.utils.V112.MigrationUtil.lowerCaseUserNameAndEmail;

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
    // Run Data Migration to Remove the quoted Fqn`
    fixExecutableTestSuiteFQN(collectionDAO);
    // Run UserName Migration to make lowercase
    lowerCaseUserNameAndEmail(collectionDAO);
  }
}
