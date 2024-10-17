package org.metamart.service.search.indexes;

import static org.metamart.service.search.EntityBuilderConstant.ES_MESSAGE_SCHEMA_FIELD;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import org.metamart.schema.entity.data.APIEndpoint;
import org.metamart.schema.type.Field;
import org.metamart.schema.type.TagLabel;
import org.metamart.service.Entity;
import org.metamart.service.search.ParseTags;
import org.metamart.service.search.models.FlattenSchemaField;
import org.metamart.service.search.models.SearchSuggest;
import org.metamart.service.util.FullyQualifiedName;

public class APIEndpointIndex implements SearchIndex {
  final Set<String> excludeAPIEndpointFields = Set.of("sampleData");
  final APIEndpoint apiEndpoint;

  public APIEndpointIndex(APIEndpoint apiEndpoint) {
    this.apiEndpoint = apiEndpoint;
  }

  @Override
  public List<SearchSuggest> getSuggest() {
    List<SearchSuggest> suggest = new ArrayList<>();
    suggest.add(
        SearchSuggest.builder().input(apiEndpoint.getFullyQualifiedName()).weight(5).build());
    suggest.add(SearchSuggest.builder().input(apiEndpoint.getName()).weight(10).build());
    return suggest;
  }

  @Override
  public Object getEntity() {
    return apiEndpoint;
  }

  @Override
  public Set<String> getExcludedFields() {
    return excludeAPIEndpointFields;
  }

  public Map<String, Object> buildSearchIndexDocInternal(Map<String, Object> doc) {
    List<SearchSuggest> fieldSuggest = new ArrayList<>();
    List<SearchSuggest> serviceSuggest = new ArrayList<>();
    Set<List<TagLabel>> tagsWithChildren = new HashSet<>();
    List<String> fieldsWithChildrenName = new ArrayList<>();
    serviceSuggest.add(
        SearchSuggest.builder().input(apiEndpoint.getService().getName()).weight(5).build());
    serviceSuggest.add(
        SearchSuggest.builder().input(apiEndpoint.getApiCollection().getName()).weight(5).build());

    if (apiEndpoint.getResponseSchema() != null
        && apiEndpoint.getResponseSchema().getSchemaFields() != null
        && !apiEndpoint.getResponseSchema().getSchemaFields().isEmpty()) {
      List<FlattenSchemaField> flattenFields = new ArrayList<>();
      parseSchemaFields(apiEndpoint.getResponseSchema().getSchemaFields(), flattenFields, null);

      for (FlattenSchemaField field : flattenFields) {
        fieldSuggest.add(SearchSuggest.builder().input(field.getName()).weight(5).build());
        fieldsWithChildrenName.add(field.getName());
        if (field.getTags() != null) {
          tagsWithChildren.add(field.getTags());
        }
      }
      doc.put("response_field_names", fieldsWithChildrenName);
    }

    if (apiEndpoint.getRequestSchema() != null
        && apiEndpoint.getRequestSchema().getSchemaFields() != null
        && !apiEndpoint.getRequestSchema().getSchemaFields().isEmpty()) {
      List<FlattenSchemaField> flattenFields = new ArrayList<>();
      parseSchemaFields(apiEndpoint.getResponseSchema().getSchemaFields(), flattenFields, null);

      for (FlattenSchemaField field : flattenFields) {
        fieldSuggest.add(SearchSuggest.builder().input(field.getName()).weight(5).build());
        fieldsWithChildrenName.add(field.getName());
        if (field.getTags() != null) {
          tagsWithChildren.add(field.getTags());
        }
      }
      doc.put("request_field_names", fieldsWithChildrenName);
    }

    ParseTags parseTags = new ParseTags(Entity.getEntityTags(Entity.API_ENDPOINT, apiEndpoint));
    tagsWithChildren.add(parseTags.getTags());
    List<TagLabel> flattenedTagList =
        tagsWithChildren.stream()
            .flatMap(List::stream)
            .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    Map<String, Object> commonAttributes = getCommonAttributesMap(apiEndpoint, Entity.API_ENDPOINT);
    doc.putAll(commonAttributes);
    doc.put("lineage", SearchIndex.getLineageData(apiEndpoint.getEntityReference()));
    doc.put(
        "requestSchema",
        apiEndpoint.getRequestSchema() != null ? apiEndpoint.getRequestSchema() : null);
    doc.put(
        "responseSchema",
        apiEndpoint.getResponseSchema() != null ? apiEndpoint.getResponseSchema() : null);
    doc.put("service", getEntityWithDisplayName(apiEndpoint.getService()));
    return doc;
  }

  private void parseSchemaFields(
      List<Field> fields, List<FlattenSchemaField> flattenSchemaFields, String parentSchemaField) {
    Optional<String> optParentField =
        Optional.ofNullable(parentSchemaField).filter(Predicate.not(String::isEmpty));
    List<TagLabel> tags = new ArrayList<>();
    for (Field field : fields) {
      String fieldName = field.getName();
      if (optParentField.isPresent()) {
        fieldName = FullyQualifiedName.add(optParentField.get(), fieldName);
      }
      if (field.getTags() != null) {
        tags = field.getTags();
      }

      FlattenSchemaField flattenSchemaField =
          FlattenSchemaField.builder().name(fieldName).description(field.getDescription()).build();

      if (!tags.isEmpty()) {
        flattenSchemaField.setTags(tags);
      }
      flattenSchemaFields.add(flattenSchemaField);
      if (field.getChildren() != null) {
        parseSchemaFields(field.getChildren(), flattenSchemaFields, field.getName());
      }
    }
  }

  public static Map<String, Float> getFields() {
    Map<String, Float> fields = SearchIndex.getDefaultFields();
    fields.put(ES_MESSAGE_SCHEMA_FIELD, 7.0f);
    fields.put("responseSchema.schemaFields.name.keyword", 5.0f);
    fields.put("responseSchema.schemaFields.description", 1.0f);
    fields.put("responseSchema.schemaFields.children.name", 7.0f);
    fields.put("responseSchema.schemaFields.children.keyword", 5.0f);
    return fields;
  }
}
