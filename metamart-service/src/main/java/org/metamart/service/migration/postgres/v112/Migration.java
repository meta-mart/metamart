package org.metamart.service.migration.postgres.v112;

import static org.metamart.service.migration.utils.V112.MigrationUtil.fixExecutableTestSuiteFQN;
import static org.metamart.service.migration.utils.V112.MigrationUtil.lowerCaseUserNameAndEmail;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.metamart.service.migration.api.MigrationProcessImpl;
import org.metamart.service.migration.utils.MigrationFile;

@Slf4j
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
