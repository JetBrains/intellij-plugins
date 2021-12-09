// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.webdev.DartWebdevConfiguration;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunConfiguration;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.TimeUnit;

public class DartRunner extends GenericProgramRunner {

  private static final Logger LOG = Logger.getInstance(DartRunner.class.getName());

  // Allow 5 seconds to connect to the observatory.
  private static final int OBSERVATORY_TIMEOUT_MS = Math.toIntExact(TimeUnit.SECONDS.toMillis(5));

  @NotNull
  @Override
  public String getRunnerId() {
    return "DartRunner";
  }

  @Override
  public boolean canRun(final @NotNull String executorId, final @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) &&
           (profile instanceof DartCommandLineRunConfiguration ||
            profile instanceof DartTestRunConfiguration ||
            profile instanceof DartRemoteDebugConfiguration ||
            profile instanceof DartWebdevConfiguration);
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final String executorId = env.getExecutor().getId();
    if (!DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
      LOG.error("Unexpected executor id: " + executorId);
      return null;
    }

    try {
      final String dasExecutionContextId;

      final RunProfile runConfig = env.getRunProfile();
      if (runConfig instanceof DartRunConfigurationBase &&
          DartAnalysisServerService.getInstance(env.getProject()).serverReadyForRequest()) {
        final String path = ((DartRunConfigurationBase)runConfig).getRunnerParameters().getFilePath();
        assert path != null; // already checked
        dasExecutionContextId = DartAnalysisServerService.getInstance(env.getProject()).execution_createContext(path);
      }
      else {
        dasExecutionContextId = null; // remote debug or can't start DAS
      }

      return doExecuteDartDebug(state, env, dasExecutionContextId);
    }
    catch (RuntimeConfigurationError e) {
      throw new ExecutionException(e);
    }
  }

  protected int getTimeout() {
    return OBSERVATORY_TIMEOUT_MS;
  }

  @Nullable
  private RunContentDescriptor doExecuteDartDebug(final @NotNull RunProfileState state,
                                                  final @NotNull ExecutionEnvironment env,
                                                  final @Nullable String dasExecutionContextId) throws RuntimeConfigurationError,
                                                                                                       ExecutionException {
    final RunProfile runConfiguration = env.getRunProfile();
    final VirtualFile contextFileOrDir;
    VirtualFile currentWorkingDirectory;
    final ExecutionResult executionResult;
    final Project project = env.getProject();
    final DartVmServiceDebugProcess.DebugType debugType;

    if (runConfiguration instanceof DartRunConfigurationBase) {
      contextFileOrDir = ((DartRunConfigurationBase)runConfiguration).getRunnerParameters().getDartFileOrDirectory();

      final String cwd =
        ((DartRunConfigurationBase)runConfiguration).getRunnerParameters().computeProcessWorkingDirectory(project);
      currentWorkingDirectory = LocalFileSystem.getInstance().findFileByPath((cwd));

      executionResult = state.execute(env.getExecutor(), this);
      if (executionResult == null) {
        return null;
      }

      debugType = DartVmServiceDebugProcess.DebugType.CLI;
    }
    else if (runConfiguration instanceof DartRemoteDebugConfiguration) {
      final String path = ((DartRemoteDebugConfiguration)runConfiguration).getParameters().getDartProjectPath();
      contextFileOrDir = LocalFileSystem.getInstance().findFileByPath(path);
      if (contextFileOrDir == null) {
        throw new RuntimeConfigurationError(DartBundle.message("dialog.message.folder.not.found", FileUtil.toSystemDependentName(path)));
      }

      currentWorkingDirectory = contextFileOrDir;
      executionResult = null;
      debugType = DartVmServiceDebugProcess.DebugType.REMOTE;
    }
    else if (runConfiguration instanceof DartWebdevConfiguration) {
      contextFileOrDir = ((DartWebdevConfiguration)runConfiguration).getParameters().getHtmlFile();

      currentWorkingDirectory = ((DartWebdevConfiguration)runConfiguration).getParameters().getWorkingDirectory(project);

      executionResult = state.execute(env.getExecutor(), this);
      if (executionResult == null) {
        return null;
      }

      debugType = DartVmServiceDebugProcess.DebugType.WEBDEV;
    }
    else {
      LOG.error("Unexpected run configuration: " + runConfiguration.getClass().getName());
      return null;
    }

    FileDocumentManager.getInstance().saveAllDocuments();

    final XDebuggerManager debuggerManager = XDebuggerManager.getInstance(project);
    final XDebugSession debugSession = debuggerManager.startSession(env, new XDebugProcessStarter() {
      @Override
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) throws ExecutionException {
        final DartUrlResolver dartUrlResolver = getDartUrlResolver(project, contextFileOrDir);
        DartVmServiceDebugProcess debugProcess = new DartVmServiceDebugProcess(session,
                                                                               executionResult,
                                                                               dartUrlResolver,
                                                                               dasExecutionContextId,
                                                                               debugType,
                                                                               getTimeout(),
                                                                               currentWorkingDirectory);
        debugProcess.start();
        return debugProcess;
      }
    });

    return debugSession.getRunContentDescriptor();
  }

  protected DartUrlResolver getDartUrlResolver(@NotNull final Project project, @NotNull final VirtualFile contextFileOrDir) {
    return DartUrlResolver.getInstance(project, contextFileOrDir);
  }
}
