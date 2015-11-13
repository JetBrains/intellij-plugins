package com.jetbrains.lang.dart.ide.runner.test;

import com.google.gson.JsonSyntaxException;
import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputLineSplitter;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor.getTFrameworkPrefix;

/**
 * Parse package:test output and convert it to test events.<p>
 * Unfortunately, the interface requires an instance of OutputToGeneralTestEventsConverter, rather than
 * an instance of some interface, so we have to override and ignore a lot of the superclass implementation.
 * These instance variables must be redefined (and all methods referring to them overridden): <code>mySplitter,
 * myTestFrameworkName, myPendingLineBreakFlag.</code> Also, <code>processServiceMessages()</code> cannot be
 * called even though it is protected. Fortunately, <code>myProcessor</code> can be re-used.
 */
class DartTestToGeneralTestEventsConverter extends OutputToGeneralTestEventsConverter implements DartTestSignaller {
  private static final Logger LOG = Logger.getInstance(DartTestToGeneralTestEventsConverter.class.getName());

  private final OutputLineSplitter mySplitter;
  private final String myTestFrameworkName;
  private final DartTestJsonReader myDartTestParser;
  private boolean myPendingLineBreakFlag;

  public DartTestToGeneralTestEventsConverter(@NotNull final String testFrameworkName,
                                              @NotNull final TestConsoleProperties consoleProperties) {
    super(testFrameworkName, consoleProperties);
    mySplitter = new OutputLineSplitter(consoleProperties.isEditable()) {
      @Override
      protected void onLineAvailable(@NotNull String text, @NotNull Key outputType, boolean tcLikeFakeOutput) {
        processConsistentText(text, outputType, tcLikeFakeOutput);
      }
    };
    myTestFrameworkName = testFrameworkName;
    myDartTestParser = new DartTestJsonReader(this);
  }

  public void process(final String text, final Key outputType) {
    mySplitter.process(text, outputType);
  }

  /**
   * Flushes the rest of stdout text buffer after output has been stopped.
   */
  public void flushBufferBeforeTerminating() {
    mySplitter.flush();
    if (myPendingLineBreakFlag) {
      fireOnUncapturedLineBreak();
    }
  }

  private void assertNotNull(final String s) {
    if (s == null) {
      LOG.error(getTFrameworkPrefix(myTestFrameworkName) + " @NotNull value is expected.");
    }
  }

  private void fireOnUncapturedLineBreak() {
    fireOnUncapturedOutput("\n", ProcessOutputTypes.STDOUT);
  }

  private void processConsistentText(final String text, final Key outputType, boolean tcLikeFakeOutput) {
    boolean result;
    try {
      result = myDartTestParser.process(text, outputType);
    }
    catch (JsonSyntaxException ex) {
      result = false;
    }
    if (!result) {
      if (myPendingLineBreakFlag) {
        // output type for line break isn't important
        // we may use any, e.g. current one
        fireOnUncapturedLineBreak();
        myPendingLineBreakFlag = false;
      }
      // Filters \n
      String outputToProcess = text;
      if (tcLikeFakeOutput && text.endsWith("\n")) {
        // ServiceMessages protocol requires that every message
        // should start with new line, so such behaviour may led to generating
        // some number of useless \n.
        //
        // IDEA process handler flush output by size or line break
        // So:
        //  1. "a\n\nb\n" -> ["a\n", "\n", "b\n"]
        //  2. "a\n##teamcity[..]\n" -> ["a\n", "#teamcity[..]\n"]
        // We need distinguish 1) and 2) cases, in 2) first linebreak is redundant and must be ignored
        // in 2) linebreak must be considered as output
        // output will be in TestOutput message
        // Lets set myPendingLineBreakFlag if we meet "\n" and then ignore it or apply depending on
        // next output chunk
        myPendingLineBreakFlag = true;
        outputToProcess = outputToProcess.substring(0, outputToProcess.length() - 1);
      }
      //fire current output
      fireOnUncapturedOutput(outputToProcess, outputType);
    }
    else {
      myPendingLineBreakFlag = false;
    }
  }

  private void fireOnTestStarted(@NotNull TestStartedEvent testStartedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestStarted(testStartedEvent);
    }
  }

  private void fireOnTestFailure(@NotNull TestFailedEvent testFailedEvent) {
    assertNotNull(testFailedEvent.getLocalizedFailureMessage());
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestFailure(testFailedEvent);
    }
  }

  private void fireOnTestFinished(@NotNull TestFinishedEvent testFinishedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestFinished(testFinishedEvent);
    }
  }

  private void fireOnTestFrameworkAttached() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestsReporterAttached();
    }
  }

  void fireOnTestIgnored(@NotNull TestIgnoredEvent testIgnoredEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestIgnored(testIgnoredEvent);
    }
  }

  void fireOnTestOutput(@NotNull TestOutputEvent testOutputEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestOutput(testOutputEvent);
    }
  }

  void fireOnUncapturedOutput(final String text, final Key outputType) {
    assertNotNull(text);
    if (StringUtil.isEmpty(text)) {
      return;
    }
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onUncapturedOutput(text, outputType);
    }
  }

  public void signalTestFrameworkAttached() {
    fireOnTestFrameworkAttached();
  }

  public void signalTestStarted(@NotNull String testName,
                                int testId,
                                int parentId,
                                @Nullable String locationUrl,
                                @Nullable String nodeType,
                                @Nullable String nodeArgs,
                                boolean running) {
    fireOnTestStarted(new TestStartedEvent(testName, testId, parentId, locationUrl, nodeType, nodeArgs, running));
  }

  public void signalTestFailure(@NotNull String testName,
                                int testId,
                                @NotNull String failureMessage,
                                @Nullable String stackTrace,
                                boolean testError,
                                @Nullable String failureActualText,
                                @Nullable String failureExpectedText,
                                @Nullable String expectedTextFilePath,
                                long durationMillis) {
    fireOnTestFailure(new TestFailedEvent(testName, testId, failureMessage, stackTrace, testError, failureActualText, failureExpectedText,
                                          expectedTextFilePath, durationMillis));
  }

  public void signalTestFinished(@NotNull String testName, int testId, long durationMillis) {
    fireOnTestFinished(new TestFinishedEvent(testName, testId, durationMillis));
  }

  public void signalTestSkipped(@NotNull String testName, @NotNull String reason, @Nullable String stackTrace) {
    fireOnTestIgnored(new TestIgnoredEvent(testName, reason, stackTrace));
  }

  public void signalTestMessage(@NotNull String testName, @NotNull String message) {
    fireOnTestOutput(new TestOutputEvent(testName, message, false));
  }
}
