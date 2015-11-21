package com.jetbrains.lang.dart.ide.runner.test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.ServiceMessageBuilder;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.openapi.util.Key;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageVisitor;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartTestEventsConverter extends OutputToGeneralTestEventsConverter {

  private static final String TYPE_START = "start";
  private static final String TYPE_ERROR = "error";
  private static final String TYPE_PRINT = "print";
  private static final String TYPE_PASS = "pass";
  private static final String TYPE_FAIL = "fail";
  private static final String TYPE_SKIP = "skip";
  private static final String TYPE_EXIT = "exit";
  private static final String TYPE_ENTER = "enter";

  private static final String JSON_TYPE = "type";
  private static final String JSON_NAME = "name";
  private static final String JSON_MILLIS = "time";
  private static final String JSON_MESSAGE = "message";
  private static final String JSON_ERROR_MESSAGE = "errorMessage";
  private static final String JSON_FAIL_MESSAGE = "failMessage";
  private static final String JSON_STACK_TRACE = "stackTrace";
  private static final String JSON_REASON = "reason";
  private static final String JSON_COUNT = "count";

  private static final String NEWLINE = "\n";
  private static final String OBSERVATORY_MSG = "Observatory listening on";
  private static final String EXPECTED = "Expected: ";
  private static final Pattern EXPECTED_ACTUAL_RESULT = Pattern.compile("\\nExpected: (.*)\\n  Actual: (.*)\\n *\\^\\n Differ.*\\n");

  // The test ID really should come from the test runner. There's no way to identify which message is associated with which
  // test if tests are run in parallel.
  private int myTestId = 0;
  private int myParentId = 0;
  // In theory, test events could be generated asynchronously and out of order. We might want to keep a map of tests to start times
  // so we get accurate durations when tests end. We need a unique identifier for tests to do that, so it is overkill for now.
  private long startMillis;
  // This is from the Go test processor. I assume it is there to deal with non-standard output.
  private boolean myOutputAppeared = false;
  private Key myCurrentOutputType;
  private ServiceMessageVisitor myCurrentVisitor;

  public DartTestEventsConverter(@NotNull final String testFrameworkName, @NotNull final TestConsoleProperties consoleProperties) {
    super(testFrameworkName, consoleProperties);
  }

  protected boolean processServiceMessages(final String text, final Key outputType, final ServiceMessageVisitor visitor)
    throws ParseException {
    myCurrentOutputType = outputType;
    myCurrentVisitor = visitor;
    // service message parser expects line like "##teamcity[ .... ]" without whitespaces in the end.
    return processEventText(text);
  }

  private boolean processEventText(final String text) throws JsonSyntaxException, ParseException {
    JsonParser jp = new JsonParser();
    JsonElement elem;
    try {
      elem = jp.parse(text);
    }
    catch (JsonSyntaxException ex) {
      //if (text.startsWith(OBSERVATORY_MSG) && text.endsWith(NEWLINE)) {
      //  processEnter();
      //}
      return super.processServiceMessages(text, myCurrentOutputType, myCurrentVisitor);
    }
    if (elem == null || !elem.isJsonObject()) return false;
    return process(elem.getAsJsonObject());
  }

  private boolean process(JsonObject obj) throws JsonSyntaxException, ParseException {
    String type = obj.get(JSON_TYPE).getAsString();
    if (TYPE_START.equals(type)) {
      return processStart(obj);
    }
    else if (TYPE_ERROR.equals(type)) {
      return processError(obj);
    }
    else if (TYPE_PASS.equals(type)) {
      return processPass(obj);
    }
    else if (TYPE_FAIL.equals(type)) {
      return processFail(obj);
    }
    else if (TYPE_SKIP.equals(type)) {
      return processSkip(obj);
    }
    else if (TYPE_EXIT.equals(type)) {
      return processExit(obj);
    }
    else if (TYPE_PRINT.equals(type)) {
      return processPrint(obj);
    }
    else if (TYPE_ENTER.equals(type)) {
      return processEnter();
    }
    else {
      throw new JsonSyntaxException("Unexpected type: " + type + " (check for SDK update)");
    }
  }

  // TODO All nodes need to include "nodeId" attribute.
  private boolean processStart(JsonObject obj) throws ParseException {
    myTestId += 1;
    myParentId = 0;
    ServiceMessageBuilder testStarted = ServiceMessageBuilder.testStarted(testName(obj));
    testStarted.addAttribute("locationHint", "unknown");
    startMillis = testMillis(obj);
    myOutputAppeared = false;
    boolean result = finishMessage(testStarted);
    myParentId = myTestId;
    return result;
  }

  private boolean processError(JsonObject obj) throws ParseException {
    long duration = testMillis(obj) - startMillis;
    //myProcessor.signalTestFailure(testName(obj), myTestId, errorMessage(obj), stackTrace(obj), true, null, null, null, duration);
    ServiceMessageBuilder testError = ServiceMessageBuilder.testFailed(testName(obj));
    testError.addAttribute("error", String.valueOf(true));
    testError.addAttribute("message", errorMessage(obj) + NEWLINE);
    ServiceMessageBuilder message = ServiceMessageBuilder.testStdErr(testName(obj));
    message.addAttribute("out", stackTrace(obj));
    return finishMessage(testError) && finishMessage(message) && processDone(obj);
  }

  private boolean processPass(JsonObject obj) throws ParseException {
    // Tests that pass have no specific marker; passing is indicated simply by the absence of a failure marker.
    return processDone(obj);
  }

  private boolean processDone(JsonObject obj) throws ParseException {
    long duration = testMillis(obj) - startMillis;
    ServiceMessageBuilder testFinished = ServiceMessageBuilder.testFinished(testName(obj));
    testFinished.addAttribute("duration", Long.toString(duration));
    return finishMessage(testFinished);
  }

  private boolean processFail(JsonObject obj) throws ParseException {
    long duration = testMillis(obj) - startMillis;
    String message = failMessage(obj);
    String expectedText = null, actualText = null, failureMessage = message;
    int firstExpectedIndex = message.indexOf(EXPECTED);
    if (firstExpectedIndex >= 0) {
      Matcher matcher = EXPECTED_ACTUAL_RESULT.matcher(message);
      if (matcher.find(firstExpectedIndex + EXPECTED.length())) {
        expectedText = matcher.group(1);
        actualText = matcher.group(2);
        failureMessage = message.substring(0, firstExpectedIndex);
      }
    }
    // The stack trace could be null, but we disallow that for consistency with all the transmitted values.
    //myProcessor.signalTestFailure(testName(obj), myTestId, failureMessage, stackTrace(obj), false, actualText, expectedText, null, duration);
    ServiceMessageBuilder testError = ServiceMessageBuilder.testFailed(testName(obj));
    testError.addAttribute("out", stackTrace(obj));
    testError.addAttribute("error", String.valueOf(false));
    testError.addAttribute("message", failureMessage + "\n");
    testError.addAttribute("expectedFile", expectedText);
    testError.addAttribute("actualFile", actualText);
    ServiceMessageBuilder msg = ServiceMessageBuilder.testStdErr(testName(obj));
    msg.addAttribute("out", stackTrace(obj));
    return finishMessage(testError) && finishMessage(msg) && processDone(obj);
  }

  private boolean processSkip(JsonObject obj) throws ParseException {
    //myProcessor.signalTestSkipped(testName(obj), skipReason(obj), null);
    ServiceMessageBuilder message = ServiceMessageBuilder.testIgnored(testName(obj));
    message.addAttribute("out", skipReason(obj));
    return finishMessage(message) && processDone(obj);
  }

  private boolean processPrint(JsonObject obj) throws ParseException {
    ServiceMessageBuilder message = ServiceMessageBuilder.testStdOut(testName(obj));
    message.addAttribute("out", message(obj));
    return finishMessage(message);
  }

  private boolean processEnter() {
    // This apparently is a no-op: myProcessor.signalTestFrameworkAttached();
    return true;
  }

  private boolean processExit(JsonObject obj) {
    // Tests are done.
    return true;
  }

  private boolean finishMessage(ServiceMessageBuilder msg) throws ParseException {
    msg.addAttribute("nodeId", String.valueOf(myTestId));
    msg.addAttribute("parentNodeId", String.valueOf(myParentId));
    return super.processServiceMessages(msg.toString(), myCurrentOutputType, myCurrentVisitor);
  }

  private static long testMillis(JsonObject obj) {
    JsonElement val = obj.get(JSON_MILLIS);
    if (val == null || !val.isJsonPrimitive()) return 0L;
    return val.getAsLong();
  }

  @NotNull
  private static String testName(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_NAME, "<no name>");
  }

  @NotNull
  private static String failMessage(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_FAIL_MESSAGE, "<no fail message>");
  }

  @NotNull
  private static String errorMessage(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_ERROR_MESSAGE, "<no error message>");
  }

  @NotNull
  private static String message(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_MESSAGE, "<no message>");
  }

  @NotNull
  private static String stackTrace(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_STACK_TRACE, "<no stack trace>");
  }

  @NotNull
  private static String skipReason(JsonObject obj) {
    return nonNullJsonValue(obj, JSON_REASON, "<no skip reason>");
  }

  @NotNull
  private static String nonNullJsonValue(@NotNull JsonObject obj, @NotNull String id, @NotNull String def) {
    JsonElement val = obj.get(id);
    if (val == null || !val.isJsonPrimitive()) return def;
    return val.getAsString();
  }
}
