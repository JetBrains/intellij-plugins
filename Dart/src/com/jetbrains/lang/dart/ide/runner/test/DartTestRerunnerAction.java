package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.actions.AbstractRerunFailedTestsAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.ui.ComponentContainer;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunnerParameters;
import org.jetbrains.annotations.NotNull;

class DartTestRerunnerAction extends AbstractRerunFailedTestsAction {

  DartTestRerunnerAction(@NotNull ComponentContainer componentContainer) {
    super(componentContainer);
  }

  @Override
  protected MyRunProfile getRunProfile(@NotNull ExecutionEnvironment environment) {
    final RunConfigurationBase configuration = (RunConfigurationBase)myConsoleProperties.getConfiguration();
    final DartTestRerunner runner = new DartTestRerunner(environment, getFailedTests(configuration.getProject()));
    return new RerunProfile(configuration, runner);
  }

  private static class RerunProfile extends MyRunProfile implements DartRunConfiguration {
    private final DartTestRerunner runner;

    private RerunProfile(RunConfigurationBase configuration, DartTestRerunner runner) {
      super(configuration);
      this.runner = runner;
    }

    @NotNull
    @Override
    public DartCommandLineRunnerParameters getRunnerParameters() {
      return ((DartRunConfiguration)runner.getEnvironment().getRunProfile()).getRunnerParameters();
    }

    @Override
    @NotNull
    public Module[] getModules() {
      return runner.getModulesToCompile();
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
      return runner;
    }
  }
}
