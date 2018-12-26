// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.testframework.JavaTestLocator;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NullableComputable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PathUtil;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public class CucumberJavaRunConfiguration extends ApplicationConfiguration {
  private NullableComputable<String> glueInitializer = null;

  protected CucumberJavaRunConfiguration(String name, Project project, ConfigurationFactory factory) {
    super(name, project, factory);
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    SettingsEditorGroup<CucumberJavaRunConfiguration> group = new SettingsEditorGroup<>();
    group.addEditor(ExecutionBundle.message("run.configuration.configuration.tab.title"), new CucumberJavaApplicationConfigurable(getProject()));
    JavaRunConfigurationExtensionManager.getInstance().appendEditors(this, group);
    group.addEditor(ExecutionBundle.message("logs.tab.title"), new LogConfigurationPanel<>());
    return group;
  }

  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
    return new JavaApplicationCommandLineState<CucumberJavaRunConfiguration>(this, env) {
      private final Collection<Filter> myConsoleFilters = new ArrayList<>();

      @Override
      protected JavaParameters createJavaParameters() throws ExecutionException {
        final JavaParameters params = new JavaParameters();
        final JavaRunConfigurationModule module = getConfigurationModule();

        final int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS;
        final String jreHome = isAlternativeJrePathEnabled() ? getAlternativeJrePath() : null;
        JavaParametersUtil.configureModule(module, params, classPathType, jreHome);
        JavaParametersUtil.configureConfiguration(params, CucumberJavaRunConfiguration.this);

        String[] paths = getSMRunnerPaths();
        for (String path : paths) {
          params.getClassPath().add(path);
        }

        params.setMainClass(getMainClassName());
        for (RunConfigurationExtension ext : RunConfigurationExtension.EP_NAME.getExtensionList()) {
          ext.updateJavaParameters(CucumberJavaRunConfiguration.this, params, getRunnerSettings());
        }

        final String glueValue = getGlue();
        if (glueValue != null && !StringUtil.isEmpty(glueValue)) {
          final String[] glues = glueValue.split(" ");
          for (String glue : glues) {
            if (!StringUtil.isEmpty(glue)) {
              params.getProgramParametersList().addParametersString(" --glue " + glue);
            }
          }
        }

        String filePath = getFilePath();
        File f = new File(filePath);
        if (!f.isDirectory()) {
          f = f.getParentFile();
        }
        params.getVMParametersList().addParametersString("-Dorg.jetbrains.run.directory=\"" + f.getAbsolutePath() + "\"");

        params.getProgramParametersList().addParametersString("\"" + filePath + "\"");
        params.setShortenCommandLine(getShortenCommandLine(), getProject());
        return params;
      }

      @NotNull
      private ConsoleView createConsole(@NotNull final Executor executor, ProcessHandler processHandler) throws ExecutionException {
        // console view
        final String testFrameworkName = "cucumber";
        final CucumberJavaRunConfiguration runConfiguration = CucumberJavaRunConfiguration.this;
        final SMTRunnerConsoleProperties consoleProperties = new SMTRunnerConsoleProperties(runConfiguration, testFrameworkName, executor) {
          @NotNull
          @Override
          public SMTestLocator getTestLocator() {
            return JavaTestLocator.INSTANCE;
          }
        };
        return SMTestRunnerConnectionUtil.createAndAttachConsole(testFrameworkName, processHandler, consoleProperties);
      }

      @NotNull
      @Override
      public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner runner) throws ExecutionException {
        final ProcessHandler processHandler = startProcess();
        final ConsoleView console = createConsole(executor, processHandler);
        myConsoleFilters.forEach((filter) -> console.addMessageFilter(filter));
        return new DefaultExecutionResult(console, processHandler, createActions(console, processHandler, executor));
      }

      @Override
      public void addConsoleFilters(Filter... filters) {
        myConsoleFilters.addAll(Arrays.asList(filters));
      }
    };
  }

  private String[] getSMRunnerPaths() {
    List<String> result = new ArrayList<>();

    String cucumberJvmFormatterClassPath = PathUtil.getJarPathForClass(CucumberJvmSMFormatter.class);
    result.add(cucumberJvmFormatterClassPath);
    if (VersionComparatorUtil.compare(getCucumberCoreVersion(), "4") >= 0) {
      if (cucumberJvmFormatterClassPath.endsWith(".jar")) {
        result.add(cucumberJvmFormatterClassPath.replace(".jar", "4.jar"));
      } else {
        // Running IDEA from sources
        result.add(cucumberJvmFormatterClassPath + "4");
      }
    }
    if (VersionComparatorUtil.compare(getCucumberCoreVersion(), "3") >= 0) {
      if (cucumberJvmFormatterClassPath.endsWith(".jar")) {
        result.add(cucumberJvmFormatterClassPath.replace(".jar", "3.jar"));
      } else {
        // Running IDEA from sources
        result.add(cucumberJvmFormatterClassPath + "3");
      }
    }

    return ArrayUtil.toStringArray(result);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    String filePath = getFilePath();
    if (filePath == null) {
      throw new RuntimeConfigurationException(CucumberBundle.message("cucumber.run.error.specify.file"));
    } else if (!(new File(filePath)).exists()) {
      throw new RuntimeConfigurationException(CucumberBundle.message("cucumber.run.error.file.doesnt.exist"));
    }
    else if (StringUtil.isEmpty(getGlue())) {
      throw new RuntimeConfigurationException(CucumberJavaBundle.message("cucumber.java.run.configuration.glue.must.not.be.empty"));
    }

    String programParameters = getProgramParameters();
    if (programParameters != null && programParameters.contains("--glue")) {
      throw new RuntimeConfigurationException(CucumberJavaBundle.message("cucumber.java.run.configuration.glue.in.program.parameters"));
    }

    super.checkConfiguration();
  }

  @NotNull
  @Override
  protected CucumberJavaConfigurationOptions getOptions() {
    return (CucumberJavaConfigurationOptions)super.getOptions();
  }

  @Nullable
  public String getGlue() {
    if (glueInitializer != null) {
      setGlue(glueInitializer.compute());
    }

    return getOptions().getGlue();
  }

  public void setGlue(String value) {
    getOptions().setGlue(value);
    glueInitializer = null;
  }

  public void setGlue(NullableComputable<String> value) {
    glueInitializer = value;
  }

  public String getFilePath() {
    return getOptions().getFilePath();
  }

  public void setFilePath(String filePath) {
    getOptions().setFilePath(filePath);
  }

  public String getNameFilter() {
    return getOptions().getNameFilter();
  }

  public void setNameFilter(String nameFilter) {
    getOptions().setNameFilter(nameFilter);
  }

  public String getCucumberCoreVersion() {
    return getOptions().getCucumberCoreVersion();
  }

  public void setCucumberCoreVersion(String cucumberCoreVersion) {
    getOptions().setCucumberCoreVersion(cucumberCoreVersion);
  }

  @Nullable
  @Override
  public String suggestedName() {
    return getOptions().getSuggestedName();
  }

  public void setSuggestedName(String suggestedName) {
    getOptions().setSuggestedName(suggestedName);
  }

  @Override
  public String getActionName() {
    return getName();
  }
}
