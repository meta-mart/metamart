package org.metamart.service.jdbi3;

import static org.metamart.common.utils.CommonUtil.nullOrEmpty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.metamart.schema.type.Include;
import org.metamart.schema.type.Relationship;
import org.metamart.schema.utils.EntityInterfaceUtil;
import org.metamart.service.Entity;
import org.metamart.service.resources.databases.DatasourceConfig;
import org.metamart.service.util.FullyQualifiedName;

public class ListFilter extends Filter<ListFilter> {
  public ListFilter() {
    this(Include.NON_DELETED);
  }

  public ListFilter(Include include) {
    this.include = include;
  }

  public String getCondition(String tableName) {
    ArrayList<String> conditions = new ArrayList<>();
    conditions.add(getIncludeCondition(tableName));
    conditions.add(getDatabaseCondition(tableName));
    conditions.add(getDatabaseSchemaCondition(tableName));
    conditions.add(getServiceCondition(tableName));
    conditions.add(getPipelineTypeCondition(tableName));
    conditions.add(getApplicationTypeCondition());
    conditions.add(getParentCondition(tableName));
    conditions.add(getDisabledCondition());
    conditions.add(getCategoryCondition(tableName));
    conditions.add(getWebhookCondition(tableName));
    conditions.add(getWebhookTypeCondition(tableName));
    conditions.add(getTestCaseCondition());
    conditions.add(getTestCaseIncidentCondition());
    conditions.add(getTestSuiteTypeCondition(tableName));
    conditions.add(getTestSuiteFQNCondition());
    conditions.add(getDomainCondition(tableName));
    conditions.add(getEntityFQNHashCondition());
    conditions.add(getTestCaseResolutionStatusType());
    conditions.add(getAssignee());
    conditions.add(getEventSubscriptionAlertType());
    conditions.add(getApiCollectionCondition(tableName));
    String condition = addCondition(conditions);
    return condition.isEmpty() ? "WHERE TRUE" : "WHERE " + condition;
  }

  private String getAssignee() {
    String assignee = queryParams.get("assignee");
    return assignee == null ? "" : String.format("assignee = '%s'", assignee);
  }

  private String getEventSubscriptionAlertType() {
    String alertType = queryParams.get("alertType");
    if (alertType == null) {
      return "";
    } else {
      if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
        return String.format("JSON_EXTRACT(json, '$.alertType') = '%s'", alertType);
      } else {
        return String.format("json->>'alertType' = '%s'", alertType);
      }
    }
  }

  private String getTestCaseResolutionStatusType() {
    String testFailureStatus = queryParams.get("testCaseResolutionStatusType");
    return testFailureStatus == null
        ? ""
        : String.format("testCaseResolutionStatusType = '%s'", testFailureStatus);
  }

  public String getIncludeCondition(String tableName) {
    String columnName = tableName == null ? "deleted" : tableName + ".deleted";
    if (include == Include.NON_DELETED || include == Include.DELETED) {
      return columnName + String.format(" =%s", include == Include.NON_DELETED ? "FALSE" : "TRUE");
    }
    return "";
  }

  public String getDatabaseCondition(String tableName) {
    String database = queryParams.get("database");
    return database == null ? "" : getFqnPrefixCondition(tableName, database, "database");
  }

  public String getDatabaseSchemaCondition(String tableName) {
    String databaseSchema = queryParams.get("databaseSchema");
    return databaseSchema == null
        ? ""
        : getFqnPrefixCondition(tableName, databaseSchema, "databaseSchema");
  }

  public String getServiceCondition(String tableName) {
    String service = queryParams.get("service");
    return service == null
        ? ""
        : getFqnPrefixCondition(tableName, EntityInterfaceUtil.quoteName(service), "service");
  }

  public String getTestSuiteFQNCondition() {
    String testSuiteName = queryParams.get("testSuite");
    return testSuiteName == null ? "" : getFqnPrefixCondition(null, testSuiteName, "testSuite");
  }

  private String getDomainCondition(String tableName) {
    String domainId = getQueryParam("domainId");
    return domainId == null
        ? ""
        : String.format(
            "(%s in (SELECT entity_relationship.toId FROM entity_relationship WHERE entity_relationship.fromEntity='domain' AND entity_relationship.fromId IN (%s) AND "
                + "relation=10))",
            nullOrEmpty(tableName) ? "id" : String.format("%s.id", tableName), domainId);
  }

  public String getApiCollectionCondition(String apiEndpoint) {
    String apiCollection = queryParams.get("apiCollection");
    return apiCollection == null
        ? ""
        : getFqnPrefixCondition(apiEndpoint, apiCollection, "apiCollection");
  }

  private String getEntityFQNHashCondition() {
    String entityFQN = getQueryParam("entityFQNHash");
    return entityFQN == null
        ? ""
        : String.format("entityFQNHash = :entityFQNHash", FullyQualifiedName.buildHash(entityFQN));
  }

  public String getParentCondition(String tableName) {
    String parentFqn = queryParams.get("parent");
    return parentFqn == null ? "" : getFqnPrefixCondition(tableName, parentFqn, "parent");
  }

  public String getDisabledCondition() {
    String disabledStr = queryParams.get("disabled");
    if (disabledStr == null) {
      return "";
    }
    boolean disabled = Boolean.parseBoolean(disabledStr);
    String disabledCondition;
    if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
      if (disabled) {
        disabledCondition = "JSON_EXTRACT(json, '$.disabled') = TRUE";
      } else {
        disabledCondition =
            "(JSON_EXTRACT(json, '$.disabled') IS NULL OR JSON_EXTRACT(json, '$.disabled') = FALSE)";
      }
    } else {
      if (disabled) {
        disabledCondition = "((c.json#>'{disabled}')::boolean)  = TRUE)";
      } else {
        disabledCondition =
            "(c.json#>'{disabled}' IS NULL OR ((c.json#>'{disabled}'):boolean) = FALSE";
      }
    }
    return disabledCondition;
  }

  public String getCategoryCondition(String tableName) {
    String category = queryParams.get("category");
    return category == null ? "" : getCategoryPrefixCondition(tableName, category);
  }

  public String getWebhookCondition(String tableName) {
    String webhookStatus = queryParams.get("status");
    return webhookStatus == null ? "" : getStatusPrefixCondition(tableName, webhookStatus);
  }

  public String getWebhookTypeCondition(String tableName) {
    String webhookType = queryParams.get("webhookType");
    return webhookType == null ? "" : getWebhookTypePrefixCondition(tableName, webhookType);
  }

  public String getPipelineTypeCondition(String tableName) {
    String pipelineType = queryParams.get("pipelineType");
    return pipelineType == null ? "" : getPipelineTypePrefixCondition(tableName, pipelineType);
  }

  public String getApplicationTypeCondition() {
    String applicationType = queryParams.get("applicationType");
    if (applicationType == null) {
      return "";
    }
    return "(appType = :applicationType)";
  }

  private String getTestCaseCondition() {
    ArrayList<String> conditions = new ArrayList<>();

    String entityFQN = getQueryParam("entityFQN");
    boolean includeAllTests = Boolean.parseBoolean(getQueryParam("includeAllTests"));
    String status = getQueryParam("testCaseStatus");
    String testSuiteId = getQueryParam("testSuiteId");
    String type = getQueryParam("testCaseType");

    if (entityFQN != null) {
      // EntityLink gets validated in the resource layer
      // EntityLink entityLinkParsed = EntityLink.parse(entityLink);
      // filter.addQueryParam("entityFQN", entityLinkParsed.getFullyQualifiedFieldValue());
      conditions.add(
          includeAllTests
              ? String.format(
                  "(entityFQN LIKE '%s%s%%' OR entityFQN = '%s')",
                  escape(entityFQN), Entity.SEPARATOR, escapeApostrophe(entityFQN))
              : String.format("entityFQN = '%s'", escapeApostrophe(entityFQN)));
    }

    if (testSuiteId != null) {
      conditions.add(
          String.format(
              "id IN (SELECT toId FROM entity_relationship WHERE fromId=:testSuiteId AND toEntity='%s' AND relation=%d AND fromEntity='%s')",
              Entity.TEST_CASE, Relationship.CONTAINS.ordinal(), Entity.TEST_SUITE));
    }

    if (status != null) {
      conditions.add("status = :testCaseStatus");
    }

    if (type != null) {
      conditions.add(
          switch (type) {
            case "table" -> "entityLink NOT LIKE '%::columns::%'";
            case "column" -> "entityLink LIKE '%::columns::%'";
            default -> "";
          });
    }

    return addCondition(conditions);
  }

  private String getTestCaseIncidentCondition() {
    String originEntityFQN = getQueryParam("originEntityFQN");
    if (originEntityFQN != null) {
      queryParams.put(
          "originEntityFQNLike",
          originEntityFQN + ".%"); // Add wildcard to get all column test cases under the entity
      return "(testCaseEntityFQN = :originEntityFQN\n"
          + " OR testCaseEntityFQN LIKE :originEntityFQNLike)";
    }
    return "";
  }

  private String getTestSuiteTypeCondition(String tableName) {
    String testSuiteType = getQueryParam("testSuiteType");

    if (testSuiteType == null) {
      return "";
    }

    return switch (testSuiteType) {
      case ("executable") -> {
        if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
          yield String.format(
              "(JSON_UNQUOTE(JSON_EXTRACT(%s.json, '$.executable')) = 'true')", tableName);
        }
        yield String.format("(%s.json->>'executable' = 'true')", tableName);
      }
      case ("logical") -> {
        if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
          yield String.format(
              "(JSON_UNQUOTE(JSON_EXTRACT(%s.json, '$.executable')) = 'false' OR JSON_UNQUOTE(JSON_EXTRACT(%s.json, '$.executable')) IS NULL)",
              tableName, tableName);
        }
        yield String.format(
            "(%s.json->>'executable' = 'false' or %s.json -> 'executable' is null)",
            tableName, tableName);
      }
      default -> "";
    };
  }

  private String getFqnPrefixCondition(String tableName, String fqnPrefix, String paramName) {
    String databaseFqnHash =
        String.format("%s%s%%", FullyQualifiedName.buildHash(fqnPrefix), Entity.SEPARATOR);
    queryParams.put(paramName + "Hash", databaseFqnHash);
    return tableName == null
        ? String.format("fqnHash LIKE :%s", paramName + "Hash")
        : String.format("%s.fqnHash LIKE :%s", tableName, paramName + "Hash");
  }

  private String getWebhookTypePrefixCondition(String tableName, String typePrefix) {
    typePrefix = String.format("%s%%", escape(typePrefix));
    queryParams.put("typePrefix", typePrefix);
    return tableName == null
        ? "webhookType LIKE :typePrefix"
        : tableName + ".webhookType LIKE typePrefix";
  }

  private String getPipelineTypePrefixCondition(String tableName, String pipelineType) {
    pipelineType = escape(pipelineType);
    String inCondition = getInConditionFromString(pipelineType);
    if (Boolean.TRUE.equals(DatasourceConfig.getInstance().isMySQL())) {
      return tableName == null
          ? String.format("pipelineType IN (%s)", inCondition)
          : String.format(
              "%s.JSON_UNQUOTE(JSON_EXTRACT(ingestion_pipeline_entity.json, '$.pipelineType')) IN (%s)",
              tableName, inCondition);
    }
    return tableName == null
        ? String.format("pipelineType IN (%s)", inCondition)
        : String.format("%s.json->>'pipelineType' IN (%s)", tableName, inCondition);
  }

  private String getInConditionFromString(String condition) {
    return Arrays.stream(condition.split(","))
        .map(s -> String.format("'%s'", s))
        .collect(Collectors.joining(","));
  }

  private String getCategoryPrefixCondition(String tableName, String category) {
    category = String.format("%s%%", escape(category));
    queryParams.put("escapedCategory", category);
    return tableName == null
        ? "category LIKE :escapedCategory"
        : tableName + ".category LIKE :escapedCategory";
  }

  private String getStatusPrefixCondition(String tableName, String statusPrefix) {
    if (!statusPrefix.isEmpty()) {
      List<String> statusList = new ArrayList<>(Arrays.asList(statusPrefix.split(",")));
      List<String> condition = new ArrayList<>();
      for (String s : statusList) {
        String format = "\"" + s + "\"";
        condition.add(format);
      }
      queryParams.put("statusList", String.join(",", condition));
      return "status in (:statusList)";
    }
    queryParams.put("statusPrefix", String.format("%s%%", statusPrefix));
    return tableName == null
        ? "status LIKE :statusPrefix"
        : tableName + ".status LIKE :statusPrefix";
  }

  protected String addCondition(List<String> conditions) {
    StringBuilder condition = new StringBuilder();

    for (String c : conditions) {
      if (!c.isEmpty()) {
        if (!condition.isEmpty()) {
          // Add `AND` between conditions
          condition.append(" AND ");
        }
        condition.append(c);
      }
    }
    return condition.toString();
  }

  public static String escapeApostrophe(String name) {
    // Escape string to be using in LIKE clause
    // "'" is used for indicated start and end of the string. Use "''" to escape it.
    // "_" is a wildcard and looks for any single character. Add "\\" in front of it to escape it
    return name.replace("'", "''");
  }

  public static String escape(String name) {
    // Escape string to be using in LIKE clause
    // "'" is used for indicated start and end of the string. Use "''" to escape it.
    name = escapeApostrophe(name);
    // "_" is a wildcard and looks for any single character. Add "\\" in front of it to escape it
    return name.replaceAll("_", "\\\\_");
  }
}
