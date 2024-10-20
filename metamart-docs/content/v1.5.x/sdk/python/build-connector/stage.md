---
title: Stage
slug: /sdk/python/build-connector/stage
---

# Stage
The **Stage** is an optional component in the workflow. It can be used to store the records in a file or data store and can be used to aggregate the work done by a processor.

## API
```python
class Stage(StageStep, ABC):
    """All Stages must inherit this base class."""

    # From the parent - just to showcase
    @abstractmethod
    def _run(self, record: Entity) -> Iterable[Either[str]]:
        """
        Main entrypoint to execute the step.

        Note that the goal of this step is to store the
        processed data somewhere (e.g., a file). We will
        return an iterable to keep track of the processed
        entities / exceptions, but the next step (Bulk Sink)
        won't read these results. It will directly
        pick up the file components.
        """
```

**_run** this method is called for each record coming down in the workflow chain and can be used to store the record. This method doesn't emit anything for the downstream to process on.

## Example
[Example implementation](https://github.com/meta-mart/MetaMart/blob/main/ingestion/src/metadata/ingestion/stage/table_usage.py#L42)
