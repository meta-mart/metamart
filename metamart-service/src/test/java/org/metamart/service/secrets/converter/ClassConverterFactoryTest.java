package org.metamart.service.secrets.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.metamart.schema.auth.SSOAuthMechanism;
import org.metamart.schema.entity.automations.TestServiceConnectionRequest;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.metadataIngestion.DbtPipeline;
import org.metamart.schema.metadataIngestion.dbtconfig.DbtGCSConfig;
import org.metamart.schema.security.credentials.GCPCredentials;
import org.metamart.schema.services.connections.dashboard.LookerConnection;
import org.metamart.schema.services.connections.dashboard.SupersetConnection;
import org.metamart.schema.services.connections.dashboard.TableauConnection;
import org.metamart.schema.services.connections.database.BigQueryConnection;
import org.metamart.schema.services.connections.database.DatalakeConnection;
import org.metamart.schema.services.connections.database.IcebergConnection;
import org.metamart.schema.services.connections.database.MysqlConnection;
import org.metamart.schema.services.connections.database.PostgresConnection;
import org.metamart.schema.services.connections.database.SalesforceConnection;
import org.metamart.schema.services.connections.database.TrinoConnection;
import org.metamart.schema.services.connections.database.datalake.GCSConfig;
import org.metamart.schema.services.connections.pipeline.AirflowConnection;
import org.metamart.schema.services.connections.search.ElasticSearchConnection;
import org.metamart.schema.services.connections.storage.GCSConnection;

public class ClassConverterFactoryTest {

  @ParameterizedTest
  @ValueSource(
      classes = {
        AirflowConnection.class,
        BigQueryConnection.class,
        DatalakeConnection.class,
        MysqlConnection.class,
        PostgresConnection.class,
        DbtGCSConfig.class,
        DbtPipeline.class,
        GCSConfig.class,
        GCSConnection.class,
        ElasticSearchConnection.class,
        LookerConnection.class,
        SSOAuthMechanism.class,
        SupersetConnection.class,
        GCPCredentials.class,
        TableauConnection.class,
        TestServiceConnectionRequest.class,
        TrinoConnection.class,
        Workflow.class,
        SalesforceConnection.class,
        IcebergConnection.class,
      })
  void testClassConverterIsSet(Class<?> clazz) {
    assertFalse(
        ClassConverterFactory.getConverter(clazz) instanceof DefaultConnectionClassConverter);
  }

  @Test
  void testClassConvertedMapIsNotModified() {
    assertEquals(26, ClassConverterFactory.getConverterMap().size());
  }
}
