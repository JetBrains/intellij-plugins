package org.jetbrains.flutter.run;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.PathUtil;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FlutterRunConfiguration extends DartRunConfigurationBase {
  private @NotNull FlutterRunnerParameters myRunnerParameters = new FlutterRunnerParameters();

  public FlutterRunConfiguration(final Project project, final ConfigurationFactory factory, final String name) {
    super(project, factory, name);
  }

  @NotNull
  public FlutterRunnerParameters getRunnerParameters() {
    return myRunnerParameters;
  }

  @NotNull
  @Override
  public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
    return new FlutterConfigurationEditorForm(getProject());
  }

  @Nullable
  @Override
  public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment environment) throws ExecutionException {
    return new FlutterRunningState(environment);
  }

  @Nullable
  public String suggestedName() {
    final String filePath = myRunnerParameters.getFilePath();
    return filePath == null ? null : PathUtil.getFileName(filePath);
  }

  public FlutterRunConfiguration clone() {
    final FlutterRunConfiguration clone = (FlutterRunConfiguration)super.clone();
    clone.myRunnerParameters = myRunnerParameters.clone();
    return clone;
  }
}
