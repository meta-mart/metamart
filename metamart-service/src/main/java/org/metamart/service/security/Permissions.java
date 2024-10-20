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

package org.metamart.service.security;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.metamart.schema.type.MetadataOperation;

@ToString
public class Permissions {
  @Getter @Setter private Map<MetadataOperation, Boolean> metadataOperations;

  public Permissions() {
    // By default, set all permissions as false.
    metadataOperations =
        Stream.of(MetadataOperation.values()).collect(Collectors.toMap(o -> o, o -> Boolean.FALSE));
  }

  public Permissions(List<MetadataOperation> allowedOperations) {
    this();
    allowedOperations.forEach(operation -> metadataOperations.put(operation, Boolean.TRUE));
  }
}
