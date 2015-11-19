package com.jetbrains.lang.dart.ide.runner.test;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface DartTestSignaller {

  void signalTestFrameworkAttached();

  void signalTestStarted(@NotNull String testName,
                         int testId,
                         int parentId,
                         @Nullable String locationUrl,
                         @Nullable String nodeType,
                         @Nullable String nodeArgs,
                         boolean running);

  void signalTestFailure(@NotNull String testName,
                         int testId,
                         @NotNull String failureMessage,
                         @Nullable String stackTrace,
                         boolean testError,
                         @Nullable String failureActualText,
                         @Nullable String failureExpectedText,
                         @Nullable String expectedTextFilePath,
                         long durationMillis);

  void signalTestFinished(@NotNull String testName, int testId, long durationMillis);

  void signalTestSkipped(@NotNull String testName, @NotNull String reason, @Nullable String stackTrace);

  void signalTestMessage(@NotNull String testName, @NotNull String message);
}
