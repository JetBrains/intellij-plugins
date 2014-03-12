package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.Executor;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.AsyncResult;
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
public class KarmaDebugProgramRunner extends AsyncGenericProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJavaScriptTestRunnerDebug";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @NotNull
  @Override
  protected AsyncResult<RunProfileStarter> prepare(@NotNull Project project,
                                                   @NotNull ExecutionEnvironment environment,
                                                   @NotNull RunProfileState state) throws ExecutionException {
    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      return AsyncResult.rejected();
    }

    final KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      throw new RuntimeException("KarmaConsoleView was expected!");
    }

    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.areBrowsersReady()) {
      final DebuggableWebBrowser debuggableWebBrowser =
        new KarmaDebugBrowserSelector(project, karmaServer.getCapturedBrowsers(), environment, this).selectDebugEngine();
      return prepareDebugger(project, debuggableWebBrowser, new RunProfileStarter() {
        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull Project project,
                                            @NotNull Executor executor,
                                            @NotNull RunProfileState state,
                                            @Nullable RunContentDescriptor contentToReuse,
                                            @NotNull ExecutionEnvironment env) throws ExecutionException {
          if (debuggableWebBrowser == null) {
            return null;
          }

          final Url url = Urls.newFromEncoded(karmaServer.formatUrl("/debug.html"));
          final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
          XDebugSession session = XDebuggerManager.getInstance(project).startSession(
            KarmaDebugProgramRunner.this,
            env,
            contentToReuse,
            new XDebugProcessStarter() {
              @Override
              @NotNull
              public XDebugProcess start(@NotNull final XDebugSession session) {
                JSDebugEngine debugEngine = debuggableWebBrowser.getDebugEngine();
                WebBrowser browser = debuggableWebBrowser.getWebBrowser();
                JSDebugProcess<?> debugProcess = debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, true);
                debugProcess.setElementsInspectorEnabled(false);
                debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess));
                return debugProcess;
              }
            }
          );
          return session.getRunContentDescriptor();

        }
      });
    }
    else {
      return AsyncResult.<RunProfileStarter>done(new RunProfileStarter() {
        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull Project project,
                                            @NotNull Executor executor,
                                            @NotNull RunProfileState state,
                                            @Nullable RunContentDescriptor contentToReuse,
                                            @NotNull ExecutionEnvironment env) {
          RunContentBuilder contentBuilder = new RunContentBuilder(KarmaDebugProgramRunner.this, executionResult, env);
          final RunContentDescriptor descriptor = contentBuilder.showRunContent(contentToReuse);
          karmaServer.onBrowsersReady(new Runnable() {
            @Override
            public void run() {
              KarmaUtil.restart(descriptor);
            }
          });
          return descriptor;
        }
      });
    }
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

  public static AsyncResult<RunProfileStarter> prepareDebugger(@NotNull final Project project,
                                                               @Nullable final DebuggableWebBrowser debuggableWebBrowser,
                                                               @NotNull final RunProfileStarter starter) {
    if (debuggableWebBrowser == null) {
      return AsyncResult.done(starter);
    }
    else {
      final AsyncResult<RunProfileStarter> result = new AsyncResult<RunProfileStarter>();
      JSDebugEngine debugEngine = debuggableWebBrowser.getDebugEngine();
      WebBrowser browser = debuggableWebBrowser.getWebBrowser();
      debugEngine.prepareDebugger(project, browser).notifyWhenRejected(result).doWhenDone(new Runnable() {
        @Override
        public void run() {
          result.setDone(starter);
        }
      });
      return result;
    }
  }

}
