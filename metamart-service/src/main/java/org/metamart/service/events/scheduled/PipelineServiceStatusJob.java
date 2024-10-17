package org.metamart.service.events.scheduled;

import static org.metamart.sdk.PipelineServiceClientInterface.HEALTHY_STATUS;
import static org.metamart.sdk.PipelineServiceClientInterface.STATUS_KEY;
import static org.metamart.service.events.scheduled.PipelineServiceStatusJobHandler.JOB_CONTEXT_CLUSTER_NAME;
import static org.metamart.service.events.scheduled.PipelineServiceStatusJobHandler.JOB_CONTEXT_METER_REGISTRY;
import static org.metamart.service.events.scheduled.PipelineServiceStatusJobHandler.JOB_CONTEXT_PIPELINE_SERVICE_CLIENT;

import io.micrometer.core.instrument.Counter;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.metamart.sdk.PipelineServiceClientInterface;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

@Slf4j
public class PipelineServiceStatusJob implements Job {
  private static final String COUNTER_NAME = "pipelineServiceClientStatus.counter";
  private static final String CLUSTER_TAG_NAME = "clusterName";
  private static final String UNHEALTHY_TAG_NAME = "unhealthy";

  @Override
  public void execute(JobExecutionContext jobExecutionContext) {

    PipelineServiceClientInterface pipelineServiceClient =
        (PipelineServiceClientInterface)
            jobExecutionContext
                .getJobDetail()
                .getJobDataMap()
                .get(JOB_CONTEXT_PIPELINE_SERVICE_CLIENT);
    PrometheusMeterRegistry meterRegistry =
        (PrometheusMeterRegistry)
            jobExecutionContext.getJobDetail().getJobDataMap().get(JOB_CONTEXT_METER_REGISTRY);
    String clusterName =
        (String) jobExecutionContext.getJobDetail().getJobDataMap().get(JOB_CONTEXT_CLUSTER_NAME);
    try {
      registerStatusMetric(pipelineServiceClient, meterRegistry, clusterName);
    } catch (Exception e) {
      LOG.error("[Pipeline Service Status Job] Failed in sending metric due to", e);
      publishUnhealthyCounter(meterRegistry, clusterName);
    }
  }

  private void registerStatusMetric(
      PipelineServiceClientInterface pipelineServiceClient,
      PrometheusMeterRegistry meterRegistry,
      String clusterName) {
    String status = pipelineServiceClient.getServiceStatusBackoff();
    if (!HEALTHY_STATUS.equals(status)) {
      publishUnhealthyCounter(meterRegistry, clusterName);
    }
  }

  private void publishUnhealthyCounter(PrometheusMeterRegistry meterRegistry, String clusterName) {
    Counter.builder(COUNTER_NAME)
        .tags(STATUS_KEY, UNHEALTHY_TAG_NAME, CLUSTER_TAG_NAME, clusterName)
        .register(meterRegistry)
        .increment();
  }
}
