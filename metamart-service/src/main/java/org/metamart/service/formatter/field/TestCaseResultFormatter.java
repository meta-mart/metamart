package org.metamart.service.formatter.field;

import org.metamart.schema.entity.feed.FeedInfo;
import org.metamart.schema.entity.feed.TestCaseResultFeedInfo;
import org.metamart.schema.entity.feed.Thread;
import org.metamart.schema.tests.TestCase;
import org.metamart.schema.tests.TestSuite;
import org.metamart.schema.tests.type.TestCaseResult;
import org.metamart.schema.tests.type.TestCaseStatus;
import org.metamart.schema.type.FieldChange;
import org.metamart.schema.type.Include;
import org.metamart.service.Entity;
import org.metamart.service.formatter.decorators.FeedMessageDecorator;
import org.metamart.service.formatter.decorators.MessageDecorator;
import org.metamart.service.jdbi3.TestCaseRepository;
import org.metamart.service.resources.feeds.MessageParser;
import org.metamart.service.util.JsonUtils;
import org.metamart.service.util.ResultList;

public class TestCaseResultFormatter extends DefaultFieldFormatter {
  public static final String TEST_RESULT_FIELD = "testCaseResult";
  private static final String HEADER_MESSAGE = "%s added results to test Case %s";

  public TestCaseResultFormatter(
      MessageDecorator<?> messageDecorator, Thread thread, FieldChange fieldChange) {
    super(messageDecorator, thread, fieldChange);
  }

  @Override
  public String formatAddedField() {
    String message;
    if (fieldChangeName.equals(TEST_RESULT_FIELD)) {
      message = transformTestCaseResult(messageDecorator, thread, fieldChange);
    } else {
      message = super.formatAddedField();
    }
    // Update the thread with the required information
    populateTestResultFeedInfo(Thread.FieldOperation.UPDATED, message);
    return message;
  }

  @Override
  public String formatUpdatedField() {
    String message;
    if (fieldChangeName.equals(TEST_RESULT_FIELD)) {
      message = transformTestCaseResult(messageDecorator, thread, fieldChange);
    } else {
      message = super.formatUpdatedField();
    }
    // Update the thread with the required information
    populateTestResultFeedInfo(Thread.FieldOperation.UPDATED, message);
    return message;
  }

  private void populateTestResultFeedInfo(Thread.FieldOperation operation, String threadMessage) {
    long currentTime = System.currentTimeMillis();
    long lastWeekTime = currentTime - 7 * 24 * 60 * 60 * 1000;
    TestCaseRepository testCaseRepository =
        (TestCaseRepository) Entity.getEntityRepository(Entity.TEST_CASE);
    TestCase testCaseEntity =
        Entity.getEntity(
            thread.getEntityRef().getType(),
            thread.getEntityRef().getId(),
            "id,testSuite",
            Include.ALL);
    TestSuite testSuiteEntity = Entity.getEntity(testCaseEntity.getTestSuite(), "id", Include.ALL);
    ResultList<TestCaseResult> testCaseResultResultList =
        testCaseRepository.getTestCaseResults(
            testCaseEntity.getFullyQualifiedName(), lastWeekTime, currentTime);
    TestCaseResultFeedInfo testCaseResultFeedInfo =
        new TestCaseResultFeedInfo()
            .withTestCaseResult(testCaseResultResultList.getData())
            .withEntityTestResultSummary(testSuiteEntity.getTestCaseResultSummary())
            .withParameterValues(testCaseEntity.getParameterValues());
    FeedInfo feedInfo =
        new FeedInfo()
            .withHeaderMessage(getHeaderForTestResultUpdate())
            .withFieldName(TEST_RESULT_FIELD)
            .withEntitySpecificInfo(testCaseResultFeedInfo);
    populateThreadFeedInfo(
        thread, threadMessage, Thread.CardStyle.TEST_CASE_RESULT, operation, feedInfo);
  }

  private String getHeaderForTestResultUpdate() {
    return String.format(HEADER_MESSAGE, thread.getUpdatedBy(), thread.getEntityUrlLink());
  }

  private String transformTestCaseResult(
      MessageDecorator<?> messageFormatter, Thread thread, FieldChange fieldChange) {
    TestCase testCaseEntity =
        Entity.getEntity(
            thread.getEntityRef().getType(), thread.getEntityRef().getId(), "id", Include.ALL);
    String testCaseName = testCaseEntity.getName();
    TestCaseResult result = JsonUtils.convertValue(fieldChange.getNewValue(), TestCaseResult.class);
    if (result != null) {
      String format =
          String.format(
              "Test Case %s is %s in %s",
              messageFormatter.getBold(),
              messageFormatter.getBold(),
              MessageParser.EntityLink.parse(testCaseEntity.getEntityLink()).getEntityFQN());
      return String.format(
          format, testCaseName, getStatusMessage(messageFormatter, result.getTestCaseStatus()));
    }
    String format =
        String.format(
            "Test Case %s is updated in %s",
            messageFormatter.getBold(), messageFormatter.getBold());
    return String.format(
        format,
        testCaseName,
        MessageParser.EntityLink.parse(testCaseEntity.getEntityLink()).getEntityFQN());
  }

  private String getStatusMessage(MessageDecorator<?> messageDecorator, TestCaseStatus status) {
    if (messageDecorator instanceof FeedMessageDecorator) {
      return switch (status) {
        case Success -> "<span style=\"color:#48CA9E\">Passed</span>";
        case Failed -> "<span style=\"color:#F24822\">Failed</span>";
        case Aborted -> "<span style=\"color:#FFBE0E\">Aborted</span>";
        case Queued -> "<span style=\"color:#959595\">Queued</span>";
      };
    } else {
      return switch (status) {
        case Success -> "Passed";
        case Failed -> "Failed";
        case Aborted -> "Aborted";
        case Queued -> "Queued";
      };
    }
  }
}
