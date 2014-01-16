package com.intellij.javascript.karma.debug;

import com.google.common.collect.ImmutableBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.BaseProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.RunContentBuilder;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.JsRunners;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
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
public class KarmaDebugProgramRunner extends BaseProgramRunner {

  @NotNull
  @Override
  public String getRunnerId() {
    return "KarmaJavaScriptTestRunnerDebug";
  }

  @Override
  public boolean canRun(@NotNull String executorId, @NotNull RunProfile profile) {
    return DefaultDebugExecutor.EXECUTOR_ID.equals(executorId) && profile instanceof KarmaRunConfiguration;
  }

  @Override
  protected void startRunProfile(@NotNull final ExecutionEnvironment environment,
                                 @Nullable final Callback callback,
                                 @NotNull final Project project,
                                 @NotNull final RunProfileState state) throws ExecutionException {
    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      JsRunners.start(environment, callback, project, state, null, null);
      return;
    }

    final KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult);
    if (consoleView == null) {
      throw new RuntimeException("KarmaConsoleView was expected!");
    }

    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.areBrowsersReady()) {
      final Pair<JSDebugEngine, WebBrowser> engineAndBrowser =
        new KarmaDebugBrowserSelector(project, karmaServer.getCapturedBrowsers(), environment, this).selectDebugEngine();
      JsRunners.start(environment, callback, project, state, engineAndBrowser, new JsRunners.Starter() {
        @Override
        public RunContentDescriptor start(RunContentDescriptor contentToReuse) throws ExecutionException {
          if (engineAndBrowser == null) {
            return null;
          }

          final Url url = Urls.newFromEncoded(karmaServer.formatUrl("/debug.html"));
          final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
          XDebugSession session = XDebuggerManager.getInstance(project).startSession(
            KarmaDebugProgramRunner.this,
            environment,
            contentToReuse,
            new XDebugProcessStarter() {
              @Override
              @NotNull
              public XDebugProcess start(@NotNull final XDebugSession session) {
                JSDebugProcess<?> debugProcess =
                  engineAndBrowser.first.createDebugProcess(session, engineAndBrowser.second, fileFinder, url, executionResult, true);
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
      JsRunners.start(environment, callback, project, state, null, new JsRunners.Starter() {
        @NotNull
        @Override
        public RunContentDescriptor start(@Nullable RunContentDescriptor contentToReuse) {
          RunContentBuilder contentBuilder = new RunContentBuilder(KarmaDebugProgramRunner.this, executionResult, environment);
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

}
