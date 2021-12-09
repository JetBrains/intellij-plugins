// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.run;

import com.intellij.diagnostic.logging.LogConfigurationPanel;
import com.intellij.execution.*;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.*;
import com.intellij.execution.filters.Filter;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ProgramRunner;
import com.intellij.execution.target.LanguageRuntimeType;
import com.intellij.execution.testframework.JavaTestLocator;
import com.intellij.execution.testframework.sm.SMTestRunnerConnectionUtil;
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.execution.testframework.ui.BaseTestsOutputConsoleView;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.junit4.ExpectedPatterns;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.options.SettingsEditorGroup;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.ArrayUtilRt;
import com.intellij.util.PathUtil;
import com.intellij.util.text.VersionComparatorUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.serialization.PathMacroUtil;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaBundle;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;

public class CucumberJavaRunConfiguration extends ApplicationConfiguration {
  private volatile CucumberGlueProvider myCucumberGlueProvider = null;
  private final static Logger LOG = Logger.getInstance(CucumberJavaRunConfiguration.class);

  protected CucumberJavaRunConfiguration(String name, Project project, ConfigurationFactory factory) {
    super(name, project, factory);
    setWorkingDirectory(PathMacroUtil.MODULE_WORKING_DIR);
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
    return new JavaApplicationCommandLineState<>(this, env) {
      private final Collection<Filter> myConsoleFilters = new ArrayList<>();

      @Override
      protected JavaParameters createJavaParameters() throws ExecutionException {
        final JavaParameters params = new JavaParameters();
        final JavaRunConfigurationModule module = getConfigurationModule();

        final int classPathType = JavaParameters.JDK_AND_CLASSES_AND_TESTS;
        final String jreHome = getTargetEnvironmentRequest() == null && isAlternativeJrePathEnabled() ? getAlternativeJrePath() : null;
        ReadAction.run(() -> {
          JavaParametersUtil.configureModule(module, params, classPathType, jreHome);
          JavaParametersUtil.configureConfiguration(params, CucumberJavaRunConfiguration.this);
        });

        String[] paths = getSMRunnerPaths();
        for (String path : paths) {
          params.getClassPath().add(path);
        }

        params.setMainClass(getMainClassName());
        ReadAction.run(() -> {
          JavaRunConfigurationExtensionManager.getInstance()
            .updateJavaParameters(CucumberJavaRunConfiguration.this, params, getRunnerSettings(), executor);
        });

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
        if (f.exists()) {
          if (!f.isDirectory()) {
            f = f.getParentFile();
          }
          params.getVMParametersList().addParametersString("-Dorg.jetbrains.run.directory=\"" + f.getAbsolutePath() + "\"");
        }

        params.getProgramParametersList().addParametersString("\"" + filePath + "\"");
        params.setShortenCommandLine(getShortenCommandLine(), getProject());
        return params;
      }

      @NotNull
      private ConsoleView createConsole(@NotNull final Executor executor, ProcessHandler processHandler) {
        @NonNls String testFrameworkName = "cucumber";
        final CucumberJavaRunConfiguration runConfiguration = CucumberJavaRunConfiguration.this;
        final SMTRunnerConsoleProperties consoleProperties = new SMTRunnerConsoleProperties(runConfiguration, testFrameworkName, executor) {
          @NotNull
          @Override
          public SMTestLocator getTestLocator() {
            return JavaTestLocator.INSTANCE;
          }
        };
        BaseTestsOutputConsoleView console = UIUtil.invokeAndWaitIfNeeded(() -> {
          return SMTestRunnerConnectionUtil.createConsole(testFrameworkName, consoleProperties);
        });
        console.attachToProcess(processHandler);
        return console;
      }

      @NotNull
      @Override
      public ExecutionResult execute(@NotNull Executor executor, @NotNull ProgramRunner<?> runner) throws ExecutionException {
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

    String rtClassPath = PathUtil.getJarPathForClass(ExpectedPatterns.class);
    result.add(rtClassPath);

    @NonNls String cucumberJvmFormatterClassPath = PathUtil.getJarPathForClass(CucumberJvmSMFormatter.class);
    result.add(cucumberJvmFormatterClassPath);

    // Attach SM formatter's folder/jar for Cucumber v3/v4
    String cucumberCoreVersion = getCucumberCoreVersion();
    LOG.info("detected cucumber-java version: " + cucumberCoreVersion);
    for (int i = 5; i >= 3; i--) {
      if (VersionComparatorUtil.compare(cucumberCoreVersion, String.valueOf(i)) >= 0) {
        if (cucumberJvmFormatterClassPath.endsWith(".jar")) {
          result.add(cucumberJvmFormatterClassPath.replace(".jar", i + ".jar"));
        } else {
          // Running IDEA from sources
          result.add(cucumberJvmFormatterClassPath + i);
        }
      }
    }

    return ArrayUtilRt.toStringArray(result);
  }

  @Override
  public void checkConfiguration() throws RuntimeConfigurationException {
    String filePath = getFilePath();
    if (filePath == null) {
      throw new RuntimeConfigurationException(CucumberBundle.message("cucumber.run.error.specify.file"));
    }

    @NonNls String programParameters = getProgramParameters();
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
    if (myCucumberGlueProvider != null) {
      //noinspection SynchronizeOnThis
      synchronized (this) {
        if (myCucumberGlueProvider != null) {
          Set<String> glues = new HashSet<>();
          if (ApplicationManager.getApplication().isDispatchThread()) {
            Task.Modal task = new Task.Modal(getProject(), CucumberJavaBundle.message("cucumber.java.glue.calculation.title"), true) {
              @Override
              public void run(@NotNull ProgressIndicator indicator) {
                indicator.setText(CucumberJavaBundle.message("cucumber.java.glue.calculation.glues.message", "-"));
                indicator.setText2(CucumberJavaBundle.message("cucumber.java.glue.calculation.glues.template.message"));
                Consumer<String> consumer = glue -> {
                  if (CucumberJavaUtil.addGlue(glue, glues)) {
                    String gluePresentation = null;
                    if (glues.size() < 15) {
                      gluePresentation = StringUtil.join(glues, " ");
                      if (gluePresentation.length() > 30) {
                        gluePresentation = null;
                      }
                    }
                    if (gluePresentation == null) {
                      gluePresentation = String.valueOf(glues.size());
                    }

                    String message = CucumberJavaBundle.message("cucumber.java.glue.calculation.glues.message", gluePresentation);
                    indicator.setText(message);
                  }
                };

                ApplicationManager.getApplication().runReadAction(() -> myCucumberGlueProvider.calculateGlue(consumer));
              }
            };
            task.setCancelText(CucumberJavaBundle.message("cucumber.java.glue.calculation.stop.title"));

            ProgressManager.getInstance().run(task);
          }
          else {
            ApplicationManager.getApplication().runReadAction(() -> myCucumberGlueProvider.calculateGlue(glue -> CucumberJavaUtil.addGlue(glue, glues)));
          }
          getOptions().setGlue(StringUtil.join(glues, " "));
          myCucumberGlueProvider = null;
        }
      }
    }

    return getOptions().getGlue();
  }

  @Nullable
  public String getPrecalculatedGlue() {
    return getOptions().getGlue();
  }

  public synchronized void setGlue(String value) {
    getOptions().setGlue(value);
    myCucumberGlueProvider = null;
  }

  public synchronized void setGlueProvider(@Nullable CucumberGlueProvider cucumberGlueProvider) {
    myCucumberGlueProvider = cucumberGlueProvider;
  }

  public @NlsSafe String getFilePath() {
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

  @Nullable
  @Override
  public LanguageRuntimeType<?> getDefaultLanguageRuntimeType() {
    return null;
  }

  @Nullable
  @Override
  public String getDefaultTargetName() {
    return null;
  }
}
