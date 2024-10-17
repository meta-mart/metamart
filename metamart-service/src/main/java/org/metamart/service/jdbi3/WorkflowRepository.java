package org.metamart.service.jdbi3;

import static org.metamart.service.Entity.WORKFLOW;

import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.metamart.schema.entity.automations.Workflow;
import org.metamart.schema.services.connections.metadata.MetaMartConnection;
import org.metamart.service.Entity;
import org.metamart.service.resources.automations.WorkflowResource;
import org.metamart.service.secrets.SecretsManager;
import org.metamart.service.secrets.SecretsManagerFactory;
import org.metamart.service.util.EntityUtil;

public class WorkflowRepository extends EntityRepository<Workflow> {
  private static final String PATCH_FIELDS = "status,response";

  public WorkflowRepository() {
    super(
        WorkflowResource.COLLECTION_PATH,
        WORKFLOW,
        Workflow.class,
        Entity.getCollectionDAO().workflowDAO(),
        PATCH_FIELDS,
        "");
    quoteFqn = true;
  }

  @Override
  public void setFields(Workflow entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void clearFields(Workflow entity, EntityUtil.Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void prepare(Workflow entity, boolean update) {
    // validate request and status
    if (entity.getRequest() == null) {
      throw new IllegalArgumentException("Request must not be empty");
    }
  }

  @Override
  public void storeEntity(Workflow entity, boolean update) {
    MetaMartConnection metamartConnection = entity.getMetaMartServerConnection();
    SecretsManager secretsManager = SecretsManagerFactory.getSecretsManager();

    if (secretsManager != null) {
      entity = secretsManager.encryptWorkflow(entity);
    }

    // Don't store owners, database, href and tags as JSON. Build it on the fly based on
    // relationships
    entity.withMetaMartServerConnection(null);
    store(entity, update);

    // Restore the relationships
    entity.withMetaMartServerConnection(metamartConnection);
  }

  /** Remove the secrets from the secret manager */
  @Override
  protected void postDelete(Workflow workflow) {
    SecretsManagerFactory.getSecretsManager().deleteSecretsFromWorkflow(workflow);
  }

  @Override
  public void storeRelationships(Workflow entity) {
    // No relationships to store beyond what is stored in the super class
  }

  @Override
  public EntityUpdater getUpdater(Workflow original, Workflow updated, Operation operation) {
    return new WorkflowUpdater(original, updated, operation);
  }

  public class WorkflowUpdater extends EntityUpdater {
    public WorkflowUpdater(Workflow original, Workflow updated, Operation operation) {
      super(original, updated, operation);
    }

    @Transaction
    @Override
    public void entitySpecificUpdate() {
      recordChange("status", original.getStatus(), updated.getStatus());
      recordChange("response", original.getResponse(), updated.getResponse(), true);
    }
  }
}
