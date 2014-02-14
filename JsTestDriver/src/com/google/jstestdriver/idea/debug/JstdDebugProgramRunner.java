package com.google.jstestdriver.idea.debug;

import com.google.jstestdriver.idea.TestRunner;
import com.google.jstestdriver.idea.execution.JstdRunConfiguration;
import com.google.jstestdriver.idea.execution.JstdRunConfigurationVerifier;
import com.google.jstestdriver.idea.execution.JstdTestRunnerCommandLineState;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowPanel;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.RunnerRegistry;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.execution.JsRunners;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
import com.intellij.openapi.util.Pair;
import com.intellij.util.Url;
import com.intellij.util.Urls;
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
public class JstdDebugProgramRunner extends AsyncGenericProgramRunner {
  private static final String DEBUG_RUNNER_ID = JstdDebugProgramRunner.class.getSimpleName();
  private static Boolean IS_AVAILABLE = null;

  @NotNull
  @Override
  public String getRunnerId() {
    return DEBUG_RUNNER_ID;
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    if (DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof JstdRunConfiguration) {
      return !((JstdRunConfiguration) profile).getRunSettings().isExternalServerType();
    }
    else {
      return false;
    }
  }

  public static boolean isAvailable() {
    Boolean isAvailable = IS_AVAILABLE;
    if (isAvailable != null) {
      return isAvailable;
    }
    RunnerRegistry registry = RunnerRegistry.getInstance();
    isAvailable = registry.findRunnerById(DEBUG_RUNNER_ID) != null;
    IS_AVAILABLE = isAvailable;
    return isAvailable;
  }

  @NotNull
  @Override
  protected AsyncResult<RunProfileStarter> prepare(@NotNull final Project project,
                                                   @NotNull final ExecutionEnvironment environment,
                                                   @NotNull RunProfileState state) throws ExecutionException {
    final JstdRunConfiguration runConfiguration = (JstdRunConfiguration) environment.getRunProfile();
    JstdRunConfigurationVerifier.checkJstdServerAndBrowserEnvironment(project, runConfiguration.getRunSettings(), true);

    final JstdDebugBrowserInfo debugBrowserInfo = JstdDebugBrowserInfo.build(runConfiguration.getRunSettings());
    if (debugBrowserInfo == null) {
      throw new ExecutionException("Cannot find a browser that supports debugging.");
    }

    return JsRunners.start(project, Pair.create(debugBrowserInfo.getDebugEngine(), debugBrowserInfo.getBrowser()), new JsRunners.Starter() {
      @Nullable
      @Override
      public RunContentDescriptor start(@Nullable RunContentDescriptor contentToReuse) throws ExecutionException {
        return startSession(project, contentToReuse, environment, runConfiguration, debugBrowserInfo);
      }
    });
  }

  @Nullable
  private RunContentDescriptor startSession(@NotNull Project project,
                                            @Nullable RunContentDescriptor contentToReuse,
                                            @NotNull ExecutionEnvironment env,
                                            @NotNull JstdRunConfiguration runConfiguration,
                                            @NotNull final JstdDebugBrowserInfo debugBrowserInfo) throws ExecutionException {
    final WebBrowser browser = debugBrowserInfo.getBrowser();
    final Url url;
    if (browser.getFamily().equals(BrowserFamily.CHROME)) {
      url = Urls.newHttpUrl("127.0.0.1:" + JstdToolWindowPanel.serverPort, debugBrowserInfo.getCapturedBrowserUrl());
    }
    else {
      url = null;
    }

    JstdTestRunnerCommandLineState runState = runConfiguration.getState(env, null, true);
    final ExecutionResult executionResult = runState.execute(env.getExecutor(), this);
    debugBrowserInfo.fixIfChrome(executionResult.getProcessHandler());

    final RemoteDebuggingFileFinder fileFinder = new JstdDebuggableFileFinderProvider(new File(runConfiguration.getRunSettings().getConfigFile())).provideFileFinder();
    XDebugSession session = XDebuggerManager.getInstance(project).startSession(this, env, contentToReuse, new XDebugProcessStarter() {
      @Override
      @NotNull
      public XDebugProcess start(@NotNull XDebugSession session) {
        JSDebugProcess<?> process = debugBrowserInfo.getDebugEngine().createDebugProcess(session, browser, fileFinder, url, executionResult,
                                                                                         false);
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

  private static void resumeJstdClientRunning(@NotNull ProcessHandler processHandler) {
    // process's input stream will be closed on process termination
    @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ConstantConditions"})
    PrintWriter writer = new PrintWriter(processHandler.getProcessInput());
    writer.println(TestRunner.DEBUG_SESSION_STARTED);
    writer.flush();
  }
}
