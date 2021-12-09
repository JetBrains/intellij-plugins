/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.execution;

import com.intellij.execution.DefaultExecutionResult;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.CommandLineState;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.thoughtworks.gauge.GaugeConstants;
import com.thoughtworks.gauge.core.GaugeVersion;
import com.thoughtworks.gauge.execution.runner.GaugeConsoleProperties;
import com.thoughtworks.gauge.settings.GaugeSettingsService;
import com.thoughtworks.gauge.util.GaugeUtil;
import org.jetbrains.annotations.NotNull;

public final class GaugeCommandLineState extends CommandLineState {
  private static final Logger LOG = Logger.getInstance(GaugeCommandLineState.class);

  private final GeneralCommandLine commandLine;
  private final Project project;
  private final ExecutionEnvironment env;
  private final GaugeRunConfiguration config;

  public GaugeCommandLineState(GeneralCommandLine commandLine, Project project, ExecutionEnvironment env, GaugeRunConfiguration config) {
    super(env);
    this.env = env;
    this.commandLine = commandLine;
    this.project = project;
    this.config = config;
  }

  @NotNull
  @Override
  protected ProcessHandler startProcess() throws ExecutionException {
    return GaugeRunProcessHandler.runCommandLine(commandLine, GaugeDebugInfo.getInstance(commandLine, env), project);
  }

  @NotNull
  @Override
  public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
    addProjectClasspath();
    if (GaugeVersion.isGreaterOrEqual(GaugeRunConfiguration.TEST_RUNNER_SUPPORT_VERSION, false)
        && GaugeSettingsService.getSettings().useIntelliJTestRunner()) {
      ProcessHandler handler = startProcess();
      GaugeConsoleProperties properties = new GaugeConsoleProperties(config, "Gauge", executor, handler);
      ConsoleView console = SMTestRunnerConnectionUtil.createAndAttachConsole("Gauge", handler, properties);
      DefaultExecutionResult result = new DefaultExecutionResult(console, handler, createActions(console, handler));
      if (ActionManager.getInstance().getAction("RerunFailedTests") != null) {
        AbstractRerunFailedTestsAction action = properties.createRerunFailedTestsAction(console);
        action.setModelProvider(((SMTRunnerConsoleView)console)::getResultsViewer);
        result.setRestartActions(action);
      }
      return result;
    }
    return super.execute(executor, runner);
  }

  private void addProjectClasspath() {
    Module module = config.getModule();
    if (module != null) {
      String cp = GaugeUtil.classpathForModule(module);
      LOG.info(String.format("Setting `%s` to `%s`", GaugeConstants.GAUGE_CUSTOM_CLASSPATH, cp));
      commandLine.getEnvironment().put(GaugeConstants.GAUGE_CUSTOM_CLASSPATH, cp);
    }
  }
}
