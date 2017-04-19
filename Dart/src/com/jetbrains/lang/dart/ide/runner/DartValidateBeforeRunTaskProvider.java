package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.BeforeRunTask;
import com.intellij.execution.BeforeRunTaskProvider;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.text.StringUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.analyzer.DartServerData;
import com.jetbrains.lang.dart.ide.errorTreeView.DartProblemsView;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.stream.Collectors;

public class DartValidateBeforeRunTaskProvider extends BeforeRunTaskProvider<DartValidateBeforeRunTaskProvider.DartValidateBeforeRunTask> {
  public static final Key<DartValidateBeforeRunTask> ID = Key.create("DartValidate");

  @SuppressWarnings({"unused", "FieldCanBeLocal"})
  @NotNull private final Project myProject;

  public DartValidateBeforeRunTaskProvider(@NotNull Project project) {
    myProject = project;
  }

  @Override
  public Key<DartValidateBeforeRunTask> getId() {
    return ID;
  }

  @Override
  public String getName() {
    return DartBundle.message("runner.validate.before.run.name");
  }

  @Override
  public String getDescription(DartValidateBeforeRunTask task) {
    return DartBundle.message("runner.validate.before.run.name");
  }

  @Override
  public Icon getIcon() {
    return DartIcons.Dart_16;
  }

  @Override
  public Icon getTaskIcon(DartValidateBeforeRunTask task) {
    return DartIcons.Dart_16;
  }

  @Override
  public boolean isConfigurable() {
    return false;
  }

  @Nullable
  @Override
  public DartValidateBeforeRunTask createTask(RunConfiguration runConfiguration) {
    // Only show for Dart run configurations.
    if (!(runConfiguration instanceof DartRunConfigurationBase)) {
      return null;
    }

    DartValidateBeforeRunTask task = new DartValidateBeforeRunTask();
    task.setEnabled(true);
    return task;
  }

  @Override
  public boolean configureTask(RunConfiguration runConfiguration, DartValidateBeforeRunTask task) {
    return false;
  }

  @Override
  public boolean canExecuteTask(RunConfiguration configuration, DartValidateBeforeRunTask task) {
    return true;
  }

  @Override
  public boolean executeTask(DataContext context,
                             RunConfiguration configuration,
                             ExecutionEnvironment env,
                             DartValidateBeforeRunTask task) {
    if (!(configuration instanceof DartRunConfiguration)) {
      return false;
    }

    final Project project = env.getProject();

    final DartProblemsView problemsView = DartProblemsView.getInstance(project);
    problemsView.clearNotifications();

    final DartRunConfiguration dartRunConfiguration = (DartRunConfiguration)configuration;
    final Module module = dartRunConfiguration.getRunnerParameters().getModule(project);
    final String launchTitle = env.getRunnerAndConfigurationSettings() == null
                               ? "Launching app"
                               : "Launching " + env.getRunnerAndConfigurationSettings().getName();

    if (module == null) {
      return true;
    }

    // Collect module errors.
    final DartAnalysisServerService analysisServerService = DartAnalysisServerService.getInstance(project);
    List<DartServerData.DartError> errors = analysisServerService.getErrors(module);
    errors = errors.stream().filter(DartServerData.DartError::isError).collect(Collectors.toList());

    // If there are no 'error' level issues then exit.
    if (errors.isEmpty()) {
      return true;
    }

    final String content =
      errors.size() + " analysis <a href=\"issues\">" + StringUtil.pluralize("issue", errors.size()) + "</a> found.";
    problemsView.showWarningNotification(project, launchTitle, content, dartRunConfiguration.getIcon());

    return true;
  }

  public static class DartValidateBeforeRunTask extends BeforeRunTask<DartValidateBeforeRunTask> {
    public DartValidateBeforeRunTask() {
      super(ID);
    }
  }
}
