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

package org.metamart.service.apps.bundles.changeEvent;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.metamart.schema.type.ChangeEvent;
import org.metamart.service.events.errors.EventPublisherException;
import org.quartz.JobExecutionContext;

public interface Consumer<T> {
  List<T> pollEvents(long offset, long batchSize);

  void publishEvents(Map<ChangeEvent, Set<UUID>> events);

  void handleFailedEvent(EventPublisherException e);

  void commit(JobExecutionContext jobExecutionContext);
}
