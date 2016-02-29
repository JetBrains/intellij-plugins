package com.jetbrains.lang.dart.ide.runner.test;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.test.ui.DartTestConfigurationEditorForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartTestRunConfiguration extends DartRunConfigurationBase {

  private @NotNull DartTestRunnerParameters myRunnerParameters = new DartTestRunnerParameters();

  protected DartTestRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @NotNull
  public DartTestRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new DartTestConfigurationEditorForm(getProject());
  }

  @Nullable
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) throws ExecutionException {
    return new DartTestRunningState(env);
  }

  @Nullable
  public String suggestedName() {
    final String path = myRunnerParameters.getFilePath();
    final String groupOrTestName = myRunnerParameters.getTestName();
    if (path == null) return null;

    final String fileOrDirName = PathUtil.getFileName(path);
    switch (myRunnerParameters.getScope()) {
      case GROUP_OR_TEST_BY_NAME:
        if (groupOrTestName != null) {
          return DartBundle.message("test.0.in.1", groupOrTestName, fileOrDirName);
        }
        // fall through
      case FILE:
        return DartBundle.message("all.tests.in.0", fileOrDirName);
      case FOLDER:
        final String dirName;
        if ("test".equals(fileOrDirName)) {
          final String parentPath = PathUtil.getParentPath(path);
          final String parentDirName = PathUtil.getFileName(parentPath);
          dirName = parentDirName.isEmpty() ? fileOrDirName : parentDirName + "/" + fileOrDirName;
        }
        else {
          dirName = fileOrDirName;
        }
        return DartBundle.message("all.tests.in.0", dirName);
      default:
        return null;
    }
  }

  public RunConfiguration clone() {
    final DartTestRunConfiguration clone = (DartTestRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }
}
