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
Airflow backend lineage module
"""


def get_provider_config():
    """
    Get provider configuration

    Returns
        dict:
    """
    return {
        "name": "MetaMart",
        "description": "`MetaMart <https://meta-mart.org/>`__",
        "package-name": "metamart-ingestion",
        "version": "0.4.1",
        "connection-types": [
            {
                "connection-type": "metamart",
                "hook-class-name": "airflow_provider_metamart.hooks.metamart.MetaMartHook",
            }
        ],
        "hook-class-names": [
            "airflow_provider_metamart.hooks.metamart.MetaMartHook",
        ],
    }
