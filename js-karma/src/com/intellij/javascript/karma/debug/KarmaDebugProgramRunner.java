package com.intellij.javascript.karma.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
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
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.Set;

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
  protected RunContentDescriptor doExecute(final Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      throw new RuntimeException("KarmaConsoleView was expected!");
    }

    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.isReady() && karmaServer.hasCapturedBrowsers()) {
      return doStart(project, karmaServer, consoleView, executionResult, contentToReuse, env);
    }
    RunContentBuilder contentBuilder = new RunContentBuilder(project, this, executor, executionResult, env);
    final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
    karmaServer.doWhenReadyWithCapturedBrowser(new Runnable() {
      @Override
      public void run() {
        KarmaUtil.restart(descriptor);
      }
    });
    return descriptor;
  }

  private <Connection> RunContentDescriptor doStart(@NotNull final Project project,
                                                    @NotNull KarmaServer karmaServer,
                                                    @NotNull final KarmaConsoleView consoleView,
                                                    @NotNull final ExecutionResult executionResult,
                                                    @Nullable RunContentDescriptor contentToReuse,
                                                    @NotNull ExecutionEnvironment env) throws ExecutionException {
    final JSDebugEngine<Connection> debugEngine = getDebugEngine(karmaServer.getCapturedBrowsers());
    if (debugEngine == null) {
      throw new ExecutionException("No debuggable browser found");
    }
    if (!debugEngine.prepareDebugger(project)) {
      return null;
    }
    final Connection connection = debugEngine.openConnection(true);
    final String url = "http://localhost:" + karmaServer.getWebServerPort() + "/debug.html";

    final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
    XDebugSession session = XDebuggerManager.getInstance(project).startSession(
      this,
      env,
      contentToReuse,
      new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull final XDebugSession session) {
          XDebugProcess debugProcess = debugEngine.createDebugProcess(session, fileFinder, connection, url, executionResult);
          debugProcess.setLayoutCustomizer(consoleView);
          return debugProcess;
        }
      }
    );
    return session.getRunContentDescriptor();
  }

  private static DebuggableFileFinder getDebuggableFileFinder(@NotNull KarmaServer karmaServer) {
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    String urlPrefix = "http://localhost:" + karmaServer.getWebServerPort();
    if (karmaConfig != null) {
      @SuppressWarnings("ConstantConditions")
      File basePath = new File(karmaConfig.getBasePath());
      VirtualFile vBasePath = VfsUtil.findFileByIoFile(basePath, false);
      if (vBasePath != null && vBasePath.isValid()) {
        mappings.put(urlPrefix + "/base", vBasePath);
      }
    }
    VirtualFile root = LocalFileSystem.getInstance().getRoot();
    if (SystemInfo.isWindows) {
      for (VirtualFile child : root.getChildren()) {
        mappings.put(urlPrefix + "/absolute" + child.getName(), child);
      }
    }
    else {
      mappings.put(urlPrefix + "/absolute", root);
    }
    return new RemoteDebuggingFileFinder(mappings, false);
  }

  @Nullable
  private static <C> JSDebugEngine<C> getDebugEngine(@NotNull Collection<String> capturedBrowsers) {
    //noinspection unchecked
    JSDebugEngine<C>[] engines = (JSDebugEngine<C>[])JSDebugEngine.getEngines();
    Set<JSDebugEngine<C>> capturedEngines = ContainerUtil.newHashSet();
    for (JSDebugEngine<C> engine : engines) {
      for (String capturedBrowserName : capturedBrowsers) {
        if (capturedBrowserName.contains(engine.getBrowserFamily().getName())) {
          capturedEngines.add(engine);
          break;
        }
      }
    }
    if (capturedEngines.isEmpty()) {
      return null;
    }
    return capturedEngines.iterator().next();
  }

}
