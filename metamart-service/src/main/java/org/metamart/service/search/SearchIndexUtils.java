package org.metamart.service.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;
import org.metamart.schema.tests.DataQualityReport;
import org.metamart.schema.tests.Datum;
import org.metamart.schema.tests.type.DataQualityReportMetadata;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.TagLabel;
import org.metamart.service.util.Utilities;

public final class SearchIndexUtils {

  private SearchIndexUtils() {}

  public static List<String> parseFollowers(List<EntityReference> followersRef) {
    if (followersRef == null) {
      return Collections.emptyList();
    }
    return followersRef.stream().map(item -> item.getId().toString()).toList();
  }

  public static List<String> parseOwners(List<EntityReference> ownersRef) {
    if (ownersRef == null) {
      return Collections.emptyList();
    }
    return ownersRef.stream().map(item -> item.getId().toString()).toList();
  }

  public static void removeNonIndexableFields(Map<String, Object> doc, Set<String> fields) {
    for (String key : fields) {
      if (key.contains(".")) {
        removeFieldByPath(doc, key);
      } else {
        doc.remove(key);
      }
    }
  }

  public static void removeFieldByPath(Map<String, Object> jsonMap, String path) {
    String[] pathElements = path.split("\\.");
    Map<String, Object> currentMap = jsonMap;

    String key = pathElements[0];
    Object value = currentMap.get(key);
    if (value instanceof Map) {
      currentMap = (Map<String, Object>) value;
    } else if (value instanceof List) {
      List<Map<String, Object>> list = (List<Map<String, Object>>) value;
      for (Map<String, Object> item : list) {
        removeFieldByPath(
            item,
            Arrays.stream(pathElements, 1, pathElements.length).collect(Collectors.joining(".")));
      }
      return;
    } else {
      return;
    }

    // Remove the field at the last path element
    String lastKey = pathElements[pathElements.length - 1];
    currentMap.remove(lastKey);
  }

  private static void handleLeafTermsAggregation(
      JsonObject aggregationResults, List<Datum> reportData, Map<String, String> nodeData) {
    Optional<String> docCount = Optional.ofNullable(aggregationResults.get("doc_count").toString());
    docCount.ifPresentOrElse(
        s -> nodeData.put("document_count", s), () -> nodeData.put("document_count", null));

    Datum datum = new Datum();
    for (Map.Entry<String, String> entry : nodeData.entrySet()) {
      datum.withAdditionalProperty(entry.getKey(), entry.getValue());
    }

    reportData.add(datum);
  }

  private static void handleLeafMetricsAggregation(
      JsonObject aggregationResults,
      List<Datum> reportData,
      Map<String, String> nodeData,
      String metric) {
    String valueStr = null;
    if (aggregationResults.containsKey("value")) {
      JsonNumber value = aggregationResults.getJsonNumber("value");
      if (value != null) valueStr = value.toString();
    } else {
      valueStr = aggregationResults.getString("value_as_string", null);
    }

    nodeData.put(metric, valueStr);
    Datum datum = new Datum();
    for (Map.Entry<String, String> entry : nodeData.entrySet()) {
      datum.withAdditionalProperty(entry.getKey(), entry.getValue());
    }

    reportData.add(datum);
  }

  /*
   * Traverse the aggregation results and build the report data. Note that the method supports
   * n levels of nested aggregations, but does not support sibling aggregations.
   *
   * @Param aggregationResults the aggregation results
   * @Param reportData the report data
   * @Param nodeData the node data
   * @Param keys the keys to traverse the aggregation tree
   * @Param metric the metric to add to the report data
   * @Param dimensions the dimensions to add to the report data
   * @return the report data
   */
  private static void traverseAggregationResults(
      JsonObject aggregationResults,
      List<Datum> reportData,
      Map<String, String> nodeData,
      List<String> keys,
      String metric,
      List<String> dimensions) {

    if (keys.isEmpty()) {
      // We are in the leaf of the term aggregation. We'll add the count of documents as the metric
      handleLeafTermsAggregation(aggregationResults, reportData, nodeData);
      return;
    }

    // The current key represent the node in the aggregation tree (i.e. the current bucket)
    String currentKey = keys.get(0);
    Optional<JsonObject> aggregation =
        Optional.ofNullable(SearchClient.getAggregationObject(aggregationResults, currentKey));

    aggregation.ifPresent(
        agg -> {
          Optional<JsonArray> buckets =
              Optional.ofNullable(SearchClient.getAggregationBuckets(agg));
          if (buckets.isEmpty()) {
            if ((keys.size() > 1) && (agg.containsKey(keys.get(1)))) {
              // If the current node in the aggregation tree does not have further buckets
              // but contains the next level of aggregation, it means we are in the nested
              // aggregation
              traverseAggregationResults(
                  agg,
                  reportData,
                  nodeData,
                  keys.subList(1, keys.size()),
                  metric,
                  dimensions.subList(1, dimensions.size()));
            }
            // If the current node in the aggregation tree does not have further bucket
            // it means we are in the leaf of the metric aggregation. We'll add the metric
            handleLeafMetricsAggregation(agg, reportData, nodeData, metric);
          } else {
            buckets
                .get()
                .forEach(
                    bucket -> {
                      JsonObject bucketObject = (JsonObject) bucket;
                      Optional<JsonValue> val = Optional.of(bucketObject.get("key"));

                      val.ifPresentOrElse(
                          s -> {
                            switch (s.getValueType()) {
                              case NUMBER -> nodeData.put(
                                  dimensions.get(0), String.valueOf((JsonNumber) s));
                              default -> nodeData.put(
                                  dimensions.get(0), ((JsonString) s).getString());
                            }
                          },
                          () -> nodeData.put(dimensions.get(0), null));

                      // Traverse the next level of the aggregation tree.
                      // Dimensions and keys represent the same level in the tree.
                      // They are used for different purpose (i.e. dimensions are used to
                      // generate the report while the keys are used to traverse the aggregation
                      // tree)
                      traverseAggregationResults(
                          bucketObject,
                          reportData,
                          nodeData,
                          keys.subList(1, keys.size()),
                          metric,
                          dimensions.subList(1, dimensions.size()));
                    });
          }
        });
  }

  public static DataQualityReport parseAggregationResults(
      Optional<JsonObject> aggregationResults, DataQualityReportMetadata metadata) {
    List<Datum> reportData = new ArrayList<>();

    aggregationResults.ifPresent(
        jsonObject ->
            traverseAggregationResults(
                jsonObject,
                reportData,
                new HashMap<>(),
                metadata.getKeys(),
                metadata.getMetrics().get(0),
                metadata.getDimensions()));

    DataQualityReport report = new DataQualityReport();
    return report.withMetadata(metadata).withData(reportData);
  }

  public static List<TagLabel> parseTags(List<TagLabel> tags) {
    if (tags == null) {
      return Collections.emptyList();
    }
    return tags;
  }

  public static SearchAggregation buildAggregationTree(String aggregationString) {
    List<List<Map<String, String>>> aggregationsMetadata = new ArrayList<>();
    SearchAggregationNode root = new SearchAggregationNode("root", "root", null);
    String[] siblings = aggregationString.split(";");
    for (String sibling : siblings) {
      List<Map<String, String>> siblingAggregationsMetadata = new ArrayList<>();
      SearchAggregationNode currentNode = root;
      String[] nestedAggregations = sibling.split(Utilities.doubleQuoteRegexEscape(","), -1);
      for (String aggregation : nestedAggregations) {
        SearchAggregationNode bucketNode = new SearchAggregationNode();
        String[] nestedAggregationSiblings = aggregation.split("::");
        for (int j = 0; j < nestedAggregationSiblings.length; j++) {
          Map<String, String> nestedAggregationMetadata = new HashMap<>();

          String nestedAggregationSibling = nestedAggregationSiblings[j];
          String[] parts = nestedAggregationSibling.split(":(?!:)");
          for (String part : parts) {
            if (part.contains("&")) {
              String[] subParts = part.split("&");
              Map<String, String> params = new HashMap<>();
              for (String subPart : subParts) {
                String[] kvPairs = subPart.split(Utilities.doubleQuoteRegexEscape("="), -1);
                params.put(kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                nestedAggregationMetadata.put(
                    kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1]));
              }
              bucketNode.setValue(params);
            } else {
              String[] kvPairs = part.split(Utilities.doubleQuoteRegexEscape("="), -1);
              switch (kvPairs[0]) {
                case "aggType":
                  bucketNode.setType(Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                  nestedAggregationMetadata.put(
                      kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                  break;
                case "bucketName":
                  bucketNode.setName(Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                  nestedAggregationMetadata.put(
                      kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                  break;
                default:
                  nestedAggregationMetadata.put(
                      kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1]));
                  bucketNode.setValue(
                      Map.of(kvPairs[0], Utilities.cleanUpDoubleQuotes(kvPairs[1])));
              }
            }
          }
          currentNode.addChild(bucketNode);
          if (j < nestedAggregationSiblings.length - 1) {
            bucketNode = new SearchAggregationNode();
          }
          siblingAggregationsMetadata.add(nestedAggregationMetadata);
        }
        currentNode = bucketNode;
      }
      aggregationsMetadata.add(siblingAggregationsMetadata);
    }
    return new SearchAggregation(root, aggregationsMetadata);
  }
}
