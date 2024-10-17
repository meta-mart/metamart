package org.metamart.service.migration.utils.v132;

import java.util.LinkedHashMap;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Handle;
import org.json.JSONObject;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtAzureConfig;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtCloudConfig;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtGCSConfig;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtHttpConfig;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtLocalConfig;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtS3Config;
import org.metamart.service.exception.UnhandledServerException;
import org.metamart.service.util.JsonUtils;

@Slf4j
public class MigrationUtil {

  private MigrationUtil() {
    /* Cannot create object  util class*/
  }

  public static void migrateDbtConfigType(
      Handle handle, String updateSqlQuery, String dbtGetDbtPipelinesQuery) {
    handle
        .createQuery(dbtGetDbtPipelinesQuery)
        .mapToMap()
        .forEach(
            row -> {
              try {
                IngestionPipeline ingestionPipeline =
                    JsonUtils.readValue(row.get("json").toString(), IngestionPipeline.class);
                String id = row.get("id").toString();
                LinkedHashMap sourceConfig =
                    (LinkedHashMap) ingestionPipeline.getSourceConfig().getConfig();
                LinkedHashMap dbtConfigSource = (LinkedHashMap) sourceConfig.get("dbtConfigSource");

                sourceConfig.put("dbtConfigSource", addDbtConfigType(dbtConfigSource));
                String json = JsonUtils.pojoToJson(ingestionPipeline);

                handle.createUpdate(updateSqlQuery).bind("json", json).bind("id", id).execute();

              } catch (Exception ex) {
                LOG.warn("Error during the dbt type migration due to ", ex);
              }
            });
  }

  public static Object addDbtConfigType(LinkedHashMap dbtConfigSource) {
    String jsonString = new JSONObject(dbtConfigSource).toString();

    // For adding s3 type
    try {
      DbtS3Config dbtS3Config = JsonUtils.readValue(jsonString, DbtS3Config.class);
      dbtS3Config.setDbtConfigType(DbtS3Config.DbtConfigType.S_3);
      return dbtS3Config;
    } catch (UnhandledServerException ex) {
    }

    // For adding GCS type
    try {
      DbtGCSConfig dbtGCSConfig = JsonUtils.readValue(jsonString, DbtGCSConfig.class);
      dbtGCSConfig.setDbtConfigType(DbtGCSConfig.DbtConfigType.GCS);
      return dbtGCSConfig;
    } catch (UnhandledServerException ex) {
    }

    // For adding Azure type
    try {
      DbtAzureConfig dbtAzureConfig = JsonUtils.readValue(jsonString, DbtAzureConfig.class);
      dbtAzureConfig.setDbtConfigType(DbtAzureConfig.DbtConfigType.AZURE);
      return dbtAzureConfig;
    } catch (UnhandledServerException ex) {
    }

    // For adding cloud type
    try {
      DbtCloudConfig dbtCloudConfig = JsonUtils.readValue(jsonString, DbtCloudConfig.class);
      dbtCloudConfig.setDbtConfigType(DbtCloudConfig.DbtConfigType.CLOUD);
      return dbtCloudConfig;
    } catch (UnhandledServerException ex) {
    }

    // For adding local type
    try {
      DbtLocalConfig dbtLocalConfig = JsonUtils.readValue(jsonString, DbtLocalConfig.class);
      dbtLocalConfig.setDbtConfigType(DbtLocalConfig.DbtConfigType.LOCAL);
      return dbtLocalConfig;
    } catch (UnhandledServerException ex) {
    }

    // For adding http type
    try {
      DbtHttpConfig dbtHttpConfig = JsonUtils.readValue(jsonString, DbtHttpConfig.class);
      dbtHttpConfig.setDbtConfigType(DbtHttpConfig.DbtConfigType.HTTP);
      return dbtHttpConfig;
    } catch (UnhandledServerException ex) {
    }
    return null;
  }
}
