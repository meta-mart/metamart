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

package org.metamart.client.security.factory;

import java.util.Objects;
import org.metamart.client.security.MetaMartAuthenticationProvider;
import org.metamart.client.security.interfaces.AuthenticationProvider;
import org.metamart.schema.services.connections.metadata.AuthProvider;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;

public class AuthenticationProviderFactory {
  public AuthenticationProvider getAuthProvider(MetaMartConnection serverConfig) {
    if (Objects.requireNonNull(serverConfig.getAuthProvider()) == AuthProvider.METAMART) {
      return new MetaMartAuthenticationProvider(serverConfig);
    }
    return null;
  }
}
