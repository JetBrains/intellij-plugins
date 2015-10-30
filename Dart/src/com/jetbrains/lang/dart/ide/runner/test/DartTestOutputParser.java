package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.openapi.util.Key;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DartTestOutputParser {
  private static final String TEST_RUN_COMMAND = "pub.dart.snapshot run test:test";
  private static final String OBSERVATORY_MSG = "Observatory listening on";
  private static final String SOME_FAILED = "Some tests failed";
  private static final String ALL_PASSED = "All tests passed";
  private static final String EXPECTED = "Expected: ";
  private static final String NEWLINE = "\n";
  private static final String TEST_PREFIX = ": ";
  private static final String CONTINUE_SPACES = "  ";
  private static final String PASS_CODE = "\u001B[32m";
  private static final String FAIL_CODE = "\u001B[0;31m";
  private static final Pattern TIME_FORMAT = Pattern.compile("\\d+:\\d\\d");
  private static final Pattern EXPECTED_ACTUAL_RESULT = Pattern.compile("\\nExpected: (.*)\\n  Actual: (.*)\\n *\\^\\n Differ.*\\n");


  private DartTestSignaller myProcessor;
  private boolean isActive = false;
  private State myState = State.Init;
  private String myPassCount, myFailCount;
  private String myCurrentTestName = "";
  private String myFailureMessage = "", myStackTrace = "";
  private String myFailureActualText = "", myFailureExpectedText = "";
  private boolean myTestError = false;
  private int myTestId = 0;

  private enum State {Init, Info, Timestamp, Pass, Fail, TestName, Error, ErrorMessage, End}

  public DartTestOutputParser(DartTestSignaller processor) {
    myProcessor = processor;
  }

  public boolean parseNext(final String text, final Key contentType) {
    if (text == null) {
      return false;
    }
    if (text.indexOf(TEST_RUN_COMMAND) > 0) {
      isActive = true;
    }
    if (isActive) {
      return accumulate(text, contentType);
    }
    else {
      return false;
    }
  }

  private boolean accumulate(final String text, final Key contentType) {
    State currentState = myState, nextState = myState;
    switch (currentState) {
      case Init:
        if (text.startsWith(OBSERVATORY_MSG) && text.endsWith(NEWLINE)) {
          testFrameworkAttached();
          nextState = State.Timestamp;
          break;
        }
        break;
      case Info:
        break;
      case Timestamp:
        if (TIME_FORMAT.matcher(text.trim()).matches()) {
          nextState = State.Pass;
        }
        break;
      case Pass:
        // fall through
      case Fail:
        String contentCode = contentType.toString();
        if (PASS_CODE.equals(contentCode)) {
          if (text.equals(myPassCount)) {
            nextState = State.Fail;
          }
          else {
            // trigger test-pass for previous test name
            testFinished();
            if (myFailCount == null) {
              nextState = State.TestName;
            }
            else {
              nextState = State.Fail;
            }
            myPassCount = text;
          }
        }
        else if (FAIL_CODE.equals(contentCode)) {
          if (text.equals(myFailCount)) {
            nextState = State.TestName;
          }
          else {
            nextState = State.Error;
            myFailCount = text;
          }
        }
        if (text.startsWith(SOME_FAILED) || text.startsWith(ALL_PASSED)) {
          nextState = State.End;
        }
        break;
      case TestName:
        nextState = State.Timestamp;
        setTestName(text);
        break;
      case Error:
        setTestName(text);
        nextState = State.ErrorMessage;
        break;
      case ErrorMessage:
        if (text.startsWith(CONTINUE_SPACES)) {
          myFailureMessage += text.substring(CONTINUE_SPACES.length());
        }
        else {
          testFailed();
          myState = State.Timestamp;
          return accumulate(text, contentType); // goto Timestamp; simulate 1 token look-ahead
        }
        break;
      case End:
        return true;
    }
    myState = nextState;
    return true;
  }

  private void setTestName(String text) {
    if (text.startsWith(TEST_PREFIX)) text = text.substring(TEST_PREFIX.length());
    if (text.endsWith(NEWLINE)) text = text.substring(0, text.length() - NEWLINE.length());
    myCurrentTestName = text;
  }

  private void testFrameworkAttached() {
    myProcessor.signalTestFrameworkAttached();
  }

  private void testFinished() {
    if (myCurrentTestName.isEmpty()) return;
    testStarted();
    myProcessor.signalTestFinished(myCurrentTestName, myTestId, 0L);
    resetTestState();
  }

  private void testFailed() {
    if (myCurrentTestName.isEmpty()) return;
    testStarted();
    splitFailureMessage();
    myProcessor.signalTestFailure(myCurrentTestName, myTestId, myFailureMessage, myStackTrace, myTestError, myFailureActualText,
                                  myFailureExpectedText, null, 0L);
    resetTestState();
  }

  private void testStarted() {
    myProcessor.signalTestStarted(myCurrentTestName, ++myTestId, 0, null, null, null, true);
  }

  private void resetTestState() {
    myCurrentTestName = "";
    myFailureMessage = "";
    myStackTrace = null;
    myFailureActualText = null;
    myFailureExpectedText = null;
    myTestError = false;
  }

  private void splitFailureMessage() {
    String message = myFailureMessage;
    int firstExpectedIndex = message.indexOf(EXPECTED);
    if (firstExpectedIndex >= 0) {
      Matcher matcher = EXPECTED_ACTUAL_RESULT.matcher(message);
      if (matcher.find(firstExpectedIndex + EXPECTED.length())) {
        int matchEnd = matcher.end();
        myFailureExpectedText = matcher.group(1);
        myFailureActualText = matcher.group(2);
        myFailureMessage = message.substring(0, firstExpectedIndex);
        myStackTrace = message.substring(matchEnd + 1);
      }
    }
  }
}
