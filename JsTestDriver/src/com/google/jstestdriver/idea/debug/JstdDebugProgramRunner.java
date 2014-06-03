package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.idea.TestRunner;
import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunProfileState;
import com.google.jstestdriver.idea.execution.NopProcessHandler;
import com.google.jstestdriver.idea.execution.settings.JstdRunSettings;
import com.google.jstestdriver.idea.server.JstdBrowserInfo;
import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.google.jstestdriver.idea.util.JstdUtil;
import com.intellij.execution.*;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.util.NullableConsumer;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.javascript.debugger.JavaScriptDebugEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.PrintWriter;

/**
 * @author Sergey Simonchik
 */
public class JstdDebugProgramRunner extends AsyncGenericProgramRunner {
  private static final String DEBUG_RUNNER_ID = JstdDebugProgramRunner.class.getSimpleName();
  private static Boolean IS_AVAILABLE_CACHE = null;

  @NotNull
  @Override
  public String getRunnerId() {
    return DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof JstdRunConfiguration;
  }

  public static boolean isAvailable() {
    Boolean isAvailable = IS_AVAILABLE_CACHE;
    if (isAvailable != null) {
      return isAvailable;
    }
    RunnerRegistry registry = RunnerRegistry.getInstance();
    isAvailable = registry.findRunnerById(DEBUG_RUNNER_ID) != null;
    IS_AVAILABLE_CACHE = isAvailable;
    return isAvailable;
  }

  @NotNull
  @Override
  protected AsyncResult<RunProfileStarter> prepare(@NotNull final Project project,
                                                   @NotNull ExecutionEnvironment environment,
                                                   @NotNull RunProfileState state) throws ExecutionException {
    final JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
    final JstdRunSettings runSettings = jstdState.getRunSettings();
    if (runSettings.isExternalServerType()) {
      throw new ExecutionException("JsTestDriver test can be debugged using local server running in IDE.");
    }
    JstdToolWindowManager jstdToolWindowManager = JstdToolWindowManager.getInstance(project);
    jstdToolWindowManager.setAvailable(true);
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    final AsyncResult<RunProfileStarter> result = new AsyncResult<RunProfileStarter>();
    if (server != null && !server.isStopped()) {
      prepareWithServer(project, result, server, jstdState);
      return result;
    }
    jstdToolWindowManager.restartServer(new NullableConsumer<JstdServer>() {
      @Override
      public void consume(@Nullable JstdServer server) {
        if (server != null) {
          prepareWithServer(project, result, server, jstdState);
        }
        else {
          result.setDone(null);
        }
      }
    });
    return result;
  }

  private void prepareWithServer(@NotNull final Project project,
                                 @NotNull final AsyncResult<RunProfileStarter> result,
                                 @NotNull JstdServer server,
                                 @NotNull final JstdRunProfileState jstdState) {
    if (server.isReadyForRunningTests()) {
      final JstdDebugBrowserInfo debugBrowserInfo = JstdDebugBrowserInfo.build(server, jstdState.getRunSettings());
      if (debugBrowserInfo != null) {
        ActionCallback prepareDebuggerCallback = debugBrowserInfo.getDebugEngine().prepareDebugger(project, debugBrowserInfo.getBrowser());
        prepareDebuggerCallback.notifyWhenRejected(result).doWhenDone(new Runnable() {
          @Override
          public void run() {
            result.setDone(new MyDebugStarter(debugBrowserInfo, JstdDebugProgramRunner.this));
          }
        });
      }
      else {
        result.setDone(new RunProfileStarter() {
          @Nullable
          @Override
          public RunContentDescriptor execute(@NotNull Project project,
                                              @NotNull Executor executor,
                                              @NotNull RunProfileState state,
                                              @Nullable RunContentDescriptor contentToReuse,
                                              @NotNull ExecutionEnvironment environment) throws ExecutionException {
            throw new ExecutionException("Please capture Chrome or Firefox and try again.");
          }
        });
      }
    }
    else {
      result.setDone(new MyRunStarter(server, this));
    }
  }

  private static class MyRunStarter extends RunProfileStarter {

    private final JstdServer myServer;
    private final JstdDebugProgramRunner myRunner;

    private MyRunStarter(@NotNull JstdServer server, @NotNull JstdDebugProgramRunner runner) {
      myServer = server;
      myRunner = runner;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull Project project,
                                        @NotNull Executor executor,
                                        @NotNull RunProfileState state,
                                        @Nullable RunContentDescriptor contentToReuse,
                                        @NotNull ExecutionEnvironment environment) throws ExecutionException {
      FileDocumentManager.getInstance().saveAllDocuments();
      JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
      ExecutionResult executionResult = jstdState.executeWithServer(myServer);
      RunContentBuilder contentBuilder = new RunContentBuilder(myRunner, executionResult, environment);
      final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
      if (executionResult.getProcessHandler() instanceof NopProcessHandler) {
        myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
          @Override
          public void onBrowserCaptured(@NotNull JstdBrowserInfo info) {
            JstdUtil.restart(descriptor);
            myServer.removeLifeCycleListener(this);
          }
        }, contentBuilder);
      }
      return descriptor;
    }
  }

  private static class MyDebugStarter extends RunProfileStarter {

    private final JstdDebugBrowserInfo myDebugBrowserInfo;
    private final JstdDebugProgramRunner myRunner;

    private MyDebugStarter(@NotNull JstdDebugBrowserInfo debugBrowserInfo, @NotNull JstdDebugProgramRunner runner) {
      myDebugBrowserInfo = debugBrowserInfo;
      myRunner = runner;
    }

    @Nullable
    @Override
    public RunContentDescriptor execute(@NotNull Project project,
                                        @NotNull Executor executor,
                                        @NotNull RunProfileState state,
                                        @Nullable RunContentDescriptor contentToReuse,
                                        @NotNull ExecutionEnvironment environment) throws ExecutionException {
      final WebBrowser browser = myDebugBrowserInfo.getBrowser();
      final Url url;
      if (browser.getFamily().equals(BrowserFamily.CHROME)) {
        url = Urls.newHttpUrl("127.0.0.1:" + myDebugBrowserInfo.getServerSettings().getPort(), myDebugBrowserInfo.getPath());
      }
      else {
        url = null;
      }
      JstdRunProfileState jstdState = JstdRunProfileState.cast(state);
      final ExecutionResult executionResult = state.execute(environment.getExecutor(), myRunner);

      final RemoteDebuggingFileFinder fileFinder = new JstdDebuggableFileFinderProvider(new File(jstdState.getRunSettings().getConfigFile())).provideFileFinder();
      XDebugSession session = XDebuggerManager.getInstance(project).startSession(myRunner, environment, contentToReuse, new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull XDebugSession session) {
          JavaScriptDebugEngine debugEngine = myDebugBrowserInfo.getDebugEngine();
          JSDebugProcess<?> process = debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, false);
          process.setElementsInspectorEnabled(false);
          return process;
        }
      });

      // must be here, after all breakpoints were queued
      ((JSDebugProcess)session.getDebugProcess()).getConnection().executeOnStart(new Runnable() {
        @Override
        public void run() {
          Runnable runnable = new Runnable() {
            @Override
            public void run() {
              resumeJstdClientRunning(executionResult.getProcessHandler());
            }
          };

          if (ApplicationManager.getApplication().isReadAccessAllowed()) {
            ApplicationManager.getApplication().executeOnPooledThread(runnable);
          }
          else {
            runnable.run();
          }
        }
      });
      return session.getRunContentDescriptor();
    }
  }

  private static void resumeJstdClientRunning(@NotNull ProcessHandler processHandler) {
    // process's input stream will be closed on process termination
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ConstantConditions"})
    PrintWriter writer = new PrintWriter(processHandler.getProcessInput());
    writer.println(TestRunner.DEBUG_SESSION_STARTED);
    writer.flush();
  }
}
