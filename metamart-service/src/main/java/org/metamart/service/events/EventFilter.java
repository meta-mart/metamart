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

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.metamart.service.MetaMartApplicationConfig;
import org.metamart.service.security.JwtFilter;
import org.metamart.service.util.ParallelStreamUtil;

@Slf4j
@Provider
public class EventFilter implements ContainerResponseFilter {
  private static final List<String> AUDITABLE_METHODS =
      Arrays.asList("POST", "PUT", "PATCH", "DELETE");
  private static final int FORK_JOIN_POOL_PARALLELISM = 20;
  private final ForkJoinPool forkJoinPool;
  private final List<EventHandler> eventHandlers;

  public EventFilter(MetaMartApplicationConfig config) {
    this.forkJoinPool = new ForkJoinPool(FORK_JOIN_POOL_PARALLELISM);
    this.eventHandlers = new ArrayList<>();
    registerEventHandlers(config);
  }

  @SuppressWarnings("unchecked")
  private void registerEventHandlers(MetaMartApplicationConfig config) {
    if (!nullOrEmpty(config.getEventHandlerConfiguration())) {
      Set<String> eventHandlerClassNames =
          new HashSet<>(config.getEventHandlerConfiguration().getEventHandlerClassNames());
      for (String eventHandlerClassName : eventHandlerClassNames) {
        try {
          EventHandler eventHandler =
              ((Class<EventHandler>) Class.forName(eventHandlerClassName))
                  .getConstructor()
                  .newInstance();
          eventHandler.init(config);
          eventHandlers.add(eventHandler);
          LOG.info("Added event handler {}", eventHandlerClassName);
        } catch (Exception e) {
          LOG.info("Exception ", e);
        }
      }
    } else {
      LOG.info("Event handler configuration is empty");
    }
  }

  @Override
  public void filter(
      ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
    int responseCode = responseContext.getStatus();
    String method = requestContext.getMethod();
    if ((responseCode < 200 || responseCode > 299) || (!AUDITABLE_METHODS.contains(method))) {
      return;
    }

    eventHandlers.parallelStream()
        .forEach(
            eventHandler -> {
              UriInfo uriInfo = requestContext.getUriInfo();
              if (JwtFilter.EXCLUDED_ENDPOINTS.stream()
                  .noneMatch(endpoint -> uriInfo.getPath().contains(endpoint))) {
                ParallelStreamUtil.runAsync(
                    () -> eventHandler.process(requestContext, responseContext), forkJoinPool);
              }
            });
  }
}
