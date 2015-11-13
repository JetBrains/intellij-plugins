package com.jetbrains.lang.dart.ide.runner.test;

import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.util.Key;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartTestJsonReaderTest extends TestCase {

  // Do not reformat this list.
  private static final String[] TestOutput = {
    // @formatter:off
    "/usr/local/opt/dart/libexec/bin/dart --ignore-unrecognized-flags --checked --enable-vm-service:60187 --trace_service_pause_events file:///usr/local/opt/dart/libexec/bin/snapshots/pub.dart.snapshot global run test:test -r json /Users/messick/src/dart_style-master/test/formatter_test.dart -n \"line endings\"\n",
    "Observatory listening on http://127.0.0.1:60187\n",
    "{\"type\":\"start\",\"name\":\"line endings uses given line ending\"}\n",
    "{\"type\":\"pass\",\"name\":\"line endings uses given line ending\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings fails once\"}\n",
    "{\"type\":\"fail\",\"name\":\"line endings fails once\",\"failMessage\":\"boom\",\"stackTrace\":\"package:test                    fail\\ntest/formatter_test.dart 107:7  main.<fn>.<fn>\\n\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings fails twice\"}\n",
    "{\"type\":\"error\",\"name\":\"line endings fails twice\",\"errorMessage\":\"No top-level method 'fail' with matching arguments declared.\\n\\nNoSuchMethodError: incorrect number of arguments passed to method named 'fail'\\nReceiver: top-level\\nTried calling: fail(...)\\nFound: fail(String)\",\"stackTrace\":\"dart:core                       NoSuchMethodError._throwNew\\ntest/formatter_test.dart 110:7  main.<fn>.<fn>\\n\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings fails thrice\"}\n",
    "{\"type\":\"fail\",\"name\":\"line endings fails thrice\",\"failMessage\":\"Expected: 'alphabet\\\\n'\\n  'soup'\\n  Actual: 'alpha\\\\n'\\n  'beta'\\n   Which: is different.\\nExpected: alphabet\\\\nsoup\\n  Actual: alpha\\\\nbeta\\n               ^\\n Differ at offset 5\\n\",\"stackTrace\":\"package:test                    expect\\ntest/formatter_test.dart 113:7  main.<fn>.<fn>\\n\"}\n",
    "{\"type\":\"skip\",\"name\":\"skip this test\",\"reason\":\"arbitrary\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings infers \\\\r\\\\n if the first newline uses that\"}\n",
    "{\"type\":\"pass\",\"name\":\"line endings infers \\\\r\\\\n if the first newline uses that\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings infers \\\\n if the first newline uses that\"}\n",
    "{\"type\":\"pass\",\"name\":\"line endings infers \\\\n if the first newline uses that\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings defaults to \\\\n if there are no newlines\"}\n",
    "{\"type\":\"pass\",\"name\":\"line endings defaults to \\\\n if there are no newlines\"}\n",
    "{\"type\":\"start\",\"name\":\"line endings handles Windows line endings in multiline strings\"}\n",
    "{\"type\":\"print\",\"name\":\"line endings handles Windows line endings in multiline strings\",\"message\":\"a message\"}\n",
    "{\"type\":\"pass\",\"name\":\"line endings handles Windows line endings in multiline strings\"}\n",
    "{\"type\":\"exit\",\"name\":\"terminated\"}\n",
    "\n",
    "Process finished with exit code 1\n",
    // @formatter:on
  };

  // Do not reformat this list.
  private static final String[] ExpectedSignals = {
    // @formatter:off
    "attached",
    "start line endings uses given line ending",
    "pass line endings uses given line ending",
    "start line endings fails once",
    "fail line endings fails once",
    "start line endings fails twice",
    "fail line endings fails twice",
    "start line endings fails thrice",
    "fail line endings fails thrice",
    "skip skip this test arbitrary",
    "start line endings infers \\r\\n if the first newline uses that",
    "pass line endings infers \\r\\n if the first newline uses that",
    "start line endings infers \\n if the first newline uses that",
    "pass line endings infers \\n if the first newline uses that",
    "start line endings defaults to \\n if there are no newlines",
    "pass line endings defaults to \\n if there are no newlines",
    "start line endings handles Windows line endings in multiline strings",
    "print line endings handles Windows line endings in multiline strings a message",
    "pass line endings handles Windows line endings in multiline strings",
    // @formatter:on
  };

  public void testSample() throws Exception {
    TestSignalCollector collector = new TestSignalCollector();
    DartTestJsonReader parser = new DartTestJsonReader(collector);
    Key key = new Key("stdout");
    for (String event : TestOutput) {
      try {
        parser.process(event, key);
      }
      catch (JsonSyntaxException ex) {
        // ignored
      }
    }
    assertEquals(ExpectedSignals.length, collector.signals.size());
    int index = 0;
    for (String signal : collector.signals) {
      assertEquals(ExpectedSignals[index++], signal);
    }
  }

  private static class TestSignalCollector implements DartTestSignaller {

    List<String> signals = new ArrayList<String>();

    @Override
    public void signalTestFrameworkAttached() {
      signals.add("attached");
    }

    @Override
    public void signalTestStarted(@NotNull String testName,
                                  int testId,
                                  int parentId,
                                  @Nullable String locationUrl,
                                  @Nullable String nodeType,
                                  @Nullable String nodeArgs,
                                  boolean running) {
      signals.add("start " + testName);
    }

    @Override
    public void signalTestFailure(@NotNull String testName,
                                  int testId,
                                  @NotNull String failureMessage,
                                  @Nullable String stackTrace,
                                  boolean testError,
                                  @Nullable String failureActualText,
                                  @Nullable String failureExpectedText,
                                  @Nullable String expectedTextFilePath,
                                  long durationMillis) {
      signals.add("fail " + testName);
    }

    @Override
    public void signalTestFinished(@NotNull String testName, int testId, long durationMillis) {
      signals.add("pass " + testName);
    }

    @Override
    public void signalTestSkipped(@NotNull String testName, @NotNull String reason, @Nullable String stackTrace) {
      signals.add("skip " + testName + " " + reason);
    }

    @Override
    public void signalTestMessage(@NotNull String testName, @NotNull String message) {
      signals.add("print " + testName + " " + message);
    }
  }
}
