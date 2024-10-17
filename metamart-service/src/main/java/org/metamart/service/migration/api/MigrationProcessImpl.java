package org.metamart.service.migration.api;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.util.EntityUtil.hash;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.metamart.schema.api.security.AuthenticationConfiguration;
import org.metamart.sdk.PipelineServiceClientInterface;
import org.metamart.service.clients.pipeline.PipelineServiceClientFactory;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.jdbi3.MigrationDAO;
import org.metamart.service.migration.QueryStatus;
import org.metamart.service.migration.context.MigrationContext;
import org.metamart.service.migration.context.MigrationOps;
import org.metamart.service.migration.utils.MigrationFile;

@Slf4j
public class MigrationProcessImpl implements MigrationProcess {
  protected MigrationDAO migrationDAO;
  protected CollectionDAO collectionDAO;
  protected Jdbi jdbi;
  protected Handle handle;
  protected PipelineServiceClientInterface pipelineServiceClient;
  protected AuthenticationConfiguration authenticationConfiguration;
  private final MigrationFile migrationFile;

  public @Getter MigrationContext context;

  public MigrationProcessImpl(MigrationFile migrationFile) {
    this.migrationFile = migrationFile;
  }

  @Override
  public void initialize(Handle handle, Jdbi jdbi) {
    this.handle = handle;
    this.jdbi = jdbi;
    this.collectionDAO = handle.attach(CollectionDAO.class);
    this.migrationDAO = handle.attach(MigrationDAO.class);
    this.pipelineServiceClient =
        PipelineServiceClientFactory.createPipelineServiceClient(
            this.migrationFile.pipelineServiceClientConfiguration);
    this.authenticationConfiguration = migrationFile.authenticationConfiguration;
  }

  @Override
  public List<MigrationOps> getMigrationOps() {
    return List.of();
  }

  @Override
  public String getDatabaseConnectionType() {
    return migrationFile.connectionType.label;
  }

  @Override
  public String getVersion() {
    return migrationFile.version;
  }

  @Override
  public String getMigrationsPath() {
    return migrationFile.getMigrationsFilePath();
  }

  @Override
  public String getSchemaChangesFilePath() {
    return migrationFile.getSchemaChangesFile();
  }

  @Override
  public String getPostDDLScriptFilePath() {
    return migrationFile.getPostDDLScriptFile();
  }

  @Override
  public Map<String, QueryStatus> runSchemaChanges(boolean isForceMigration) {
    return performSqlExecutionAndUpdate(
        handle,
        migrationDAO,
        migrationFile.getSchemaChanges(),
        migrationFile.version,
        isForceMigration);
  }

  public static Map<String, QueryStatus> performSqlExecutionAndUpdate(
      Handle handle,
      MigrationDAO migrationDAO,
      List<String> queryList,
      String version,
      boolean isForceMigration) {
    // These are DDL Statements and will cause an Implicit commit even if part of transaction still
    // committed inplace
    Map<String, QueryStatus> queryStatusMap = new HashMap<>();
    if (!nullOrEmpty(queryList)) {
      for (String sql : queryList) {
        try {
          String previouslyRanSql = migrationDAO.getSqlQuery(hash(sql), version);
          if ((previouslyRanSql == null || previouslyRanSql.isEmpty())) {
            handle.execute(sql);
            migrationDAO.upsertServerMigrationSQL(version, sql, hash(sql));
          }
          queryStatusMap.put(
              sql, new QueryStatus(QueryStatus.Status.SUCCESS, "Successfully Executed Query"));
        } catch (Exception e) {
          String message = String.format("Failed to run sql: [%s] due to [%s]", sql, e);
          queryStatusMap.put(sql, new QueryStatus(QueryStatus.Status.FAILURE, message));
          if (!isForceMigration) {
            throw new RuntimeException(message, e);
          }
        }
      }
    }
    return queryStatusMap;
  }

  @Override
  public void runDataMigration() {}

  @Override
  public Map<String, QueryStatus> runPostDDLScripts(boolean isForceMigration) {
    return performSqlExecutionAndUpdate(
        handle,
        migrationDAO,
        migrationFile.getPostDDLScripts(),
        migrationFile.version,
        isForceMigration);
  }

  @Override
  public void close() {
    if (handle != null) {
      handle.close();
    }
  }
}
