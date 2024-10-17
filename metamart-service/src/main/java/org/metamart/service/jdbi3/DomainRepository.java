/*
 *  Copyright 2021 DigiTrans
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.metamart.service.jdbi3;

import static org.metamart.common.utils.CommonUtil.listOrEmpty;
import static org.metamart.schema.type.Include.ALL;
import static org.metamart.service.Entity.DOMAIN;
import static org.metamart.service.Entity.FIELD_ASSETS;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.entity.domains.Domain;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.Relationship;
import org.metamart.schema.type.api.BulkAssets;
import org.metamart.schema.type.api.BulkOperationResult;
import org.metamart.service.Entity;
import org.metamart.service.resources.domains.DomainResource;
import org.metamart.service.util.EntityUtil.Fields;
import org.metamart.service.util.FullyQualifiedName;

@Slf4j
public class DomainRepository extends EntityRepository<Domain> {
  private static final String UPDATE_FIELDS = "parent,children,experts";

  public DomainRepository() {
    super(
        DomainResource.COLLECTION_PATH,
        DOMAIN,
        Domain.class,
        Entity.getCollectionDAO().domainDAO(),
        UPDATE_FIELDS,
        UPDATE_FIELDS);
    supportsSearch = true;
  }

  @Override
  public void setFields(Domain entity, Fields fields) {
    entity.withAssets(fields.contains(FIELD_ASSETS) ? getAssets(entity) : null);
    entity.withParent(getParent(entity));
  }

  @Override
  public void clearFields(Domain entity, Fields fields) {
    entity.withParent(fields.contains("parent") ? entity.getParent() : null);
  }

  @Override
  public void prepare(Domain entity, boolean update) {
    // Parent, Experts, Owner are already validated
  }

  @Override
  public void storeEntity(Domain entity, boolean update) {
    EntityReference parent = entity.getParent();
    entity.withParent(null);
    store(entity, update);
    entity.withParent(parent);
  }

  @Override
  public void storeRelationships(Domain entity) {
    if (entity.getParent() != null) {
      addRelationship(
          entity.getParent().getId(), entity.getId(), DOMAIN, DOMAIN, Relationship.CONTAINS);
    }
    for (EntityReference expert : listOrEmpty(entity.getExperts())) {
      addRelationship(entity.getId(), expert.getId(), DOMAIN, Entity.USER, Relationship.EXPERT);
    }
  }

  @Override
  public void setInheritedFields(Domain domain, Fields fields) {
    // If subdomain does not have owners and experts, then inherit it from parent domain
    EntityReference parentRef = domain.getParent() != null ? domain.getParent() : getParent(domain);
    if (parentRef != null) {
      Domain parent = Entity.getEntity(DOMAIN, parentRef.getId(), "owners,experts", ALL);
      inheritOwners(domain, fields, parent);
      inheritExperts(domain, fields, parent);
    }
  }

  private List<EntityReference> getAssets(Domain entity) {
    return findTo(entity.getId(), DOMAIN, Relationship.HAS, null);
  }

  public BulkOperationResult bulkAddAssets(String domainName, BulkAssets request) {
    Domain domain = getByName(null, domainName, getFields("id"));
    return bulkAssetsOperation(domain.getId(), DOMAIN, Relationship.HAS, request, true);
  }

  public BulkOperationResult bulkRemoveAssets(String domainName, BulkAssets request) {
    Domain domain = getByName(null, domainName, getFields("id"));
    return bulkAssetsOperation(domain.getId(), DOMAIN, Relationship.HAS, request, false);
  }

  @Override
  public EntityUpdater getUpdater(Domain original, Domain updated, Operation operation) {
    return new DomainUpdater(original, updated, operation);
  }

  @Override
  public void restorePatchAttributes(Domain original, Domain updated) {
    super.restorePatchAttributes(original, updated);
    updated.withParent(original.getParent()); // Parent can't be changed
    updated.withChildren(original.getChildren()); // Children can't be changed
  }

  @Override
  public void setFullyQualifiedName(Domain entity) {
    // Validate parent
    if (entity.getParent() == null) { // Top level domain
      entity.setFullyQualifiedName(FullyQualifiedName.build(entity.getName()));
    } else { // Sub domain
      EntityReference parent = entity.getParent();
      entity.setFullyQualifiedName(
          FullyQualifiedName.add(parent.getFullyQualifiedName(), entity.getName()));
    }
  }

  @Override
  public EntityInterface getParentEntity(Domain entity, String fields) {
    return entity.getParent() != null
        ? Entity.getEntity(entity.getParent(), fields, Include.NON_DELETED)
        : null;
  }

  public class DomainUpdater extends EntityUpdater {
    public DomainUpdater(Domain original, Domain updated, Operation operation) {
      super(original, updated, operation);
    }

    @Transaction
    @Override
    public void entitySpecificUpdate() {
      recordChange("domainType", original.getDomainType(), updated.getDomainType());
    }
  }
}
