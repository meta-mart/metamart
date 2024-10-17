/*
 *  Copyright 2021 DigiTrans
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

package org.metamart.service.events;

import java.util.UUID;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.EntityTimeSeriesInterface;
import org.metamart.schema.entity.feed.Thread;
import org.metamart.schema.type.AuditLog;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.Entity;
import org.metamart.service.MetaMartApplicationConfig;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

@Slf4j
public class AuditEventHandler implements EventHandler {
  private final Marker auditMarker = MarkerFactory.getMarker("AUDIT");

  public void init(MetaMartApplicationConfig config) {
    // Nothing to do
  }

  public Void process(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    int responseCode = responseContext.getStatus();
    String method = requestContext.getMethod();
    if (responseContext.getEntity() != null) {
      String path = requestContext.getUriInfo().getPath();
      String username = "anonymous";
      if (requestContext.getSecurityContext().getUserPrincipal() != null) {
        username = requestContext.getSecurityContext().getUserPrincipal().getName();
      }
      try {
        // TODO: EntityInterface and EntityTimeSeriesInterface share some common implementation and
        // diverge at the edge (e.g. EntityTimeSeriesInterface does not expect owners, etc.).
        // We should implement a parent class that captures the common fields and then have
        // EntityInterface and EntityTimeSeriesInterface extend it.
        // TODO: if we are just interested in entity's we can just do else and return null.
        UUID entityId;
        String entityType;
        if (responseContext.getEntity()
            instanceof EntityTimeSeriesInterface entityTimeSeriesInterface) {
          entityId = entityTimeSeriesInterface.getEntityReference().getId();
          entityType = entityTimeSeriesInterface.getEntityReference().getType();
        } else if (responseContext.getEntity() instanceof EntityInterface entityInterface) {
          entityId = entityInterface.getEntityReference().getId();
          entityType = entityInterface.getEntityReference().getType();
        } else if (responseContext.getEntity() instanceof ChangeEvent changeEvent) {
          entityId = changeEvent.getId();
          entityType = "CHANGE_EVENT";
        } else if (responseContext.getEntity() instanceof Thread thread) {
          entityId = thread.getId();
          entityType = Entity.THREAD;
        } else {
          return null;
        }
        AuditLog auditLog =
            new AuditLog()
                .withPath(path)
                .withTimestamp(System.currentTimeMillis())
                .withEntityId(entityId)
                .withEntityType(entityType)
                .withMethod(AuditLog.Method.fromValue(method))
                .withUserName(username)
                .withResponseCode(responseCode);
        LOG.info(auditMarker, String.format("Added audit log entry: %s", auditLog));
      } catch (Exception e) {
        LOG.error(
            auditMarker,
            String.format(
                "Failed to capture audit log for %s and method %s due to %s",
                path, method, e.getMessage()));
      }
    }
    return null;
  }

  public void close() {
    /* Nothing to do */
  }
}
