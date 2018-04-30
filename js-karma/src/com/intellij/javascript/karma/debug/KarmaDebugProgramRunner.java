package com.intellij.javascript.karma.debug;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfile;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.runners.AsyncProgramRunner;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.runners.ExecutionUtil;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.browsers.BrowserFamily;
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
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.util.ConcurrencyUtil;
import com.intellij.util.SingleAlarm;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import com.jetbrains.debugger.wip.BrowserChromeDebugProcess;
import com.jetbrains.debugger.wip.WipRemoteVmConnection;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.concurrency.Promises;
import org.jetbrains.debugger.connection.VmConnection;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

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
    KarmaServer karmaServer = consoleView.getKarmaServer();
    if (executionResult.getProcessHandler() instanceof NopProcessHandler) {
      RunContentDescriptor descriptor = KarmaUtil.createDefaultDescriptor(executionResult, environment);
      karmaServer.onPortBound(() -> ExecutionUtil.restartIfActive(descriptor));
      return Promises.resolvedPromise(descriptor);
    }
    DebuggableWebBrowser debuggableWebBrowser = getChromeInfo();
    return KarmaKt.prepareKarmaDebugger(environment.getProject(), debuggableWebBrowser, () -> {
      //noinspection CodeBlock2Expr
      return createDescriptor(environment, executionResult, consoleView, karmaServer, debuggableWebBrowser);
    });
  }

  @NotNull
  private static DebuggableWebBrowser getChromeInfo() throws ExecutionException {
    WebBrowser browser = WebBrowserManager.getInstance().getFirstBrowserOrNull(BrowserFamily.CHROME);
    if (browser == null) {
      throw new ExecutionException("Debugging is available in Chrome browser only.<p>" +
                                   "Please configure Chrome browser in 'Settings | Tools | Web Browsers'");
    }
    DebuggableWebBrowser debuggableWebBrowser = DebuggableWebBrowser.create(browser);
    if (debuggableWebBrowser == null) {
      throw new ExecutionException("Cannot find Chrome engine for debugging");
    }
    return debuggableWebBrowser;
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
          JavaScriptDebugProcess<? extends VmConnection> debugProcess =
            createDebugProcess(session, karmaServer, fileFinder, executionResult, debuggableWebBrowser, url);
          debugProcess.setScriptsCanBeReloaded(true);
          debugProcess.addFirstLineBreakpointPattern("\\.browserify$");
          debugProcess.setElementsInspectorEnabled(false);
          debugProcess.setConsoleMessagesSupportEnabled(false);
          debugProcess.setLayouter(consoleView.createDebugLayouter(debugProcess));
          karmaServer.onBrowsersReady(() -> {
            Runnable resumeTestRunning = ConcurrencyUtil.once(() -> resumeTestRunning((OSProcessHandler)executionResult.getProcessHandler()));
            SingleAlarm alarm = new SingleAlarm(resumeTestRunning, 5000);
            alarm.request();
            debugProcess.getConnection().executeOnStart((vm) -> {
              alarm.cancelAllRequests();
              resumeTestRunning.run();
              return Unit.INSTANCE;
            });
          });
          return debugProcess;
        }
      }
    );
    return session.getRunContentDescriptor();
  }

  @NotNull
  private static JavaScriptDebugProcess<? extends VmConnection> createDebugProcess(@NotNull XDebugSession session,
                                                                                   @NotNull KarmaServer karmaServer,
                                                                                   @NotNull DebuggableFileFinder fileFinder,
                                                                                   @NotNull ExecutionResult executionResult,
                                                                                   @NotNull DebuggableWebBrowser debuggableWebBrowser,
                                                                                   @NotNull Url url) {
    KarmaConfig karmaConfig = karmaServer.getKarmaConfig();
    if (karmaConfig != null && karmaConfig.getRemoteDebuggingPort() > 0) {
      WipRemoteVmConnection connection = new WipRemoteVmConnection();
      BrowserChromeDebugProcess debugProcess = new BrowserChromeDebugProcess(session, fileFinder, connection, executionResult);
      connection.open(new InetSocketAddress(karmaConfig.getHostname(), karmaConfig.getRemoteDebuggingPort()));
      return debugProcess;
    }
    JavaScriptDebugEngine debugEngine = debuggableWebBrowser.getDebugEngine();
    WebBrowser browser = debuggableWebBrowser.getWebBrowser();
    // If a capturing page was open, but not connected (e.g. it happens after karma server restart),
    // reload it to capture. Otherwise (no capturing page was open), reloading shouldn't harm.
    boolean reloadPage = !karmaServer.areBrowsersReady();
    return debugEngine.createDebugProcess(session, browser, fileFinder, url, executionResult, reloadPage);
  }

  @NotNull
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

  private static void resumeTestRunning(@NotNull OSProcessHandler processHandler) {
    Process process = processHandler.getProcess();
    if (process.isAlive()) {
      try {
        OutputStream processInput = process.getOutputStream();
        processInput.write("resume-test-running\n".getBytes(CharsetToolkit.UTF8_CHARSET));
        processInput.flush();
      }
      catch (IOException e) {
        LOG.warn("process.isAlive()=" + process.isAlive(), e);
      }
    }
  }
}
