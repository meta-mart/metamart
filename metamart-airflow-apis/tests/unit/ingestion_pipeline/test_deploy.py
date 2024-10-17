#  Copyright 2021 DigiTrans
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""
Test Deploy
"""
import os
import uuid
from unittest.mock import patch

from metamart_managed_apis.operations.deploy import dump_with_safe_jwt

from metadata.generated.schema.entity.services.connections.metadata.metaMartConnection import (
    AuthProvider,
    MetaMartConnection,
)
from metadata.generated.schema.entity.services.ingestionPipelines.ingestionPipeline import (
    AirflowConfig,
    IngestionPipeline,
    PipelineType,
)
from metadata.generated.schema.metadataIngestion.workflow import SourceConfig
from metadata.generated.schema.security.client.metaMartJWTClientConfig import (
    MetaMartJWTClientConfig,
)
from metadata.generated.schema.security.secrets.secretsManagerClientLoader import (
    SecretsManagerClientLoader,
)
from metadata.generated.schema.security.secrets.secretsManagerProvider import (
    SecretsManagerProvider,
)
from metadata.generated.schema.type.basic import EntityName, Uuid
from metadata.utils.secrets.aws_secrets_manager import AWSSecretsManager
from metadata.utils.secrets.secrets_manager_factory import SecretsManagerFactory

SECRET_VALUE = "I am a secret value"
INGESTION_PIPELINE = IngestionPipeline(
    id=Uuid(str(uuid.uuid4())),
    name=EntityName("ingestion-pipeline"),
    pipelineType=PipelineType.metadata,
    sourceConfig=SourceConfig(),
    airflowConfig=AirflowConfig(),
    metaMartServerConnection=MetaMartConnection(
        hostPort="http://localhost:8585/api",
        authProvider=AuthProvider.metamart,
        securityConfig=MetaMartJWTClientConfig(jwtToken="secret:/super/secret"),
    ),
)


@patch.dict(os.environ, {"AWS_DEFAULT_REGION": "us-east-2"})
def test_deploy_ingestion_pipeline():
    """We can dump an ingestion pipeline to a file without exposing secrets"""
    # Instantiate the Secrets Manager
    SecretsManagerFactory.clear_all()
    with patch.object(AWSSecretsManager, "get_string_value", return_value=SECRET_VALUE):
        # Prep the singleton
        SecretsManagerFactory(
            SecretsManagerProvider.managed_aws,
            SecretsManagerClientLoader.noop,
        )
        # Now we'll try to dump the ingestion pipeline
        dumped = dump_with_safe_jwt(INGESTION_PIPELINE)

    assert SECRET_VALUE not in dumped
