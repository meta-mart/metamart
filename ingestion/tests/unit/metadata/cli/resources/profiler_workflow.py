"""
This file has been generated from dag_runner.j2
"""
from metamart_managed_apis.workflows import workflow_factory

workflow = workflow_factory.WorkflowFactory.create(
    "/airflow/dag_generated_configs/local_redshift_profiler_e9AziRXs.json"
)
workflow.generate_dag(globals())
