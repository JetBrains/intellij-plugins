package com.jetbrains.lang.dart.ide.runner.unittest;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.jetbrains.annotations.NotNull;

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
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final DartUnitRunConfiguration configuration = (DartUnitRunConfiguration)env.getRunProfile();

    final DartUnitRunnerParameters parameters = configuration.getRunnerParameters();
    final String filePath = parameters.getFilePath();
    assert filePath != null;

    final VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(VfsUtilCore.pathToUrl(filePath));
    if (virtualFile == null) {
      throw new ExecutionException("Can't find file: " + filePath);
    }

    final DartSdk sdk = DartSdk.getGlobalDartSdk();
    if (sdk == null) {
      throw new ExecutionException("Dart SDK is not configured");
    }
    final DartUnitRunningState dartCommandLineRunningState = new DartUnitRunningState(env, parameters, sdk);
    return super.doExecute(project, dartCommandLineRunningState, contentToReuse, env);
  }
}
