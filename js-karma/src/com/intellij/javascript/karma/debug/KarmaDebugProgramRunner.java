package com.intellij.javascript.karma.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.GenericProgramRunner;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowsersConfiguration;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author Sergey Simonchik
 */
public class KarmaDebugProgramRunner extends GenericProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJsTestRunnerDebug";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(Project project,
                                           Executor executor,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
    return startSession(project, executor, state, contentToReuse, env);
  }

  @Nullable
  private <Connection> RunContentDescriptor startSession(@NotNull Project project,
                                                         @NotNull Executor executor,
                                                         RunProfileState state,
                                                         @Nullable RunContentDescriptor contentToReuse,
                                                         @NotNull ExecutionEnvironment env) throws ExecutionException {
    final JSDebugEngine<Connection> debugEngine = getChromeDebugEngine();
    if (debugEngine == null) {
      throw new ExecutionException("No debuggable browser found");
    }
    if (!debugEngine.prepareDebugger(project)) {
      return null;
    }

    final ExecutionResult executionResult = state.execute(executor, this);
    if (executionResult == null) {
      return null;
    }
    KarmaConsoleView testRunConsole = KarmaConsoleView.get(executionResult);
    if (testRunConsole == null) {
      throw new RuntimeException("KarmaRunSession was expected!");
    }
    KarmaServer karmaServer = testRunConsole.getKarmaRunSession().getKarmaServer();

    final Connection connection = debugEngine.openConnection(false);
    final String url = "http://localhost:" + karmaServer.getWebServerPort();

    final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
    XDebugSession session = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
      @NotNull
      public XDebugProcess start(@NotNull final XDebugSession session) {
        return debugEngine.createDebugProcess(session, fileFinder, connection, url, executionResult);
      }
    });

    // must be here, after all breakpoints were queued
    ((JSDebugProcess)session.getDebugProcess()).getConnection().queueRequest(new Runnable() {
      @Override
      public void run() {
        resumeJstdClientRunning(executionResult.getProcessHandler());
      }
    });
    return session.getRunContentDescriptor();
  }

  private static DebuggableFileFinder getDebuggableFileFinder(@NotNull KarmaServer karmaServer) {
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      File basePath = new File(karmaConfig.getBasePath());
      for (String path : karmaConfig.getFiles()) {
        String suffix = "/**/*.js";
        if (path.equals(suffix)) {
          String res = path.substring(0, path.length() - suffix.length());
          VirtualFile vFile = VfsUtil.findFileByIoFile(new File(basePath, res), false);
          if (vFile != null) {
            String key = "http://localhost:" + karmaServer.getWebServerPort() + "/base/" + res;
            mappings.put(key, vFile);
          }
        }
      }
    }
    System.out.println("Mappings: " + mappings);
    return new RemoteDebuggingFileFinder(mappings, false);
  }

  private static <C> JSDebugEngine<C> getChromeDebugEngine() {
    JSDebugEngine<C>[] engines = (JSDebugEngine<C>[])JSDebugEngine.getEngines();
    for (JSDebugEngine<C> engine : engines) {
      if (engine.getBrowserFamily() == BrowsersConfiguration.BrowserFamily.CHROME) {
        return engine;
      }
    }
    return null;
  }

  private static void resumeJstdClientRunning(@NotNull ProcessHandler processHandler) {
    if (processHandler instanceof OSProcessHandler) {
      // process's input stream will be closed on process termination
      @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ConstantConditions"})
      PrintWriter writer = new PrintWriter(processHandler.getProcessInput());
      writer.println("debug-session-started");
      writer.flush();
    }
  }

}
