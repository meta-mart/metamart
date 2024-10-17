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
MetaMart Airflow Lineage Backend
"""
import json
import os

from airflow.configuration import AirflowConfigParser
from pydantic import BaseModel

from airflow_provider_metamart.lineage.config.commons import LINEAGE
from metadata.generated.schema.entity.services.connections.metadata.metaMartConnection import (
    AuthProvider,
    MetaMartConnection,
)
from metadata.generated.schema.security.client.metaMartJWTClientConfig import (
    MetaMartJWTClientConfig,
)


class AirflowLineageConfig(BaseModel):
    airflow_service_name: str
    metadata_config: MetaMartConnection
    only_keep_dag_lineage: bool = False
    max_status: int = 10


def parse_airflow_config(
    airflow_service_name: str, conf: AirflowConfigParser
) -> AirflowLineageConfig:
    """
    Get airflow config from airflow.cfg and parse it
    to the config model
    """

    return AirflowLineageConfig(
        airflow_service_name=airflow_service_name,
        # Check if value is a literal string `true`
        only_keep_dag_lineage=conf.get(
            LINEAGE, "only_keep_dag_lineage", fallback="false"
        )
        == "true",
        max_status=int(conf.get(LINEAGE, "max_status", fallback=10)),
        metadata_config=MetaMartConnection(
            hostPort=conf.get(
                LINEAGE,
                "metamart_api_endpoint",
                fallback="http://localhost:8585/api",
            ),
            authProvider=AuthProvider.metamart.value,
            securityConfig=MetaMartJWTClientConfig(
                jwtToken=conf.get(
                    LINEAGE,
                    "jwt_token",
                    fallback=None,
                ),
            ),
            verifySSL=conf.get(LINEAGE, "verify_ssl", fallback="no-ssl"),
        ),
    )


def get_lineage_config() -> AirflowLineageConfig:
    """
    Load the lineage config from airflow.cfg, from
    a JSON file path configures as env in METAMART_LINEAGE_CONFIG
    or return a default config.
    """

    # Import conf settings at call time
    from airflow.configuration import conf  # pylint: disable=import-outside-toplevel

    airflow_service_name = conf.get(LINEAGE, "airflow_service_name", fallback=None)
    if airflow_service_name:
        return parse_airflow_config(airflow_service_name, conf=conf)

    metamart_config_file = os.getenv("METAMART_LINEAGE_CONFIG")

    # If config file, parse the JSON config, that should conform to AirflowLineageConfig
    if metamart_config_file:
        with open(metamart_config_file, encoding="utf-8") as config_file:
            config = json.load(config_file)
            return AirflowLineageConfig.model_validate(config)

    # If nothing is configured, raise
    raise ValueError("Missing lineage backend configuration")
