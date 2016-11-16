package com.intellij.javascript.karma.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.RunProfileStarter;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.AsyncGenericProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import com.intellij.javascript.debugger.JavaScriptDebugProcess;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.LocalFileSystem;
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
import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.connection.VmConnection;

public class KarmaDebugProgramRunner extends AsyncGenericProgramRunner {

  private static final Logger LOG = Logger.getInstance(KarmaDebugProgramRunner.class);

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
  protected Promise<RunProfileStarter> prepare(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    final ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      return Promise.resolve(null);
    }
    final KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult, state);
    if (consoleView == null) {
      return Promise.resolve(KarmaUtil.createDefaultRunProfileStarter(executionResult));
    }
    final KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.areBrowsersReady()) {
      KarmaDebugBrowserSelector browserSelector = new KarmaDebugBrowserSelector(
        karmaServer.getCapturedBrowsers(),
        environment,
        consoleView
      );
      final DebuggableWebBrowser debuggableWebBrowser = browserSelector.selectDebugEngine();
      if (debuggableWebBrowser == null) {
        return Promise.resolve(KarmaUtil.createDefaultRunProfileStarter(executionResult));
      }
      return prepareDebugger(environment.getProject(), debuggableWebBrowser, new RunProfileStarter() {
        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) throws ExecutionException {
          final Url url = Urls.newFromEncoded(karmaServer.formatUrl("/debug.html"));
          final DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
          XDebugSession session = XDebuggerManager.getInstance(env.getProject()).startSession(
            env,
            new XDebugProcessStarter() {
              @Override
              @NotNull
              public XDebugProcess start(@NotNull XDebugSession session) {
                JavaScriptDebugEngine debugEngine = debuggableWebBrowser.getDebugEngine();
                WebBrowser browser = debuggableWebBrowser.getWebBrowser();
                JavaScriptDebugProcess<? extends VmConnection>
                  debugProcess = debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, true);
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
      return Promise.<RunProfileStarter>resolve(new RunProfileStarter() {
        @Nullable
        @Override
        public RunContentDescriptor execute(@NotNull RunProfileState state, @NotNull ExecutionEnvironment env) {
          final RunContentDescriptor descriptor = KarmaUtil.createDefaultDescriptor(executionResult, env);
          karmaServer.onBrowsersReady(() -> ExecutionUtil.restartIfActive(descriptor));
          return descriptor;
        }
      });
    }
  }

  private static DebuggableFileFinder getDebuggableFileFinder(@NotNull KarmaServer karmaServer) {
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      String systemDependentBasePath = FileUtil.toSystemDependentName(karmaConfig.getBasePath());
      VirtualFile basePath = LocalFileSystem.getInstance().findFileByPath(systemDependentBasePath);
      if (basePath != null && basePath.isValid()) {
        String baseUrl = karmaConfig.isWebpack() ? "webpack:///." : karmaServer.formatUrlWithoutUrlRoot("/base");
        mappings.put(baseUrl, basePath);
      }
    }
    if (SystemInfo.isWindows) {
      VirtualFile[] roots = ManagingFS.getInstance().getLocalRoots();
      for (VirtualFile root : roots) {
        String key = karmaServer.formatUrlWithoutUrlRoot("/absolute" + root.getName());
        if (mappings.containsKey(key)) {
          LOG.warn("Duplicate mapping for Karma debug: " + key);
        }
        else {
          mappings.put(key, root);
        }
      }
    }
    else {
      VirtualFile[] roots = ManagingFS.getInstance().getLocalRoots();
      if (roots.length == 1) {
        mappings.put(karmaServer.formatUrlWithoutUrlRoot("/absolute"), roots[0]);
      }
    }
    return new RemoteDebuggingFileFinder(mappings, null);
  }

  public static Promise<RunProfileStarter> prepareDebugger(@NotNull Project project,
                                                           @NotNull DebuggableWebBrowser debuggableWebBrowser,
                                                           @NotNull final RunProfileStarter starter) {
    return debuggableWebBrowser.getDebugEngine().prepareDebugger(project, debuggableWebBrowser.getWebBrowser())
      .then(aVoid -> starter);
  }
}
