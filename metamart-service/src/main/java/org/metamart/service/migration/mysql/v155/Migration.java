package org.metamart.service.migration.mysql.v155;

import static org.metamart.service.migration.utils.v155.MigrationUtil.updateUserNameToEmailPrefixForLdapAuthProvider;

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
    updateUserNameToEmailPrefixForLdapAuthProvider(
        handle, collectionDAO, authenticationConfiguration, false);
  }
}
