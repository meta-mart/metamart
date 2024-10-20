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

import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.Entity;
import org.metamart.service.events.AbstractEventPublisher;
import org.metamart.service.resources.events.EventResource;

@Slf4j
public class EventMonitorPublisher extends AbstractEventPublisher {

  private final EventMonitor eventMonitor;

  public EventMonitorPublisher(EventMonitorConfiguration config, EventMonitor eventMonitor) {
    super(config.getBatchSize());
    this.eventMonitor = eventMonitor;
  }

  @Override
  public void publish(EventResource.EventList events) {
    for (ChangeEvent event : events.getData()) {
      String entityType = event.getEntityType();
      if (Entity.INGESTION_PIPELINE.equals(entityType)) {
        this.eventMonitor.pushMetric(event);
      }
    }
  }

  @Override
  public void onStart() {
    LOG.info("Event Monitor Publisher Started");
  }

  @Override
  public void onShutdown() {
    eventMonitor.close();
    LOG.info("Event Monitor Publisher Closed");
  }
}
