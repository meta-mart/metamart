package org.metamart.service.jdbi3;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.metamart.service.Entity;
import org.metamart.service.search.SearchListFilter;

public class SearchListFilterTest {
  @Test
  void testSimpleGetCondition() {
    SearchListFilter searchListFilter = new SearchListFilter();
    String actual = searchListFilter.getCondition();
    String expected =
        "{\"_source\": {\"exclude\": [\"fqnParts\",\"entityType\",\"suggest\"]},\"query\": {\"bool\": {\"filter\": [{\"term\": {\"deleted\": \"false\"}}]}}}";
    assertEquals(expected, actual);
  }

  @Test
  void testComplexGetCondition() {
    SearchListFilter searchListFilter = new SearchListFilter();
    searchListFilter.addQueryParam("includeFields", "field1,field2");
    searchListFilter.addQueryParam("excludeFields", "field3,field4");
    searchListFilter.addQueryParam("testCaseStatus", "failed");
    String actual = searchListFilter.getCondition(Entity.TEST_CASE);
    String expected =
        "{\"_source\": {\"exclude\": [\"fqnParts\",\"entityType\",\"suggest\",\"field3\",\"field4\"],\n\"include\": [\"field1\",\"field2\"]},\"query\": {\"bool\": {\"filter\": [{\"term\": {\"deleted\": \"false\"}},\n{\"term\": {\"testCaseResult.testCaseStatus\": \"failed\"}}]}}}";
    assertEquals(expected, actual);
  }
}
