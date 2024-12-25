// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KarmaRerunFailedTestAction extends AbstractRerunFailedTestsAction {
  public KarmaRerunFailedTestAction(@NotNull SMTRunnerConsoleView consoleView,
                                    @NotNull KarmaConsoleProperties consoleProperties) {
    super(consoleView);
    init(consoleProperties);
    setModel(consoleView.getResultsViewer());
  }

  @Override
  protected @Nullable MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
    KarmaRunConfiguration configuration = (KarmaRunConfiguration)myConsoleProperties.getConfiguration();
    KarmaRunProfileState state = new KarmaRunProfileState(configuration.getProject(), configuration, environment, configuration.getKarmaPackage());
    List<AbstractTestProxy> failedTests = getFailedTests(configuration.getProject());
    state.setFailedTestNames(convertToTestFqns(failedTests));
    return new MyRunProfile(configuration) {
      @Override
      public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) {
        return state;
      }
    };
  }

  private static @NotNull List<List<String>> convertToTestFqns(List<AbstractTestProxy> tests) {
    List<List<String>> result = new ArrayList<>();
    for (AbstractTestProxy test : tests) {
      List<String> fqn = convertToTestFqn(test);
      if (fqn != null) {
        result.add(fqn);
      }
    }
    return result;
  }

  private static @Nullable List<String> convertToTestFqn(@NotNull AbstractTestProxy test) {
    String url = test.getLocationUrl();
    if (test.isLeaf() && url != null) {
      String path = VirtualFileManager.extractPath(url);
      return EscapeUtils.split(path, '.');
    }
    return null;
  }
}
