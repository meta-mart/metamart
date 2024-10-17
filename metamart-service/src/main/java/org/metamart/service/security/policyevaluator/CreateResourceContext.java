package org.metamart.service.security.policyevaluator;

import java.util.Collections;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NonNull;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.TagLabel;
import org.metamart.service.Entity;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.jdbi3.EntityRepository;
import org.metamart.service.util.EntityUtil;

/**
 * ResourceContext used for CREATE operations where ownership, tags are inherited from the parent term.
 *
 * <p>As multiple threads don't access this, the class is not thread-safe by design.
 */
public class CreateResourceContext<T extends EntityInterface> implements ResourceContextInterface {
  @NonNull @Getter private final String resource;
  private final EntityRepository<T> entityRepository;
  private final T entity; // Entity being created
  private EntityInterface parentEntity; // Entity being created

  public CreateResourceContext(@NonNull String resource, @NotNull T entity) {
    this.resource = resource;
    this.entityRepository = (EntityRepository<T>) Entity.getEntityRepository(resource);
    this.entity = entity;
    setParent(entity);
  }

  @Override
  public List<EntityReference> getOwners() {
    return parentEntity == null ? null : parentEntity.getOwners();
  }

  @Override
  public List<TagLabel> getTags() {
    return parentEntity == null
        ? Collections.emptyList()
        : Entity.getEntityTags(getResource(), parentEntity);
  }

  @Override
  public EntityInterface getEntity() {
    return entity;
  }

  @Override
  public EntityReference getDomain() {
    return parentEntity == null ? null : parentEntity.getDomain();
  }

  private void setParent(T entity) {
    String fields = "";
    if (entityRepository.isSupportsOwners()) {
      fields = EntityUtil.addField(fields, Entity.FIELD_OWNERS);
    }
    if (entityRepository.isSupportsTags()) {
      fields = EntityUtil.addField(fields, Entity.FIELD_TAGS);
    }
    if (entityRepository.isSupportsDomain()) {
      fields = EntityUtil.addField(fields, Entity.FIELD_DOMAIN);
    }
    if (entityRepository.isSupportsReviewers()) {
      fields = EntityUtil.addField(fields, Entity.FIELD_REVIEWERS);
    }
    if (entityRepository.isSupportsDomain()) {
      fields = EntityUtil.addField(fields, Entity.FIELD_DOMAIN);
    }
    try {
      parentEntity = entityRepository.getParentEntity(entity, fields);
    } catch (EntityNotFoundException e) {
      parentEntity = null;
    }
  }
}
