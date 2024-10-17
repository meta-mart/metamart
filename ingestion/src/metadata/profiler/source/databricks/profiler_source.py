"""Extend the ProfilerSource class to add support for Databricks is_disconnect SQA method"""

from metadata.generated.schema.configuration.profilerConfiguration import (
    ProfilerConfiguration,
)
from metadata.generated.schema.entity.services.databaseService import DatabaseService
from metadata.generated.schema.metadataIngestion.workflow import (
    MetaMartWorkflowConfig,
)
from metadata.ingestion.ometa.ometa_api import MetaMart
from metadata.profiler.source.base.profiler_source import ProfilerSource


def is_disconnect(self, e, connection, cursor):
    """is_disconnect method for the Databricks dialect"""
    if "Invalid SessionHandle: SessionHandle" in str(e):
        return True
    return False


class DataBricksProfilerSource(ProfilerSource):
    """Databricks Profiler source"""

    def __init__(
        self,
        config: MetaMartWorkflowConfig,
        database: DatabaseService,
        ometa_client: MetaMart,
        global_profiler_config: ProfilerConfiguration,
    ):
        super().__init__(config, database, ometa_client, global_profiler_config)
        self.set_is_disconnect()

    def set_is_disconnect(self):
        """Set the is_disconnect method for the Databricks dialect"""
        from databricks.sqlalchemy import (
            DatabricksDialect,  # pylint: disable=import-outside-toplevel
        )

        DatabricksDialect.is_disconnect = is_disconnect
