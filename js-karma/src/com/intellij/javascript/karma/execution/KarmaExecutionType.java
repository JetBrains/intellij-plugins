package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import org.jetbrains.annotations.NotNull;

public enum KarmaExecutionType {
  RUN, DEBUG, COVERAGE;

  private static final String COVERAGE_EXECUTOR_ID = "Coverage";

  static boolean isCoverageExecutor(@NotNull Executor executor) {
    return COVERAGE_EXECUTOR_ID.equals(executor.getId());
  }
}
