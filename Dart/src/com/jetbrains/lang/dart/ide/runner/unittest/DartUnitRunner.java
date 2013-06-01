package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.ide.settings.DartSettings;
import org.jetbrains.annotations.NotNull;

/**
 * @author: Fedor.Korotkov
 */
public class DartUnitRunner extends DefaultProgramRunner {
  public static final String DART_UNIT_RUNNER_ID = "DartUnitRunner";

  @NotNull
  @Override
  public String getRunnerId() {
    return DART_UNIT_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultRunExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof DartUnitRunConfiguration;
  }

  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    final DartUnitRunConfiguration configuration = (DartUnitRunConfiguration)env.getRunProfile();

    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    final String filePath = parameters.getFilePath();
    assert filePath != null;

    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath));
    if (virtualFile == null) {
      throw new ExecutionException("Can't find file: " + filePath);
    }

    final Module module = ModuleUtilCore.findModuleForFile(virtualFile, project);
    final DartSettings dartSettings = DartSettings.getSettingsForModule(module);

    final DartUnitRunningState dartCommandLineRunningState = new DartUnitRunningState(env, parameters, dartSettings);
    return super.doExecute(project, executor, dartCommandLineRunningState, contentToReuse, env);
  }
}
