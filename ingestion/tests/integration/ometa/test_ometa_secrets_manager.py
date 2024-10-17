#  Copyright 2022 DigiTrans
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
import os
from unittest import TestCase, mock

from metadata.generated.schema.entity.services.connections.metadata.metaMartConnection import (
    MetaMartConnection,
)
from metadata.generated.schema.security.secrets.secretsManagerClientLoader import (
    SecretsManagerClientLoader,
)
from metadata.generated.schema.security.secrets.secretsManagerProvider import (
    SecretsManagerProvider,
)
from metadata.ingestion.ometa.auth_provider import MetaMartAuthenticationProvider
from metadata.ingestion.ometa.ometa_api import MetaMart
from metadata.utils.secrets.aws_secrets_manager import AWSSecretsManager
from metadata.utils.secrets.db_secrets_manager import DBSecretsManager
from metadata.utils.singleton import Singleton


class OMetaSecretManagerTest(TestCase):
    metadata: MetaMart
    aws_server_config: MetaMartConnection
    local_server_config: MetaMartConnection

    @classmethod
    def setUp(cls) -> None:
        Singleton.clear_all()
        cls.local_server_config = MetaMartConnection(
            hostPort="http://localhost:8585/api",
            enableVersionValidation=False,
        )
        cls.aws_server_config = MetaMartConnection(
            hostPort="http://localhost:8585/api",
            secretsManagerProvider=SecretsManagerProvider.aws,
            secretsManagerLoader=SecretsManagerClientLoader.noop,
            enableVersionValidation=False,
        )

    def test_ometa_with_local_secret_manager(self):
        self._init_local_secret_manager()
        assert type(self.metadata.secrets_manager_client) is DBSecretsManager
        assert type(self.metadata._auth_provider) is MetaMartAuthenticationProvider

    @mock.patch.dict(os.environ, {"AWS_DEFAULT_REGION": "us-east-2"}, clear=True)
    def test_ometa_with_aws_secret_manager(self):
        self._init_aws_secret_manager()
        assert type(self.metadata.secrets_manager_client) is AWSSecretsManager
        assert type(self.metadata._auth_provider) is MetaMartAuthenticationProvider

    def _init_local_secret_manager(self):
        self.metadata = MetaMart(self.local_server_config)

    def _init_aws_secret_manager(self):
        self.metadata = MetaMart(self.aws_server_config)
