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

package org.metamart.service.resources.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.metamart.service.util.TestUtils.ADMIN_AUTH_HEADERS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.HttpResponseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.metamart.schema.CreateEntity;
import org.metamart.schema.EntityInterface;
import org.metamart.service.resources.EntityResourceTest;
import org.metamart.service.resources.domains.DomainResourceTest;
import org.metamart.service.util.ResultList;

@Slf4j
public abstract class ServiceResourceTest<T extends EntityInterface, K extends CreateEntity>
    extends EntityResourceTest<T, K> {
  public ServiceResourceTest(
      String entityType,
      Class<T> entityClass,
      Class<? extends ResultList<T>> entityListClass,
      String collectionName,
      String fields) {
    super(entityType, entityClass, entityListClass, collectionName, fields);
    this.supportsPatch = false;
  }

  @Test
  void test_listWithDomainFilter(TestInfo test) throws HttpResponseException {
    DomainResourceTest domainTest = new DomainResourceTest();
    String domain1 =
        domainTest
            .createEntity(domainTest.createRequest(test, 1), ADMIN_AUTH_HEADERS)
            .getFullyQualifiedName();
    String domain2 =
        domainTest
            .createEntity(domainTest.createRequest(test, 2), ADMIN_AUTH_HEADERS)
            .getFullyQualifiedName();
    T s1 = createEntity(createRequest(test, 1).withDomain(domain1), ADMIN_AUTH_HEADERS);
    T s2 = createEntity(createRequest(test, 2).withDomain(domain1), ADMIN_AUTH_HEADERS);
    T s3 = createEntity(createRequest(test, 3).withDomain(domain2), ADMIN_AUTH_HEADERS);
    T s4 = createEntity(createRequest(test, 4).withDomain(domain2), ADMIN_AUTH_HEADERS);

    Map<String, String> params = new HashMap<>();
    params.put("domain", domain1);
    List<T> list = listEntities(params, ADMIN_AUTH_HEADERS).getData();
    assertEquals(2, list.size());
    assertTrue(list.stream().anyMatch(s -> s.getName().equals(s1.getName())));
    assertTrue(list.stream().anyMatch(s -> s.getName().equals(s2.getName())));

    params.put("domain", domain2);
    list = listEntities(params, ADMIN_AUTH_HEADERS).getData();
    assertEquals(2, list.size());
    assertTrue(list.stream().anyMatch(s -> s.getName().equals(s3.getName())));
    assertTrue(list.stream().anyMatch(s -> s.getName().equals(s4.getName())));
  }
}
