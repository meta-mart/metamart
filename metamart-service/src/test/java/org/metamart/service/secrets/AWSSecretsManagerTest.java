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
package org.metamart.service.secrets;

import static org.mockito.Mockito.reset;

import java.util.List;
import org.mockito.Mock;
import org.metamart.schema.security.secrets.SecretsManagerConfiguration;
import org.metamart.schema.security.secrets.SecretsManagerProvider;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

public class AWSSecretsManagerTest extends AWSBasedSecretsManagerTest {

  @Mock private SecretsManagerClient secretsManagerClient;

  @Override
  void setUpSpecific(SecretsManagerConfiguration config) {
    secretsManager =
        AWSSecretsManager.getInstance(
            new SecretsManager.SecretsConfig(
                "metamart", "prefix", List.of("key:value", "key2:value2"), null));
    ((AWSSecretsManager) secretsManager).setSecretsClient(secretsManagerClient);
    reset(secretsManagerClient);
  }

  @Override
  protected SecretsManagerProvider expectedSecretManagerProvider() {
    return SecretsManagerProvider.MANAGED_AWS;
  }
}
