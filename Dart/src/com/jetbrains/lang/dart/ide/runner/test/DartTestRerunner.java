package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.states.TestStateInfo.Magnitude;
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
    return Module.EMPTY_ARRAY;
  }

  private String computeTestNameRegexp() {
    StringBuilder buf = new StringBuilder();

    for (AbstractTestProxy test : failedTests) {
      assert test instanceof SMTestProxy : test.getClass().getName();

      final Magnitude magnitude = ((SMTestProxy)test).getMagnitudeInfo();
      if (test.getParent() != null &&
          !((SMTestProxy)test).isSuite() &&
          test.isLeaf() &&
          (magnitude == Magnitude.FAILED_INDEX || magnitude == Magnitude.ERROR_INDEX || magnitude == Magnitude.TERMINATED_INDEX)) {
        if (buf.length() > 0) buf.append('|');
        buf.append(buildFullTestName(test));
      }
    }
    return buf.toString();
  }

  private static String buildFullTestName(@NotNull AbstractTestProxy test) {
    StringBuffer fullName = new StringBuffer();
    buildFullTestName(test.getParent(), fullName);
    fullName.append(test.getName());
    return StringUtil.escapeToRegexp(fullName.toString());
  }

  private static void buildFullTestName(AbstractTestProxy test, StringBuffer fullName) {
    if (test == null) return;
    buildFullTestName(test.getParent(), fullName);
    String url = test.getLocationUrl();
    if (url == null || url.endsWith(",[]")) return;
    String name = test.getName();
    if (name == null || name.equals("[root]")) return; // Previous return should prevent reaching here.
    fullName.append(name);
    fullName.append(' ');
  }
}
