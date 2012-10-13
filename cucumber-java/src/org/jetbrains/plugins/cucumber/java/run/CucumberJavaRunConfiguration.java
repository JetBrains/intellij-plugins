package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;

import java.io.File;

/**
 * User: Andrey.Vokin
 * Date: 8/6/12
 */

public class CucumberJavaRunConfiguration extends ApplicationConfiguration {
  public CucumberJavaRunConfiguration(String name, Project project, CucumberJavaRunConfigurationType applicationConfigurationType) {
    super(name, project, applicationConfigurationType);
  }

  protected CucumberJavaRunConfiguration(String name, Project project, ConfigurationFactory factory) {
    super(name, project, factory);
  }

  protected ModuleBasedConfiguration createInstance() {
    return new CucumberJavaRunConfiguration(getName(), getProject(), CucumberJavaRunConfigurationType.getInstance());
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final JavaCommandLineState state = new JavaApplicationCommandLineState(this, env) {
      protected JavaParameters createJavaParameters() throws ExecutionException {
        final JavaParameters params = new JavaParameters();
        final JavaRunConfigurationModule module = getConfigurationModule();

        final int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS;
        final String jreHome = CucumberJavaRunConfiguration.this.ALTERNATIVE_JRE_PATH_ENABLED ? ALTERNATIVE_JRE_PATH : null;
        JavaParametersUtil.configureModule(module, params, classPathType, jreHome);
        JavaParametersUtil.configureConfiguration(params, CucumberJavaRunConfiguration.this);

        String path = CucumberJavaRunConfiguration.class.getResource("/SMFormatter.jar").getPath();
        params.getClassPath().add(path);

        params.setMainClass(MAIN_CLASS_NAME);
        for(RunConfigurationExtension ext: Extensions.getExtensions(RunConfigurationExtension.EP_NAME)) {
          ext.updateJavaParameters(CucumberJavaRunConfiguration.this, params, getRunnerSettings());
        }

        return params;
      }

      @Nullable
      protected ConsoleView createConsole(@NotNull final Executor executor, ProcessHandler processHandler) throws ExecutionException {
        // console view
        final ConsoleView testRunnerConsole;

        final String testFrameworkName = "cucumber";
        final CucumberJavaRunConfiguration runConfiguration = CucumberJavaRunConfiguration.this;
        final SMTRunnerConsoleProperties consoleProperties = new SMTRunnerConsoleProperties(runConfiguration, testFrameworkName, executor);

        testRunnerConsole = SMTestRunnerConnectionUtil.createAndAttachConsole(testFrameworkName, processHandler, consoleProperties,
                                                                              getRunnerSettings(),
                                                                              getConfigurationSettings());

        return testRunnerConsole;
      }

      @Override
      public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        final ProcessHandler processHandler = startProcess();
        final ConsoleView console = createConsole(executor, processHandler);
        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler, executor));

      }
    };
    state.setConsoleBuilder(TextConsoleBuilderFactory.getInstance().createBuilder(getProject()));
    return state;
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    final String[] parameters = getProgramParameters().split(" ");
    if (parameters.length > 0) {
      final String fileToRun = parameters[0];
      if (!(new File(fileToRun)).exists()) {
        throw new RuntimeConfigurationException(CucumberBundle.message("cucumber.run.error.file.doesnt.exist"));
      }
    } else {
      throw new RuntimeConfigurationException(CucumberBundle.message("cucumber.run.error.specify.file"));
    }

    super.checkConfiguration();
  }
}
