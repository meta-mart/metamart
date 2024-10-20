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
Common Query Log Connector
"""
from typing import Optional

from metadata.generated.schema.metadataIngestion.workflow import (
    Source as WorkflowSource,
)
from metadata.ingestion.ometa.ometa_api import MetaMart
from metadata.ingestion.source.database.lineage_source import LineageSource


class QueryLogLineageSource(LineageSource):
    @classmethod
    def create(
        cls, config_dict, metadata: MetaMart, pipeline_name: Optional[str] = None
    ):
        config: WorkflowSource = WorkflowSource.model_validate(config_dict)
        return cls(config, metadata)

    def prepare(self):
        """
        Nothing to prepare for Query Log Lineage
        """
