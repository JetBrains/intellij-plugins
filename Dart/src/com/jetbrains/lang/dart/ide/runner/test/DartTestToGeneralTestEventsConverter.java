package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.process.ProcessOutputTypes;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor;
import com.intellij.execution.testframework.sm.runner.OutputLineSplitter;
import com.intellij.execution.testframework.sm.runner.OutputToGeneralTestEventsConverter;
import com.intellij.execution.testframework.sm.runner.events.*;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static com.intellij.execution.testframework.sm.runner.GeneralTestEventsProcessor.getTFrameworkPrefix;

/**
 * Parse package:test output and convert it to test events.<p>
 * Unfortunately, the interface requires an instance of OutputToGeneralTestEventsConverter, rather than
 * an instance of some interface, so we have to override and ignore a lot of the superclass implementation.
 * These instance variables must be redefined (and all methods referring to them overridden): <code>mySplitter,
 * myTestFrameworkName, myPendingLineBreakFlag.</code> Also, <code>processServiceMessages()</code> cannot be
 * called even though it is protected. Fortunately, <code>myProcessor</code> can be re-used.
 */
class DartTestToGeneralTestEventsConverter extends OutputToGeneralTestEventsConverter {
  private static final Logger LOG = Logger.getInstance(DartTestToGeneralTestEventsConverter.class.getName());

  private final OutputLineSplitter mySplitter;
  private final String myTestFrameworkName;
  private final DartTestOutputParser myDartTestParser;
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
    myDartTestParser = new DartTestOutputParser(this);
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
    if (!myDartTestParser.parseNext(text, outputType)) {
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

  void fireOnTestStarted(@NotNull TestStartedEvent testStartedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestStarted(testStartedEvent);
    }
  }

  void fireOnTestFailure(@NotNull TestFailedEvent testFailedEvent) {
    assertNotNull(testFailedEvent.getLocalizedFailureMessage());

    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestFailure(testFailedEvent);
    }
  }

  void fireOnTestIgnored(@NotNull TestIgnoredEvent testIgnoredEvent) {

    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestIgnored(testIgnoredEvent);
    }
  }

  void fireOnTestFinished(@NotNull TestFinishedEvent testFinishedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestFinished(testFinishedEvent);
    }
  }

  void fireOnCustomProgressTestsCategory(final String categoryName,
                                                 int testsCount) {
    assertNotNull(categoryName);

    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      final boolean disableCustomMode = StringUtil.isEmpty(categoryName);
      processor.onCustomProgressTestsCategory(disableCustomMode ? null : categoryName,
                                              disableCustomMode ? 0 : testsCount);
    }
  }

  void fireOnCustomProgressTestStarted() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onCustomProgressTestStarted();
    }
  }

  void fireOnCustomProgressTestFinished() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onCustomProgressTestFinished();
    }
  }

  void fireOnCustomProgressTestFailed() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onCustomProgressTestFailed();
    }
  }

  void fireOnTestFrameworkAttached() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestsReporterAttached();
    }
  }

  void fireOnSuiteTreeNodeAdded(String testName, String locationHint) {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onSuiteTreeNodeAdded(testName, locationHint);
    }
  }


  void fireRootPresentationAdded(String rootName, @Nullable String comment, String rootLocation) {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onRootPresentationAdded(rootName, comment, rootLocation);
    }
  }

  void fireOnSuiteTreeStarted(String suiteName, String locationHint) {

    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onSuiteTreeStarted(suiteName, locationHint);
    }
  }

  void fireOnSuiteTreeEnded(String suiteName) {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onSuiteTreeEnded(suiteName);
    }
  }

  void fireOnBuildTreeEnded() {
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onBuildTreeEnded();
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

  void fireOnTestsCountInSuite(final int count) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onTestsCountInSuite(count);
    }
  }

  void fireOnSuiteStarted(@NotNull TestSuiteStartedEvent suiteStartedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onSuiteStarted(suiteStartedEvent);
    }
  }

  void fireOnSuiteFinished(@NotNull TestSuiteFinishedEvent suiteFinishedEvent) {
    // local variable is used to prevent concurrent modification
    final GeneralTestEventsProcessor processor = getProcessor();
    if (processor != null) {
      processor.onSuiteFinished(suiteFinishedEvent);
    }
  }
}
