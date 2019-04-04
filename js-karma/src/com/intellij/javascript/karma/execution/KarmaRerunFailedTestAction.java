// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.AbstractTestProxy;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.javascript.testFramework.util.EscapeUtils;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class KarmaRerunFailedTestAction extends AbstractRerunFailedTestsAction {
  public KarmaRerunFailedTestAction(@NotNull SMTRunnerConsoleView consoleView,
                                    @NotNull KarmaConsoleProperties consoleProperties) {
    super(consoleView);
    init(consoleProperties);
    setModel(consoleView.getResultsViewer());
  }

  @Nullable
  @Override
  protected MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
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

  @NotNull
  private static List<List<String>> convertToTestFqns(List<AbstractTestProxy> tests) {
    List<List<String>> result = ContainerUtil.newArrayList();
    for (AbstractTestProxy test : tests) {
      List<String> fqn = convertToTestFqn(test);
      if (fqn != null) {
        result.add(fqn);
      }
    }
    return result;
  }

  @Nullable
  private static List<String> convertToTestFqn(@NotNull AbstractTestProxy test) {
    String url = test.getLocationUrl();
    if (test.isLeaf() && url != null) {
      String path = VirtualFileManager.extractPath(url);
      return EscapeUtils.split(path, '.');
    }
    return null;
  }
}
