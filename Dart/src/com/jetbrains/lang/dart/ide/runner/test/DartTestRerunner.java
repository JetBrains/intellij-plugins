package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DartTestRerunner implements RunProfileState {
  private final ExecutionEnvironment environment;
  private final List<AbstractTestProxy> failedTests;

  DartTestRerunner(@NotNull ExecutionEnvironment env, @NotNull List<AbstractTestProxy> tests) {
    environment = env;
    failedTests = tests;
  }

  ExecutionEnvironment getEnvironment() {
    return environment;
  }

  @Nullable
  @Override
  public ExecutionResult execute(Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
    DartTestRunningState state = new DartTestRunningState(environment);
    DartTestRunnerParameters params = state.getParameters();
    params.setScope(DartTestRunnerParameters.Scope.MULTIPLE_NAMES);
    params.setTestName(computeTestNameRegexp());
    return state.execute(executor, runner);
  }

  @NotNull
  Module[] getModulesToCompile() {
    return new Module[0];
  }

  private String computeTestNameRegexp() {
    StringBuilder buf = new StringBuilder();
    boolean needsSeparator = false;
    for (AbstractTestProxy test : failedTests) {
      if (test.isPassed()) {
        continue;
      }
      if (test.isLeaf()) {
        if (needsSeparator) {
          buf.append('|');
        }
        else {
          needsSeparator = true;
        }
        buf.append(StringUtil.escapeToRegexp(test.getName()));
      }
    }
    return buf.toString();
  }
}
