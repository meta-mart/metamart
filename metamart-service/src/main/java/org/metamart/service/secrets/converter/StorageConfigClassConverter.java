package org.metamart.service.secrets.converter;

import java.util.List;
import org.metamart.schema.services.connections.database.datalake.S3Config;
import org.metamart.schema.services.connections.database.deltalake.StorageConfig;
import org.metamart.service.util.JsonUtils;

/** Converter class to get an `StorageConfig` object for DeltaLakeConnection. */
public class StorageConfigClassConverter extends ClassConverter {
  private static final List<Class<?>> CONNECTION_CLASSES = List.of(S3Config.class);

  public StorageConfigClassConverter() {
    super(StorageConfig.class);
  }

  @Override
  public Object convert(Object object) {
    StorageConfig storageConfig = (StorageConfig) JsonUtils.convertValue(object, this.clazz);

    tryToConvertOrFail(storageConfig.getConnection(), CONNECTION_CLASSES)
        .ifPresent(storageConfig::setConnection);

    return storageConfig;
  }
}
