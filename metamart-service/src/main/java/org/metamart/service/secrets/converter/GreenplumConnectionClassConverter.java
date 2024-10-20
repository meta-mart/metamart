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
import org.metamart.schema.security.ssl.ValidateSSLClientConfig;
import org.metamart.schema.services.connections.database.GreenplumConnection;
import org.metamart.schema.services.connections.database.common.IamAuthConfig;
import org.metamart.schema.services.connections.database.common.basicAuth;
import org.metamart.service.util.JsonUtils;

/**
 * Converter class to get an `GreenplumConnection` object.
 */
public class GreenplumConnectionClassConverter extends ClassConverter {

  private static final List<Class<?>> SSL_SOURCE_CLASS = List.of(ValidateSSLClientConfig.class);
  private static final List<Class<?>> CONFIG_SOURCE_CLASSES =
      List.of(basicAuth.class, IamAuthConfig.class);

  public GreenplumConnectionClassConverter() {
    super(GreenplumConnection.class);
  }

  @Override
  public Object convert(Object object) {
    GreenplumConnection greenplumConnection =
        (GreenplumConnection) JsonUtils.convertValue(object, this.clazz);

    tryToConvert(greenplumConnection.getAuthType(), CONFIG_SOURCE_CLASSES)
        .ifPresent(greenplumConnection::setAuthType);

    tryToConvert(greenplumConnection.getSslConfig(), SSL_SOURCE_CLASS)
        .ifPresent(greenplumConnection::setSslConfig);

    return greenplumConnection;
  }
}
