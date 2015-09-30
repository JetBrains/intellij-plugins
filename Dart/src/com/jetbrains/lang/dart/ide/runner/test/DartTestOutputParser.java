package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.testframework.sm.runner.events.TestFailedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestFinishedEvent;
import com.intellij.execution.testframework.sm.runner.events.TestStartedEvent;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

public class DartTestOutputParser {
  private static final String TEST_RUN_COMMAND = "pub.dart.snapshot run test:test";
  private static final String OBSERVATORY_MSG = "Observatory listening on";
  private static final String SOME_FAILED = "Some tests failed";
  private static final String ALL_PASSED = "All tests passed";
  private static final String NEWLINE = "\n";
  private static final String TEST_PREFIX = ": ";
  private static final String PASS_CODE = "\u001B[32m";
  private static final String FAIL_CODE = "\u001B[0;31m";
  private static final Pattern TIME_FORMAT = Pattern.compile("\\d+:\\d\\d");

  private DartTestToGeneralTestEventsConverter myProcessor;
  private boolean isFirstTime = true;
  private boolean isActive = false;
  private boolean didFail = false;
  private State myState = State.Init;
  private String myPassCount, myFailCount;
  private String myCurrentTestName = "";
  private String myFailureMessage = "", myStackTrace = "";
  private String myFailureActualText = "", myFailureExpectedText = "";
  private boolean myTestError = false;
  private int myTestId = 1;

  private enum State {Init, Info, Timestamp, Pass, Fail, TestName, Message, Error, ErrorMessage, End}

  public DartTestOutputParser(DartTestToGeneralTestEventsConverter processor) {
    myProcessor = processor;
  }

  public boolean parseNext(final String text, final Key contentType) {
    if (text == null) {
      return false;
    }
    if (isFirstTime) {
      if (text.indexOf(TEST_RUN_COMMAND) > 0) {
        isActive = true;
      }
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
          nextState = State.Timestamp;
          return false;
        }
        break;
      case Info:
        break;
      case Timestamp:
        if (TIME_FORMAT.matcher(text.trim()).matches()) {
          nextState = State.Pass;
          // emit test-found string
        }
        break;
      case Pass:
        // fall through
      case Fail:
        String contentCode = contentType.toString();
        if (PASS_CODE.equals(contentCode)) {
          currentState = State.Pass;
          if (text.equals(myPassCount)) {
            nextState = State.Fail;
          }
          else {
            // trigger test-pass for previous test name
            testFinished();
            nextState = State.TestName;
            myPassCount = text;
          }
          // emit test-pass string
        }
        else if (FAIL_CODE.equals(contentCode)) {
          currentState = State.Fail;
          if (text.equals(myFailCount)) {
            nextState = State.Message;
            didFail = true;
          }
          else {
            testFailed();
            nextState = State.Error;
            myFailCount = text;
            // emit test-fail string
          }
        }
        if (text.startsWith(SOME_FAILED)) {
          nextState = State.End;
        }
        else if (text.startsWith(ALL_PASSED)) {
          nextState = State.End;
        }
        break;
      case TestName:
        nextState = State.Timestamp;
        setTestName(text);
        break;
      case Message:
        nextState = State.Timestamp;
        if (didFail) {
          didFail = false;
          return true; // EARLY EXIT
        }
        myFailureMessage += text;
        break;
      case Error:
        setTestName(text);
        nextState = State.ErrorMessage;
        break;
      case ErrorMessage:
        myFailureMessage = text;
        nextState = State.Message; // wrong
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

  private void testFinished() {
    if (myCurrentTestName.isEmpty()) return;
    testStarted();
    myProcessor.fireOnTestFinished(new TestFinishedEvent(myCurrentTestName, myTestId, 0L));
    resetTestState();
  }

  private void testFailed() {
    if (myCurrentTestName.isEmpty()) return;
    testStarted();
    myProcessor.fireOnTestFailure(
      new TestFailedEvent(myCurrentTestName, myTestId, myFailureMessage, myStackTrace, myTestError, myFailureActualText,
                          myFailureExpectedText, null, 0L));
    resetTestState();
  }

  private void testStarted() {
    myProcessor.fireOnTestStarted(new TestStartedEvent(myCurrentTestName, ++myTestId, 0, null, null, null, true));
  }

  private void resetTestState() {
    myCurrentTestName = "";
    myFailureMessage = "";
    myStackTrace = "";
    myFailureActualText = "";
    myFailureExpectedText = "";
    myTestError = false;
  }
}
