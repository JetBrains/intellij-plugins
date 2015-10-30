package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.openapi.util.Key;
import junit.framework.TestCase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DartTestOutputParserTest extends TestCase {

  private static final String[][] TestOutput = {
    {"/usr/local/opt/dart/libexec/bin/dart --ignore-unrecognized-flags --checked --enable-vm-service:57087 --trace_service_pause_events file:///usr/local/opt/dart/libexec/bin/snapshots/pub.dart.snapshot run test:test -r expanded /Users/messick/src/dart_style-master/test/formatter_test.dart -n \"line endings\"\n","system"},
    {"Observatory listening on http://127.0.0.1:57087\n","stdout"},
    {"00:00 ","stdout"},
    {"+0","\u001B[32m"},
    {": line endings uses given line ending\n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {": line endings fails once\n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -1","\u001B[0;31m"},
    {": line endings fails once\n","stdout"},
    {"  boom\n","stdout"},
    {"  package:test                    fail\n","stdout"},
    {"  test/formatter_test.dart 107:7  main.<fn>.<fn>\n","stdout"},
    {"  \n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -1","\u001B[0;31m"},
    {": line endings fails twice\n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -2","\u001B[0;31m"},
    {": line endings fails twice\n","stdout"},
    {"  No top-level method 'fail' with matching arguments declared.\n","stdout"},
    {"  \n","stdout"},
    {"  NoSuchMethodError: incorrect number of arguments passed to method named 'fail'\n","stdout"},
    {"  Receiver: top-level\n","stdout"},
    {"  Tried calling: fail(...)\n","stdout"},
    {"  Found: fail(String)\n","stdout"},
    {"  dart:core                       NoSuchMethodError._throwNew\n","stdout"},
    {"  test/formatter_test.dart 110:7  main.<fn>.<fn>\n","stdout"},
    {"  \n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -2","\u001B[0;31m"},
    {": line endings fails thrice\n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": line endings fails thrice\n","stdout"},
    {"  Expected: 'alphabet\\n'\n","stdout"},
    {"    'soup'\n","stdout"},
    {"    Actual: 'alpha\\n'\n","stdout"},
    {"    'beta'\n","stdout"},
    {"     Which: is different.\n","stdout"},
    {"  Expected: alphabet\\nsoup\n","stdout"},
    {"    Actual: alpha\\nbeta\n","stdout"},
    {"                 ^\n","stdout"},
    {"   Differ at offset 5\n","stdout"},
    {"  \n","stdout"},
    {"  package:test                    expect\n","stdout"},
    {"  test/formatter_test.dart 113:7  main.<fn>.<fn>\n","stdout"},
    {"  \n","stdout"},
    {"00:00 ","stdout"},
    {"+1","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": line endings infers \\r\\n if the first newline uses that\n","stdout"},
    {"00:00 ","stdout"},
    {"+2","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": line endings infers \\n if the first newline uses that\n","stdout"},
    {"00:00 ","stdout"},
    {"+3","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": line endings defaults to \\n if there are no newlines\n","stdout"},
    {"00:00 ","stdout"},
    {"+4","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": line endings handles Windows line endings in multiline strings\n","stdout"},
    {"00:00 ","stdout"},
    {"+5","\u001B[32m"},
    {" -3","\u001B[0;31m"},
    {": ","stdout"},
    {"Some tests failed.","\u001B[31m"},
    {"\n","stdout"},
    {"\n","system"},
    {"Process finished with exit code 1\n","system"},
  };

  private static final String[] expectedSignals = {
    "attached",
    "start line endings uses given line ending",
    "pass line endings uses given line ending",
    "start line endings fails once",
    "fail line endings fails once",
    "start line endings fails twice",
    "fail line endings fails twice",
    "start line endings fails thrice",
    "fail line endings fails thrice",
    "start line endings infers \\r\\n if the first newline uses that",
    "pass line endings infers \\r\\n if the first newline uses that",
    "start line endings infers \\n if the first newline uses that",
    "pass line endings infers \\n if the first newline uses that",
    "start line endings defaults to \\n if there are no newlines",
    "pass line endings defaults to \\n if there are no newlines",
    "start line endings handles Windows line endings in multiline strings",
    "pass line endings handles Windows line endings in multiline strings",
  };

  public void testSample() throws Exception {
    TestSignalCollector collector = new TestSignalCollector();
    DartTestOutputParser parser = new DartTestOutputParser(collector);
    for (String[] event : TestOutput) {
      parser.parseNext(event[0], new Key(event[1]));
    }
    assertEquals(expectedSignals.length, collector.signals.size());
    int index = 0;
    for (String signal : collector.signals) {
      assertEquals(expectedSignals[index++], signal);
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
  }
}
