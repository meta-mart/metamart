package org.metamart.service.search.indexes;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;
import static org.metamart.service.Entity.FIELD_DESCRIPTION;
import static org.metamart.service.Entity.FIELD_DISPLAY_NAME;
import static org.metamart.service.jdbi3.LineageRepository.buildRelationshipDetailsMap;
import static org.metamart.service.search.EntityBuilderConstant.DISPLAY_NAME_KEYWORD;
import static org.metamart.service.search.EntityBuilderConstant.FIELD_DISPLAY_NAME_NGRAM;
import static org.metamart.service.search.EntityBuilderConstant.FULLY_QUALIFIED_NAME;
import static org.metamart.service.search.EntityBuilderConstant.FULLY_QUALIFIED_NAME_PARTS;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.metamart.schema.EntityInterface;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.LineageDetails;
import org.metamart.schema.type.Relationship;
import org.metamart.service.Entity;
import org.metamart.service.jdbi3.CollectionDAO;
import org.metamart.service.search.SearchIndexUtils;
import org.metamart.service.search.models.SearchSuggest;
import org.metamart.service.util.FullyQualifiedName;
import org.metamart.service.util.JsonUtils;

public interface SearchIndex {
  Set<String> DEFAULT_EXCLUDED_FIELDS =
      Set.of("changeDescription", "lineage.pipeline.changeDescription", "connection");

  default Map<String, Object> buildSearchIndexDoc() {
    // Build Index Doc
    Map<String, Object> esDoc = this.buildSearchIndexDocInternal(JsonUtils.getMap(getEntity()));

    // Non Indexable Fields
    removeNonIndexableFields(esDoc);

    return esDoc;
  }

  default void removeNonIndexableFields(Map<String, Object> esDoc) {
    // Remove non indexable fields
    SearchIndexUtils.removeNonIndexableFields(esDoc, DEFAULT_EXCLUDED_FIELDS);

    // Remove Entity Specific Field
    SearchIndexUtils.removeNonIndexableFields(esDoc, getExcludedFields());
  }

  Object getEntity();

  default Set<String> getExcludedFields() {
    return Collections.emptySet();
  }

  Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> esDoc);

  default List<SearchSuggest> getSuggest() {
    return null;
  }

  default Map<String, Object> getCommonAttributesMap(EntityInterface entity, String entityType) {
    Map<String, Object> map = new HashMap<>();
    List<SearchSuggest> suggest = getSuggest();
    map.put(
        "displayName",
        entity.getDisplayName() != null ? entity.getDisplayName() : entity.getName());
    map.put("entityType", entityType);
    map.put("owners", getEntitiesWithDisplayName(entity.getOwners()));
    map.put("domain", getEntityWithDisplayName(entity.getDomain()));
    map.put("followers", SearchIndexUtils.parseFollowers(entity.getFollowers()));
    map.put(
        "totalVotes",
        nullOrEmpty(entity.getVotes())
            ? 0
            : entity.getVotes().getUpVotes() - entity.getVotes().getDownVotes());
    map.put("descriptionStatus", getDescriptionStatus(entity));
    map.put("suggest", suggest);
    map.put(
        "fqnParts",
        getFQNParts(
            entity.getFullyQualifiedName(),
            suggest.stream().map(SearchSuggest::getInput).toList()));
    map.put("deleted", entity.getDeleted() != null && entity.getDeleted());
    return map;
  }

  default Set<String> getFQNParts(String fqn, List<String> fqnSplits) {
    Set<String> fqnParts = new HashSet<>();
    fqnParts.add(fqn);
    String parent = FullyQualifiedName.getParentFQN(fqn);
    while (parent != null) {
      fqnParts.add(parent);
      parent = FullyQualifiedName.getParentFQN(parent);
    }
    fqnParts.addAll(fqnSplits);
    return fqnParts;
  }

  default List<EntityReference> getEntitiesWithDisplayName(List<EntityReference> entities) {
    if (nullOrEmpty(entities)) {
      return Collections.emptyList();
    }
    List<EntityReference> clone = new ArrayList<>();
    for (EntityReference entity : entities) {
      EntityReference cloneEntity = JsonUtils.deepCopy(entity, EntityReference.class);
      cloneEntity.setDisplayName(
          nullOrEmpty(cloneEntity.getDisplayName())
              ? cloneEntity.getName()
              : cloneEntity.getDisplayName());
      cloneEntity.setFullyQualifiedName(cloneEntity.getFullyQualifiedName().replace("\"", "\\'"));
      clone.add(cloneEntity);
    }
    return clone;
  }

  default EntityReference getEntityWithDisplayName(EntityReference entity) {
    if (entity == null) {
      return null;
    }
    EntityReference cloneEntity = JsonUtils.deepCopy(entity, EntityReference.class);
    cloneEntity.setDisplayName(
        nullOrEmpty(cloneEntity.getDisplayName())
            ? cloneEntity.getName()
            : cloneEntity.getDisplayName());
    return cloneEntity;
  }

  default String getDescriptionStatus(EntityInterface entity) {
    return nullOrEmpty(entity.getDescription()) ? "INCOMPLETE" : "COMPLETE";
  }

  static List<Map<String, Object>> getLineageData(EntityReference entity) {
    List<Map<String, Object>> data = new ArrayList<>();
    CollectionDAO dao = Entity.getCollectionDAO();
    List<CollectionDAO.EntityRelationshipRecord> toRelationshipsRecords =
        dao.relationshipDAO()
            .findTo(entity.getId(), entity.getType(), Relationship.UPSTREAM.ordinal());
    for (CollectionDAO.EntityRelationshipRecord entityRelationshipRecord : toRelationshipsRecords) {
      EntityReference ref =
          Entity.getEntityReferenceById(
              entityRelationshipRecord.getType(), entityRelationshipRecord.getId(), Include.ALL);
      LineageDetails lineageDetails =
          JsonUtils.readValue(entityRelationshipRecord.getJson(), LineageDetails.class);
      data.add(buildRelationshipDetailsMap(entity, ref, lineageDetails));
    }
    List<CollectionDAO.EntityRelationshipRecord> fromRelationshipsRecords =
        dao.relationshipDAO()
            .findFrom(entity.getId(), entity.getType(), Relationship.UPSTREAM.ordinal());
    for (CollectionDAO.EntityRelationshipRecord entityRelationshipRecord :
        fromRelationshipsRecords) {
      EntityReference ref =
          Entity.getEntityReferenceById(
              entityRelationshipRecord.getType(), entityRelationshipRecord.getId(), Include.ALL);
      LineageDetails lineageDetails =
          JsonUtils.readValue(entityRelationshipRecord.getJson(), LineageDetails.class);
      data.add(buildRelationshipDetailsMap(ref, entity, lineageDetails));
    }
    return data;
  }

  static Map<String, Float> getDefaultFields() {
    Map<String, Float> fields = new HashMap<>();
    fields.put(DISPLAY_NAME_KEYWORD, 10.0f);
    fields.put(FIELD_DISPLAY_NAME_NGRAM, 1.0f);
    fields.put(FIELD_DISPLAY_NAME, 10.0f);
    fields.put(FIELD_DESCRIPTION, 2.0f);
    fields.put(FULLY_QUALIFIED_NAME, 5.0f);
    fields.put(FULLY_QUALIFIED_NAME_PARTS, 5.0f);
    return fields;
  }

  static Map<String, Float> getAllFields() {
    Map<String, Float> fields = getDefaultFields();
    fields.putAll(TableIndex.getFields());
    fields.putAll(StoredProcedureIndex.getFields());
    fields.putAll(DashboardIndex.getFields());
    fields.putAll(DashboardDataModelIndex.getFields());
    fields.putAll(PipelineIndex.getFields());
    fields.putAll(TopicIndex.getFields());
    fields.putAll(MlModelIndex.getFields());
    fields.putAll(ContainerIndex.getFields());
    fields.putAll(SearchEntityIndex.getFields());
    fields.putAll(GlossaryTermIndex.getFields());
    fields.putAll(TagIndex.getFields());
    fields.putAll(DataProductIndex.getFields());
    fields.putAll(APIEndpointIndex.getFields());
    return fields;
  }
}
