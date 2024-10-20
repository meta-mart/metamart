#  Copyright 2024 DigiTrans
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#  http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
"""Module that defines the RuntimeParameterSetter class."""
from abc import ABC, abstractmethod

from pydantic import BaseModel

from metadata.generated.schema.entity.data.table import Table
from metadata.ingestion.ometa.ometa_api import MetaMart
from metadata.profiler.processor.sampler.sqlalchemy.sampler import SQASampler


class RuntimeParameterSetter(ABC):
    """
    Set runtime parameters for a test case. This can be useful for resolving parameters based on the
    state of MetaMart entities.
    You can see an example implemnentation in `TableDiffParamsSetter`.
    """

    def __init__(
        self,
        ometa_client: MetaMart,
        service_connection_config,
        table_entity: Table,
        sampler: SQASampler,
    ):
        self.ometa_client = ometa_client
        self.service_connection_config = service_connection_config
        self.table_entity = table_entity
        self.sampler = sampler

    @abstractmethod
    def get_parameters(self, test_case) -> BaseModel:
        raise NotImplementedError
