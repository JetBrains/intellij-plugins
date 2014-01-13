package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Sergey Simonchik
 */
public class KarmaDebugProgramRunner extends GenericProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJavaScriptTestRunnerDebug";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(@NotNull Project project,
                                           @NotNull RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           @NotNull ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final ExecutionResult executionResult = state.execute(env.getExecutor(), this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      throw new RuntimeException("KarmaConsoleView was expected!");
    }

    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.areBrowsersReady()) {
      return doStart(project, karmaServer, consoleView, executionResult, contentToReuse, env);
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(this, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    karmaServer.onBrowsersReady(new Runnable() {
      @Override
      public void run() {
        KarmaUtil.restart(descriptor);
      }
    });
    return descriptor;
  }

  @Nullable
  private RunContentDescriptor doStart(@NotNull final Project project,
                                       @NotNull KarmaServer karmaServer,
                                       @NotNull final KarmaConsoleView consoleView,
                                       @NotNull final ExecutionResult executionResult,
                                       @Nullable RunContentDescriptor contentToReuse,
                                       @NotNull ExecutionEnvironment env) throws ExecutionException {
    KarmaDebugBrowserSelector browserSelector = new KarmaDebugBrowserSelector(
      project,
      karmaServer.getCapturedBrowsers(),
      env,
      this
    );
    final JSDebugEngine debugEngine = browserSelector.selectDebugEngine();
    if (debugEngine == null) {
      return null;
    }
    if (!debugEngine.prepareDebugger(project, debugEngine.getWebBrowser())) {
      return null;
    }

    final Url url = Urls.newFromEncoded(karmaServer.formatUrl("/debug.html"));
    final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
    XDebugSession session = XDebuggerManager.getInstance(project).startSession(
      this,
      env,
      contentToReuse,
      new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          JSDebugProcess<?> debugProcess = debugEngine.createDebugProcess(session, fileFinder, url, executionResult, true);
          debugProcess.setElementsInspectorEnabled(false);
          debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess));
          return debugProcess;
        }
      }
    );
    return session.getRunContentDescriptor();
  }

  private static DebuggableFileFinder getDebuggableFileFinder(@NotNull KarmaServer karmaServer) {
    ImmutableBiMap.Builder<String, VirtualFile> mappings = ImmutableBiMap.builder();
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      File basePath = new File(karmaConfig.getBasePath());
      VirtualFile vBasePath = VfsUtil.findFileByIoFile(basePath, false);
      if (vBasePath != null && vBasePath.isValid()) {
        String baseUrl = karmaServer.formatUrlWithoutUrlRoot("/base");
        mappings.put(baseUrl, vBasePath);
      }
    }
    for (VirtualFile root : ManagingFS.getInstance().getLocalRoots()) {
      mappings.put(karmaServer.formatUrlWithoutUrlRoot("/absolute"), root);
    }
    return new RemoteDebuggingFileFinder(mappings.build(), false);
  }

}
