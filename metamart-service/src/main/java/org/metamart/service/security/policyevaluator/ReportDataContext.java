package org.metamart.service.security.policyevaluator;

import java.util.List;
import lombok.Builder;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.TagLabel;
import org.metamart.service.Entity;

@Builder
public class ReportDataContext implements ResourceContextInterface {
  @Override
  public String getResource() {
    return Entity.DATA_INSIGHT_CHART;
  }

  @Override
  public List<EntityReference> getOwners() {
    return null;
  }

  @Override
  public List<TagLabel> getTags() {
    return null;
  }

  @Override
  public EntityInterface getEntity() {
    return null;
  }

  @Override
  public EntityReference getDomain() {
    return null;
  }
}
