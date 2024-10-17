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

package org.metamart.schema;

import java.util.List;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.LifeCycle;
import org.metamart.schema.type.TagLabel;

@SuppressWarnings("unchecked")
public interface CreateEntity {
  String getName();

  String getDisplayName();

  String getDescription();

  default List<EntityReference> getOwners() {
    return null;
  }

  default List<EntityReference> getReviewers() {
    return null;
  }

  default List<TagLabel> getTags() {
    return null;
  }

  default Object getExtension() {
    return null;
  }

  default String getDomain() {
    return null;
  }

  default List<String> getDataProducts() {
    return null;
  }

  default LifeCycle getLifeCycle() {
    return null;
  }

  <K extends CreateEntity> K withName(String name);

  <K extends CreateEntity> K withDisplayName(String displayName);

  <K extends CreateEntity> K withDescription(String description);

  default void setOwners(List<EntityReference> owners) {}

  default void setTags(List<TagLabel> tags) {
    /* no-op implementation to be overridden */
  }

  default <K extends CreateEntity> K withExtension(Object extension) {
    return (K) this;
  }

  default <K extends CreateEntity> K withDomain(String domain) {
    return (K) this;
  }

  default <K extends CreateEntity> K withLifeCycle(LifeCycle lifeCycle) {
    return (K) this;
  }
}
