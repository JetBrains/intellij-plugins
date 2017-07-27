package com.intellij.javascript.karma.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserFamily;
import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.ide.browsers.WebBrowserManager;
import com.intellij.javascript.debugger.DebuggableFileFinder;
import com.intellij.javascript.debugger.JavaScriptDebugEngine;
import com.intellij.javascript.debugger.JavaScriptDebugProcess;
import com.intellij.javascript.debugger.RemoteDebuggingFileFinder;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.util.Alarm;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;
import org.jetbrains.debugger.connection.VmConnection;

import java.io.PrintWriter;

public class KarmaDebugProgramRunner extends AsyncProgramRunner {
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
  protected Promise<RunContentDescriptor> execute(@NotNull ExecutionEnvironment environment, @NotNull RunProfileState state) throws ExecutionException {
    FileDocumentManager.getInstance().saveAllDocuments();
    ExecutionResult executionResult = state.execute(environment.getExecutor(), this);
    if (executionResult == null) {
      return Promise.resolve(null);
    }
    KarmaConsoleView consoleView = KarmaConsoleView.get(executionResult, state);
    if (consoleView == null) {
      return Promise.resolve(KarmaUtil.createDefaultDescriptor(executionResult, environment));
    }
    KarmaServer karmaServer = consoleView.getKarmaExecutionSession().getKarmaServer();
    if (karmaServer.areBrowsersReady()) {
      KarmaDebugBrowserSelector browserSelector = new KarmaDebugBrowserSelector(
        karmaServer.getCapturedBrowsers(),
        environment,
        consoleView
      );
      DebuggableWebBrowser debuggableWebBrowser = browserSelector.selectDebugEngine();
      if (debuggableWebBrowser == null) {
        return Promises.resolvedPromise(KarmaUtil.createDefaultDescriptor(executionResult, environment));
      }
      return KarmaKt.prepareKarmaDebugger(environment.getProject(), debuggableWebBrowser,
                                          () -> createDescriptor(environment, executionResult, consoleView, karmaServer,
                                                                 debuggableWebBrowser));
    }
    else {
      RunContentDescriptor descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment);
      karmaServer.onPortBound(() -> {
        if (Disposer.isDisposed(environment)) {
          return;
        }
        Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, environment);
        alarm.addRequest(() -> {
          if (karmaServer.isTerminated()) {
            return;
          }
          WebBrowser browser = WebBrowserManager.getInstance().getFirstBrowserOrNull(BrowserFamily.CHROME);
          BrowserLauncher.getInstance().browse(karmaServer.formatUrl("/"), browser, environment.getProject());
          Disposer.dispose(alarm);
        }, 2000, ModalityState.NON_MODAL);
        karmaServer.onBrowsersReady(() -> {
          Disposer.dispose(alarm);
          ExecutionUtil.restartIfActive(descriptor);
        });
      });
      return Promises.resolvedPromise(descriptor);
    }
  }

  @NotNull
  private static RunContentDescriptor createDescriptor(@NotNull ExecutionEnvironment environment,
                                                       @NotNull ExecutionResult executionResult,
                                                       @NotNull KarmaConsoleView consoleView,
                                                       @NotNull KarmaServer karmaServer,
                                                       @NotNull DebuggableWebBrowser debuggableWebBrowser) throws ExecutionException {
    Url url = Urls.newFromEncoded(karmaServer.formatUrl("/"));
    DebuggableFileFinder fileFinder = getDebuggableFileFinder(karmaServer);
    XDebugSession session = XDebuggerManager.getInstance(environment.getProject()).startSession(
      environment,
      new XDebugProcessStarter() {
        @Override
        @NotNull
        public XDebugProcess start(@NotNull XDebugSession session) {
          JavaScriptDebugEngine debugEngine = debuggableWebBrowser.getDebugEngine();
          WebBrowser browser = debuggableWebBrowser.getWebBrowser();
          JavaScriptDebugProcess<? extends VmConnection> debugProcess =
            debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, false);
          debugProcess.setScriptsCanBeReloaded(true);
          debugProcess.addFirstLineBreakpointPattern("\\.browserify$");
          debugProcess.setElementsInspectorEnabled(false);
          debugProcess.setConsoleMessagesSupportEnabled(false);
          debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess));
          Alarm alarm = new Alarm(Alarm.ThreadToUse.POOLED_THREAD, consoleView);
          alarm.addRequest(() -> {
            resumeTestRunning(executionResult.getProcessHandler());
            Disposer.dispose(alarm);
          }, 2000);
          return debugProcess;
        }
      }
    );
    return session.getRunContentDescriptor();
  }

  private static DebuggableFileFinder getDebuggableFileFinder(@NotNull KarmaServer karmaServer) {
    BiMap<String, VirtualFile> mappings = HashBiMap.create();
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    if (karmaConfig != null) {
      VirtualFile basePath = LocalFileSystem.getInstance().findFileByPath(karmaConfig.getBasePath());
      if (basePath != null && basePath.isValid()) {
        if (karmaConfig.isWebpack()) {
          mappings.put("webpack:///" + basePath.getPath(), basePath);
          VirtualFile nodeModulesDir = basePath.findChild(NodeModuleUtil.NODE_MODULES);
          if (nodeModulesDir != null && nodeModulesDir.isValid() && nodeModulesDir.isDirectory()) {
            mappings.put(karmaServer.formatUrlWithoutUrlRoot("/base/" + NodeModuleUtil.NODE_MODULES), nodeModulesDir);
          }
        }
        else {
          mappings.put(karmaServer.formatUrlWithoutUrlRoot("/base"), basePath);
        }
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

  private static void resumeTestRunning(@NotNull ProcessHandler processHandler) {
    if (processHandler instanceof OSProcessHandler) {
      // process's input stream will be closed on process termination
      @SuppressWarnings({"IOResourceOpenedButNotSafelyClosed", "ConstantConditions"})
      PrintWriter writer = new PrintWriter(processHandler.getProcessInput());
      writer.print("resume-test-running\n");
      writer.flush();
    }
  }
}
