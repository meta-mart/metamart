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
Module containing the logic to trigger a DAG
"""
from typing import Optional

try:
    from airflow.api.common.trigger_dag import trigger_dag
except ImportError:
    from airflow.api.common.experimental.trigger_dag import trigger_dag
from airflow.utils import timezone
from flask import Response
from metamart_managed_apis.api.response import ApiResponse


def trigger(dag_id: str, run_id: Optional[str]) -> Response:
    dag_run = trigger_dag(
        dag_id=dag_id,
        run_id=run_id,
        conf=None,
        execution_date=timezone.utcnow(),
    )
    return ApiResponse.success(
        {"message": f"Workflow [{dag_id}] has been triggered {dag_run}"}
    )
