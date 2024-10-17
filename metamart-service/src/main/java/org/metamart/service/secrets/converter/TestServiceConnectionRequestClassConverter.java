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

package org.metamart.service.secrets.converter;

import java.util.List;
import org.metamart.schema.ServiceConnectionEntityInterface;
import org.metamart.schema.api.services.DatabaseConnection;
import org.metamart.schema.entity.automations.TestServiceConnectionRequest;
import org.metamart.schema.entity.services.MetadataConnection;
import org.metamart.schema.type.DashboardConnection;
import org.metamart.schema.type.MessagingConnection;
import org.metamart.schema.type.MlModelConnection;
import org.metamart.schema.type.PipelineConnection;
import org.metamart.schema.type.StorageConnection;
import org.metamart.service.exception.InvalidServiceConnectionException;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.ReflectionUtil;

/** Converter class to get an `TestServiceConnectionRequest` object. */
public class TestServiceConnectionRequestClassConverter extends ClassConverter {

  private static final List<Class<?>> CONNECTION_CLASSES =
      List.of(
          DatabaseConnection.class,
          DashboardConnection.class,
          MessagingConnection.class,
          PipelineConnection.class,
          MlModelConnection.class,
          MetadataConnection.class,
          StorageConnection.class);

  public TestServiceConnectionRequestClassConverter() {
    super(TestServiceConnectionRequest.class);
  }

  @Override
  public Object convert(Object object) {
    TestServiceConnectionRequest testServiceConnectionRequest =
        (TestServiceConnectionRequest) JsonUtils.convertValue(object, this.clazz);

    try {
      Class<?> clazz =
          ReflectionUtil.createConnectionConfigClass(
              testServiceConnectionRequest.getConnectionType(),
              testServiceConnectionRequest.getServiceType());

      tryToConvertOrFail(testServiceConnectionRequest.getConnection(), CONNECTION_CLASSES)
          .ifPresent(testServiceConnectionRequest::setConnection);

      Object newConnectionConfig =
          ClassConverterFactory.getConverter(clazz)
              .convert(
                  ((ServiceConnectionEntityInterface) testServiceConnectionRequest.getConnection())
                      .getConfig());
      ((ServiceConnectionEntityInterface) testServiceConnectionRequest.getConnection())
          .setConfig(newConnectionConfig);
    } catch (Exception e) {
      throw InvalidServiceConnectionException.byMessage(
          testServiceConnectionRequest.getConnectionType(),
          String.format(
              "Failed to convert class instance of %s",
              testServiceConnectionRequest.getConnectionType()));
    }

    return testServiceConnectionRequest;
  }
}
