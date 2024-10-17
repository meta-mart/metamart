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

package org.metamart.csv;

import static org.metamart.common.utils.CommonUtil.listOf;
import static org.metamart.common.utils.CommonUtil.listOrEmpty;
import static org.metamart.common.utils.CommonUtil.nullOrEmpty;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVFormat.Builder;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.metamart.schema.type.EntityReference;
import org.metamart.schema.type.TagLabel;
import org.metamart.schema.type.csv.CsvFile;
import org.metamart.schema.type.csv.CsvHeader;

public final class CsvUtil {
  public static final String SEPARATOR = ",";
  public static final String FIELD_SEPARATOR = ";";

  public static final String ENTITY_TYPE_SEPARATOR = ":";
  public static final String LINE_SEPARATOR = "\r\n";

  public static final String INTERNAL_ARRAY_SEPARATOR = "|";

  private CsvUtil() {
    // Utility class hides the constructor
  }

  public static String formatCsv(CsvFile csvFile) throws IOException {
    // CSV file is generated by the backend and the data exported is expected to be correct. Hence,
    // no validation
    StringWriter writer = new StringWriter();
    List<String> headers = getHeaders(csvFile.getHeaders());
    CSVFormat csvFormat =
        Builder.create(CSVFormat.DEFAULT).setHeader(headers.toArray(new String[0])).build();
    try (CSVPrinter printer = new CSVPrinter(writer, csvFormat)) {
      for (List<String> csvRecord : listOrEmpty(csvFile.getRecords())) {
        printer.printRecord(csvRecord);
      }
    }
    return writer.toString();
  }

  /** Get headers from CsvHeaders */
  public static List<String> getHeaders(List<CsvHeader> csvHeaders) {
    List<String> headers = new ArrayList<>();
    for (CsvHeader header : csvHeaders) {
      String headerString = header.getName();
      if (Boolean.TRUE.equals(header.getRequired()))
        headerString = String.format("%s*", header.getName());
      headers.add(headerString);
    }
    return headers;
  }

  public static String recordToString(CSVRecord csvRecord) {
    return recordToString(csvRecord.toList());
  }

  public static String recordToString(List<String> fields) {
    return nullOrEmpty(fields)
        ? ""
        : fields.stream().map(CsvUtil::quoteCsvField).collect(Collectors.joining(SEPARATOR));
  }

  public static String recordToString(String[] fields) {
    return recordToString(Arrays.asList(fields));
  }

  public static List<String> fieldToStrings(String field) {
    // Split a field that contains multiple strings separated by FIELD_SEPARATOR
    return field == null || field.isBlank() ? null : listOf(field.split(FIELD_SEPARATOR));
  }

  public static List<String> fieldToEntities(String field) {
    // Split a field that contains multiple strings separated by FIELD_SEPARATOR
    return field == null ? null : listOf(field.split(ENTITY_TYPE_SEPARATOR));
  }

  public static List<String> fieldToInternalArray(String field) {
    // Split a fieldValue that contains multiple elements of an array separated by
    // INTERNAL_ARRAY_SEPARATOR
    if (field == null || field.isBlank()) {
      return Collections.emptyList();
    }
    return listOf(field.split(Pattern.quote(INTERNAL_ARRAY_SEPARATOR)));
  }

  /**
   * Parses a field containing key-value pairs separated by FIELD_SEPARATOR, correctly handling quotes.
   * Each key-value pair may also be enclosed in quotes, especially if it contains delimiter like (SEPARATOR , FIELD_SEPARATOR).
   * Input Example:
   * "key1:value1;key2:value2;\"key3:value;with;semicolon\""
   * Output: [key1:value1, key2:value2, key3:value;with;semicolon]
   *
   */
  public static List<String> fieldToExtensionStrings(String field) throws IOException {
    if (field == null || field.isBlank()) {
      return List.of();
    }

    // Case when semicolon is part of the fieldValue - Replace semicolons within quoted strings with
    // a placeholder
    String preprocessedField =
        Pattern.compile("\"([^\"]*)\"") // Matches content inside double quotes
            .matcher(field)
            .replaceAll(mr -> "\"" + mr.group(1).replace(";", "__SEMICOLON__") + "\"");

    preprocessedField = preprocessedField.replace("\n", "\\n").replace("\"", "\\\"");

    CSVFormat format =
        CSVFormat.DEFAULT
            .withDelimiter(';')
            .withQuote('"')
            .withRecordSeparator(null)
            .withIgnoreSurroundingSpaces(true)
            .withIgnoreEmptyLines(true)
            .withEscape('\\'); // Use backslash for escaping special characters

    try (CSVParser parser = CSVParser.parse(new StringReader(preprocessedField), format)) {
      return parser.getRecords().stream()
          .flatMap(CSVRecord::stream)
          .map(
              value ->
                  value.replace("__SEMICOLON__", ";")) // Restore original semicolons and newlines
          .map(
              value ->
                  value.startsWith("\"") && value.endsWith("\"") // Remove outer quotes if present
                      ? value.substring(1, value.length() - 1)
                      : value)
          .toList();
    }
  }

  /**
   * Parses a field containing column values separated by SEPARATOR, correctly handling quotes.
   * Each value  enclosed in quotes, especially if it contains delimiter like SEPARATOR.
   * Input Example:
   * "value1,value2,\"value,with,comma\""
   * Output: [value1, value2, value,with,comma]
   *
   */
  public static List<String> fieldToColumns(String field) throws IOException {
    if (field == null || field.isBlank()) {
      return Collections.emptyList();
    }

    // Case when comma is part of the columnValue - Replace commas within quoted strings with a
    // placeholder
    String preprocessedField =
        Pattern.compile("\"([^\"]*)\"")
            .matcher(field)
            .replaceAll(mr -> "\"" + mr.group(1).replace(",", "__COMMA__") + "\"");

    preprocessedField = preprocessedField.replace("\n", "\\n").replace("\"", "\\\"");

    CSVFormat format = CSVFormat.DEFAULT.withDelimiter(',').withQuote('"').withEscape('\\');

    List<String> columns;
    try (CSVParser parser = CSVParser.parse(new StringReader(preprocessedField), format)) {
      columns =
          parser.getRecords().stream()
              .flatMap(CSVRecord::stream)
              .map(value -> value.replace("__COMMA__", ","))
              .map(
                  value ->
                      value.startsWith("\"")
                              && value.endsWith("\"") // Remove outer quotes if present
                          ? value.substring(1, value.length() - 1)
                          : value)
              .collect(Collectors.toList());
    }

    return columns;
  }

  public static String quote(String field) {
    return String.format("\"%s\"", field);
  }

  /** Quote a CSV field made of multiple strings that has SEPARATOR or FIELD_SEPARATOR with " " */
  public static String quoteField(List<String> field) {
    return nullOrEmpty(field)
        ? ""
        : field.stream().map(CsvUtil::quoteCsvField).collect(Collectors.joining(FIELD_SEPARATOR));
  }

  public static void addField(List<String> csvRecord, Boolean field) {
    csvRecord.add(field == null ? "" : field.toString());
  }

  public static List<String> addField(List<String> csvRecord, String field) {
    csvRecord.add(field);
    return csvRecord;
  }

  public static List<String> addFieldList(List<String> csvRecord, List<String> field) {
    csvRecord.add(quoteField(field));
    return csvRecord;
  }

  public static List<String> addEntityReferences(
      List<String> csvRecord, List<EntityReference> refs) {
    csvRecord.add(
        nullOrEmpty(refs)
            ? null
            : refs.stream()
                .map(EntityReference::getFullyQualifiedName)
                .collect(Collectors.joining(FIELD_SEPARATOR)));
    return csvRecord;
  }

  public static List<String> addEntityReference(List<String> csvRecord, EntityReference ref) {
    csvRecord.add(nullOrEmpty(ref) ? null : ref.getFullyQualifiedName());
    return csvRecord;
  }

  public static List<String> addTagLabels(List<String> csvRecord, List<TagLabel> tags) {
    csvRecord.add(
        nullOrEmpty(tags)
            ? null
            : tags.stream()
                .filter(
                    tagLabel ->
                        tagLabel.getSource().equals(TagLabel.TagSource.CLASSIFICATION)
                            && !tagLabel.getTagFQN().split("\\.")[0].equals("Tier")
                            && !tagLabel.getLabelType().equals(TagLabel.LabelType.DERIVED))
                .map(TagLabel::getTagFQN)
                .collect(Collectors.joining(FIELD_SEPARATOR)));

    return csvRecord;
  }

  public static List<String> addGlossaryTerms(List<String> csvRecord, List<TagLabel> tags) {
    csvRecord.add(
        nullOrEmpty(tags)
            ? null
            : tags.stream()
                .filter(
                    tagLabel ->
                        tagLabel.getSource().equals(TagLabel.TagSource.GLOSSARY)
                            && !tagLabel.getTagFQN().split("\\.")[0].equals("Tier"))
                .map(TagLabel::getTagFQN)
                .collect(Collectors.joining(FIELD_SEPARATOR)));

    return csvRecord;
  }

  public static List<String> addTagTiers(List<String> csvRecord, List<TagLabel> tags) {
    csvRecord.add(
        nullOrEmpty(tags)
            ? null
            : tags.stream()
                .filter(
                    tagLabel ->
                        tagLabel.getSource().equals(TagLabel.TagSource.CLASSIFICATION)
                            && tagLabel.getTagFQN().split("\\.")[0].equals("Tier"))
                .map(TagLabel::getTagFQN)
                .collect(Collectors.joining(FIELD_SEPARATOR)));

    return csvRecord;
  }

  public static void addOwners(List<String> csvRecord, List<EntityReference> owners) {
    csvRecord.add(
        nullOrEmpty(owners)
            ? null
            : owners.stream()
                .map(owner -> (owner.getType() + ENTITY_TYPE_SEPARATOR + owner.getName()))
                .collect(Collectors.joining(FIELD_SEPARATOR)));
  }

  public static void addReviewers(List<String> csvRecord, List<EntityReference> reviewers) {
    csvRecord.add(
        nullOrEmpty(reviewers)
            ? null
            : reviewers.stream()
                .map(reviewer -> (reviewer.getType() + ENTITY_TYPE_SEPARATOR + reviewer.getName()))
                .collect(Collectors.joining(FIELD_SEPARATOR)));
  }

  private static String quoteCsvField(String str) {
    if (str.contains(SEPARATOR) || str.contains(FIELD_SEPARATOR)) {
      return quote(str);
    }
    return str;
  }

  private static String quoteCsvFieldForSeparator(String str) {
    if (str.contains(SEPARATOR)) {
      return quote(str);
    }
    return str;
  }

  public static List<String> addExtension(List<String> csvRecord, Object extension) {
    if (extension == null) {
      csvRecord.add(null);
      return csvRecord;
    }

    ObjectMapper objectMapper = new ObjectMapper();
    Map<String, Object> extensionMap = objectMapper.convertValue(extension, Map.class);

    String extensionString =
        extensionMap.entrySet().stream()
            .map(
                entry -> {
                  String key = entry.getKey();
                  Object value = entry.getValue();
                  return CsvUtil.quoteCsvField(key + ENTITY_TYPE_SEPARATOR + formatValue(value));
                })
            .collect(Collectors.joining(FIELD_SEPARATOR));

    csvRecord.add(extensionString);
    return csvRecord;
  }

  private static String formatValue(Object value) {
    if (value instanceof Map) {
      return formatMapValue((Map<String, Object>) value);
    }

    if (value instanceof List) {
      return formatListValue((List<?>) value);
    }

    return value != null ? value.toString() : "";
  }

  private static String formatMapValue(Map<String, Object> valueMap) {
    if (isEntityReference(valueMap)) {
      return formatEntityReference(valueMap);
    } else if (isTimeInterval(valueMap)) {
      return formatTimeInterval(valueMap);
    } else if (isTableType(valueMap)) {
      return formatTableRows(valueMap);
    }

    return valueMap.toString();
  }

  private static String formatListValue(List<?> list) {
    if (list.isEmpty()) {
      return "";
    }

    if (list.get(0) instanceof Map) {
      return list.stream()
          .map(item -> formatMapValue((Map<String, Object>) item))
          .collect(Collectors.joining(INTERNAL_ARRAY_SEPARATOR));
    } else {
      return list.stream()
          .map(Object::toString)
          .collect(Collectors.joining(INTERNAL_ARRAY_SEPARATOR));
    }
  }

  private static boolean isEntityReference(Map<String, Object> valueMap) {
    return valueMap.containsKey("type") && valueMap.containsKey("fullyQualifiedName");
  }

  private static boolean isTimeInterval(Map<String, Object> valueMap) {
    return valueMap.containsKey("start") && valueMap.containsKey("end");
  }

  private static boolean isTableType(Map<String, Object> valueMap) {
    return valueMap.containsKey("rows") && valueMap.containsKey("columns");
  }

  private static String formatEntityReference(Map<String, Object> valueMap) {
    return valueMap.get("type") + ENTITY_TYPE_SEPARATOR + valueMap.get("fullyQualifiedName");
  }

  private static String formatTimeInterval(Map<String, Object> valueMap) {
    return valueMap.get("start") + ENTITY_TYPE_SEPARATOR + valueMap.get("end");
  }

  private static String formatTableRows(Map<String, Object> valueMap) {
    List<String> columns = (List<String>) valueMap.get("columns");
    List<Map<String, Object>> rows = (List<Map<String, Object>>) valueMap.get("rows");

    return rows.stream()
        .map(
            row ->
                columns.stream()
                    .map(
                        column ->
                            quoteCsvFieldForSeparator(row.getOrDefault(column, "").toString()))
                    .collect(Collectors.joining(SEPARATOR)))
        .collect(Collectors.joining(INTERNAL_ARRAY_SEPARATOR));
  }
}
