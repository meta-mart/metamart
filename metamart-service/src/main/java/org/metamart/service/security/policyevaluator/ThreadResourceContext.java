package org.metamart.service.security.policyevaluator;

import java.util.ArrayList;
import java.util.List;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.TagLabel;
import org.metamart.service.Entity;

/** Conversation threads require special handling */
public record ThreadResourceContext(String createdBy) implements ResourceContextInterface {
  @Override
  public String getResource() {
    return Entity.THREAD;
  }

  @Override
  public List<EntityReference> getOwners() {
    List<EntityReference> owners = new ArrayList<>();
    owners.add(Entity.getEntityReferenceByName(Entity.USER, createdBy, Include.NON_DELETED));
    return owners;
  }

  @Override
  public List<TagLabel> getTags() {
    return null;
  }

  @Override
  public EntityInterface getEntity() {
    return null;
  }

  // TODO: Fix this this should be thread.getEntity().getDomain()
  @Override
  public EntityReference getDomain() {
    return null;
  }
}
