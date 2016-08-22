package com.jetbrains.lang.dart.ide.runner;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationError;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.DefaultProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.runner.base.DartRunConfigurationBase;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineDebugProcess;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.DartCommandLineRunningState;
import com.jetbrains.lang.dart.ide.runner.server.DartRemoteDebugConfiguration;
import com.jetbrains.lang.dart.ide.runner.server.vmService.DartVmServiceDebugProcess;
import com.jetbrains.lang.dart.ide.runner.test.DartTestRunnerParameters;
import com.jetbrains.lang.dart.sdk.DartSdk;
import com.jetbrains.lang.dart.util.DartUrlResolver;
import com.jetbrains.lang.dart.util.PubspecYamlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DartRunner extends DefaultProgramRunner {

  private static final Logger LOG = Logger.getInstance(DartRunner.class.getName());

  @NotNull
  @Override
  public String getRunnerId() {
    return "DartRunner";
  }

  @Override
  public boolean canRun(final @NotNull String executorId, final @NotNull RunProfile profile) {
    return (profile instanceof DartCommandLineRunConfiguration && (DefaultRunExecutor.EXECUTOR_ID.equals(executorId) ||
                                                                   DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)))
           ||
           (profile instanceof DartTestRunnerParameters && DefaultRunExecutor.EXECUTOR_ID.equals(executorId))
           ||
           (profile instanceof DartRemoteDebugConfiguration && DefaultDebugExecutor.EXECUTOR_ID.equals(executorId));
  }

  @Override
  protected RunContentDescriptor doExecute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
    final String executorId = env.getExecutor().getId();

    if (DefaultRunExecutor.EXECUTOR_ID.equals(executorId)) {
      return super.doExecute(state, env);
    }

    if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId)) {
      try {
        final String dasExecutionContextId;

        final RunProfile runConfig = env.getRunProfile();
        if (runConfig instanceof DartRunConfigurationBase &&
            DartAnalysisServerService.getInstance().serverReadyForRequest(env.getProject())) {
          final String path = ((DartRunConfigurationBase)runConfig).getRunnerParameters().getFilePath();
          assert path != null; // already checked
          dasExecutionContextId = DartAnalysisServerService.getInstance().execution_createContext(path);
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

    LOG.error("Unexpected executor id: " + executorId);
    return null;
  }

  protected int getTimeout() {
    return 5000; // Allow 5 seconds to connect to the observatory.
  }

  private RunContentDescriptor doExecuteDartDebug(final @NotNull RunProfileState state,
                                                  final @NotNull ExecutionEnvironment env,
                                                  final @Nullable String dasExecutionContextId) throws RuntimeConfigurationError,
                                                                                                       ExecutionException {
    final DartSdk sdk = DartSdk.getDartSdk(env.getProject());
    assert (sdk != null); // already checked

    final RunProfile runConfiguration = env.getRunProfile();
    final VirtualFile contextFileOrDir;
    final boolean entryPointInLibFolder;
    final ExecutionResult executionResult;
    final String debuggingHost;
    final int debuggingPort;
    final int observatoryPort;

    if (runConfiguration instanceof DartRunConfigurationBase) {
      contextFileOrDir = ((DartRunConfigurationBase)runConfiguration).getRunnerParameters().getDartFile();

      final VirtualFile pubspec = PubspecYamlUtil.findPubspecYamlFile(env.getProject(), contextFileOrDir);
      entryPointInLibFolder = pubspec != null && contextFileOrDir.getPath().startsWith(pubspec.getParent().getPath() + "/lib/");

      executionResult = state.execute(env.getExecutor(), this);
      if (executionResult == null) {
        return null;
      }

      debuggingHost = null;
      debuggingPort = ((DartCommandLineRunningState)state).getDebuggingPort();
      observatoryPort = ((DartCommandLineRunningState)state).getObservatoryPort();
    }
    else if (runConfiguration instanceof DartRemoteDebugConfiguration) {
      entryPointInLibFolder = false;
      final String path = ((DartRemoteDebugConfiguration)runConfiguration).getParameters().getDartProjectPath();
      contextFileOrDir = LocalFileSystem.getInstance().findFileByPath(path);
      if (contextFileOrDir == null) {
        throw new RuntimeConfigurationError("Folder not found: " + FileUtil.toSystemDependentName(path));
      }

      executionResult = null;

      debuggingHost = ((DartRemoteDebugConfiguration)runConfiguration).getParameters().getHost();

      if (StringUtil.compareVersionNumbers(sdk.getVersion(), "1.14") < 0) {
        debuggingPort = ((DartRemoteDebugConfiguration)runConfiguration).getParameters().getPort();
        observatoryPort = -1;
      }
      else {
        debuggingPort = -1; // not used
        observatoryPort = ((DartRemoteDebugConfiguration)runConfiguration).getParameters().getPort();
      }
    }
    else {
      LOG.error("Unexpected run configuration: " + runConfiguration.getClass().getName());
      return null;
    }

    FileDocumentManager.getInstance().saveAllDocuments();

    final XDebuggerManager debuggerManager = XDebuggerManager.getInstance(env.getProject());
    final XDebugSession debugSession = debuggerManager.startSession(env, new XDebugProcessStarter() {
      @Override
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) {
        final DartUrlResolver dartUrlResolver = getDartUrlResolver(env.getProject(), contextFileOrDir);
        return StringUtil.compareVersionNumbers(sdk.getVersion(), "1.14") < 0
               ? new DartCommandLineDebugProcess(session, debuggingHost, debuggingPort, observatoryPort, executionResult, dartUrlResolver)
               : new DartVmServiceDebugProcess(session,
                                               StringUtil.notNullize(debuggingHost, "localhost"),
                                               observatoryPort,
                                               executionResult,
                                               dartUrlResolver,
                                               dasExecutionContextId,
                                               runConfiguration instanceof DartRemoteDebugConfiguration,
                                               entryPointInLibFolder,
                                               getTimeout());
      }
    });

    return debugSession.getRunContentDescriptor();
  }

  protected DartUrlResolver getDartUrlResolver(@NotNull final Project project, @NotNull final VirtualFile contextFileOrDir) {
    return DartUrlResolver.getInstance(project, contextFileOrDir);
  }
}
