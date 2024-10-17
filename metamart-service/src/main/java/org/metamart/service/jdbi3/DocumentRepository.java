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

import static org.metamart.service.Entity.DOCUMENT;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.sqlobject.transaction.Transaction;
import org.metamart.schema.email.EmailTemplate;
import org.metamart.schema.email.SmtpSettings;
import org.metamart.schema.email.TemplateValidationResponse;
import org.metamart.schema.entities.docStore.Data;
import org.metamart.schema.entities.docStore.Document;
import org.metamart.schema.settings.SettingsType;
import org.metamart.service.Entity;
import org.metamart.service.exception.EntityNotFoundException;
import org.metamart.service.resources.docstore.DocStoreResource;
import org.metamart.service.resources.settings.SettingsCache;
import org.metamart.service.util.EntityUtil.Fields;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.email.DefaultTemplateProvider;
import org.metamart.service.util.email.TemplateProvider;

@Slf4j
public class DocumentRepository extends EntityRepository<Document> {
  static final String DOCUMENT_UPDATE_FIELDS = "data";
  static final String DOCUMENT_PATCH_FIELDS = "data";
  private final CollectionDAO.DocStoreDAO dao;
  private final TemplateProvider templateProvider;
  private final String DIGITRANS = "digitrans";

  public DocumentRepository() {
    super(
        DocStoreResource.COLLECTION_PATH,
        DOCUMENT,
        Document.class,
        Entity.getCollectionDAO().docStoreDAO(),
        DOCUMENT_UPDATE_FIELDS,
        DOCUMENT_PATCH_FIELDS);
    supportsSearch = false;
    this.dao = Entity.getCollectionDAO().docStoreDAO();
    this.templateProvider = new DefaultTemplateProvider();
  }

  @Override
  public List<Document> getEntitiesFromSeedData() throws IOException {
    List<Document> entitiesFromSeedData = new ArrayList<>();
    SmtpSettings emailConfig =
        SettingsCache.getSetting(SettingsType.EMAIL_CONFIGURATION, SmtpSettings.class);

    if (emailConfig.getTemplates().value().equals(DIGITRANS)) {
      entitiesFromSeedData.addAll(
          getEntitiesFromSeedData(
              String.format(".*json/data/%s/emailTemplates/digitrans/.*\\.json$", entityType)));
    } else {
      entitiesFromSeedData.addAll(
          getEntitiesFromSeedData(
              String.format(".*json/data/%s/emailTemplates/metamart/.*\\.json$", entityType)));
    }

    entitiesFromSeedData.addAll(
        getEntitiesFromSeedData(String.format(".*json/data/%s/docs/.*\\.json$", entityType)));

    return entitiesFromSeedData;
  }

  public List<Document> fetchAllEmailTemplates() {
    List<String> jsons = dao.fetchAllEmailTemplates();
    return jsons.stream().map(json -> JsonUtils.readValue(json, Document.class)).toList();
  }

  public EmailTemplate fetchEmailTemplateByName(String name) {
    String json = dao.fetchEmailTemplateByName(name);
    if (json == null) {
      throw new EntityNotFoundException("Email template not found with name : " + name);
    }
    Document document = JsonUtils.readValue(json, Document.class);
    return JsonUtils.convertValue(document.getData(), EmailTemplate.class);
  }

  @Transaction
  public void deleteEmailTemplates() {
    dao.deleteEmailTemplates();
  }

  @Override
  public void setFullyQualifiedName(Document doc) {
    doc.setFullyQualifiedName(doc.getFullyQualifiedName());
  }

  @Override
  public void setFields(Document document, Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void clearFields(Document document, Fields fields) {
    /* Nothing to do */
  }

  @Override
  public void prepare(Document document, boolean update) {
    // validations are not implemented for Document
  }

  @Override
  public void storeEntity(Document document, boolean update) {
    store(document, update);
  }

  @Override
  public void storeRelationships(Document entity) {
    // validations are not implemented for Document
  }

  public TemplateValidationResponse validateEmailTemplate(String name, String content) {
    return templateProvider.validateEmailTemplate(name, content);
  }

  @Override
  public DocumentUpdater getUpdater(Document original, Document updated, Operation operation) {
    return new DocumentUpdater(original, updated, operation);
  }

  /** Handles entity updated from PUT and POST operation. */
  public class DocumentUpdater extends EntityUpdater {
    public DocumentUpdater(Document original, Document updated, Operation operation) {
      super(original, updated, operation);
    }

    @Transaction
    @Override
    public void entitySpecificUpdate() {
      updateEmailTemplatePlaceholders(original, updated);
      recordChange("data", original.getData(), updated.getData(), true);
    }
  }

  public void updateEmailTemplatePlaceholders(Document original, Document updated) {
    if (updated.getEntityType().equals(DefaultTemplateProvider.ENTITY_TYPE_EMAIL_TEMPLATE)) {
      EmailTemplate originalTemplate =
          JsonUtils.convertValue(original.getData(), EmailTemplate.class);
      EmailTemplate updatedTemplate =
          JsonUtils.convertValue(updated.getData(), EmailTemplate.class);
      updatedTemplate.setPlaceHolders(originalTemplate.getPlaceHolders());
      updated.setData(JsonUtils.convertValue(updatedTemplate, Data.class));
    }
  }
}
