/*
 *  Copyright 2022 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.monitoring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.metamart.service.resources.services.ingestionpipelines.IngestionPipelineResourceTest.DATABASE_METADATA_CONFIG;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.metamart.schema.entity.services.ingestionPipelines.AirflowConfig;
import org.metamart.schema.entity.services.ingestionPipelines.IngestionPipeline;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineStatus;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineStatusType;
import org.metamart.schema.entity.services.ingestionPipelines.PipelineType;
import org.metamart.schema.monitoring.EventMonitorProvider;
import org.metamart.schema.type.ChangeDescription;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.schema.type.EventType;
import org.metamart.schema.type.FieldChange;
import org.metamart.service.Entity;
import software.amazon.awssdk.services.cloudwatch.model.Dimension;
import software.amazon.awssdk.services.cloudwatch.model.MetricDatum;
import software.amazon.awssdk.services.cloudwatch.model.PutMetricDataRequest;
import software.amazon.awssdk.services.cloudwatch.model.StandardUnit;

public class CloudWatchEventMonitorTest {

  private static final String CLUSTER_NAME = "metamart";
  private static final String NAMESPACE = "INGESTION_PIPELINE";
  private static final String EXPECTED_NAMESPACE = "metamart/INGESTION_PIPELINE";
  private static final String FQN = "service.ingestion";

  private static CloudwatchEventMonitor eventMonitor;

  public static final Long current_ts = System.currentTimeMillis();
  public static final Instant instant = Instant.ofEpochMilli(current_ts);

  public static final IngestionPipeline INGESTION_PIPELINE =
      new IngestionPipeline()
          .withName("ingestion")
          .withId(UUID.randomUUID())
          .withPipelineType(PipelineType.METADATA)
          .withSourceConfig(DATABASE_METADATA_CONFIG)
          .withAirflowConfig(
              new AirflowConfig()
                  .withStartDate(new DateTime("2022-06-10T15:06:47+00:00").toDate()));

  private ChangeEvent buildChangeEvent(EventType eventType) {
    return new ChangeEvent()
        .withId(UUID.randomUUID())
        .withEntityType(Entity.INGESTION_PIPELINE)
        .withEventType(eventType)
        .withEntityFullyQualifiedName(FQN)
        .withTimestamp(current_ts)
        .withEntity(INGESTION_PIPELINE);
  }

  private Dimension buildDimension(String pipelineType, String fqn) {
    return Dimension.builder().name(pipelineType).value(fqn).build();
  }

  @BeforeAll
  static void setUp() {
    EventMonitorConfiguration config = new EventMonitorConfiguration();
    config.setEventMonitor(EventMonitorProvider.CLOUDWATCH);
    config.setBatchSize(10);
    config.setParameters(
        new HashMap<>() {
          {
            put("region", "eu-west-2");
            put("accessKeyId", "asdf1234");
            put("secretAccessKey", "asdf1234");
          }
        });
    eventMonitor =
        new CloudwatchEventMonitor(EventMonitorProvider.CLOUDWATCH, config, CLUSTER_NAME);
  }

  @Test
  void buildMetricNamespaceTest() {
    assertEquals(EXPECTED_NAMESPACE, eventMonitor.buildMetricNamespace(NAMESPACE));
  }

  @Test
  void buildMetricRequestForCreatedIngestionPipelineTest() {
    ChangeEvent event = buildChangeEvent(EventType.ENTITY_CREATED);
    List<PutMetricDataRequest> metricRequests = eventMonitor.buildMetricRequest(event);

    PutMetricDataRequest expectedMetric =
        PutMetricDataRequest.builder()
            .namespace(EXPECTED_NAMESPACE)
            .metricData(
                MetricDatum.builder()
                    .metricName("INGESTION_PIPELINE_CREATED")
                    .unit(StandardUnit.COUNT)
                    .value(1.0)
                    .timestamp(instant)
                    .dimensions(buildDimension("metadata", FQN))
                    .build())
            .build();

    assertEquals(metricRequests.get(0), expectedMetric);
  }

  @Test
  void buildMetricRequestForDeletedIngestionPipelineTest() {
    ChangeEvent event = buildChangeEvent(EventType.ENTITY_DELETED);
    List<PutMetricDataRequest> metricRequests = eventMonitor.buildMetricRequest(event);

    PutMetricDataRequest expectedMetric =
        PutMetricDataRequest.builder()
            .namespace(EXPECTED_NAMESPACE)
            .metricData(
                MetricDatum.builder()
                    .metricName("INGESTION_PIPELINE_DELETED")
                    .unit(StandardUnit.COUNT)
                    .value(1.0)
                    .timestamp(instant)
                    .dimensions(buildDimension("metadata", FQN))
                    .build())
            .build();

    assertEquals(metricRequests.get(0), expectedMetric);
  }

  @Test
  void buildMetricRequestForUpdatedIngestionPipelineTest() {
    ChangeEvent event = buildChangeEvent(EventType.ENTITY_UPDATED);
    event.withChangeDescription(
        new ChangeDescription()
            .withFieldsUpdated(
                List.of(
                    new FieldChange()
                        .withName("pipelineStatus")
                        .withOldValue(null)
                        .withNewValue(
                            new PipelineStatus().withPipelineState(PipelineStatusType.RUNNING)))));

    List<PutMetricDataRequest> metricRequests = eventMonitor.buildMetricRequest(event);

    PutMetricDataRequest expectedMetric =
        PutMetricDataRequest.builder()
            .namespace(EXPECTED_NAMESPACE)
            .metricData(
                MetricDatum.builder()
                    .metricName("INGESTION_PIPELINE_RUNNING")
                    .unit(StandardUnit.COUNT)
                    .value(1.0)
                    .timestamp(instant)
                    .dimensions(buildDimension("metadata", FQN))
                    .build())
            .build();

    assertEquals(metricRequests.get(0), expectedMetric);
  }
}
