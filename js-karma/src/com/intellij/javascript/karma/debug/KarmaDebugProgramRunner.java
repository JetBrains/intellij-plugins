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
import com.intellij.ide.browsers.WebBrowser;
import com.intellij.javascript.debugger.engine.JSDebugEngine;
import com.intellij.javascript.debugger.execution.RemoteDebuggingFileFinder;
import com.intellij.javascript.debugger.impl.DebuggableFileFinder;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.CapturedBrowser;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * @author Sergey Simonchik
 */
public class KarmaDebugProgramRunner extends GenericProgramRunner {

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

  @Nullable
  @Override
  protected RunContentDescriptor doExecute(final Project project,
                                           RunProfileState state,
                                           RunContentDescriptor contentToReuse,
                                           ExecutionEnvironment env) throws ExecutionException {
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
    final Connection connection = debugEngine.openConnection();
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
          JSDebugProcess debugProcess = debugEngine.createDebugProcess(session, fileFinder, connection, url, executionResult, true);
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
      @SuppressWarnings("ConstantConditions")
      File basePath = new File(karmaConfig.getBasePath());
      VirtualFile vBasePath = VfsUtil.findFileByIoFile(basePath, false);
      if (vBasePath != null && vBasePath.isValid()) {
        String baseUrl = karmaServer.formatUrlWithoutUrlRoot("/base");
        mappings.put(baseUrl, vBasePath);
      }
    }
    VirtualFile root = LocalFileSystem.getInstance().getRoot();
    if (SystemInfo.isWindows) {
      for (VirtualFile child : root.getChildren()) {
        String url = karmaServer.formatUrlWithoutUrlRoot("/absolute" + child.getName());
        mappings.put(url, child);
      }
    }
    else {
      String url = karmaServer.formatUrlWithoutUrlRoot("/absolute");
      mappings.put(url, root);
    }
    return new RemoteDebuggingFileFinder(mappings.build(), false);
  }

  @Nullable
  private static <C> JSDebugEngine<C> getDebugEngine(@NotNull Collection<CapturedBrowser> browsers) throws ExecutionException {
    //noinspection unchecked
    JSDebugEngine<C>[] engines = (JSDebugEngine<C>[])JSDebugEngine.getEngines();
    for (JSDebugEngine<C> engine : engines) {
      for (CapturedBrowser browser : browsers) {
        if (browser.getName().contains(engine.getWebBrowser().getName())) {
          return engine;
        }
      }
    }
    JSDebugEngine<C> defaultEngine = null;
    for (JSDebugEngine<C> engine : engines) {
      if (engine.getWebBrowser() == WebBrowser.CHROME) {
        defaultEngine = engine;
        break;
      }
    }
    String message = "No captured browsers with debugging capabilities found!";
    if (defaultEngine != null) {
      LOG.warn(message + " Trying " + defaultEngine.getId());
      return defaultEngine;
    }
    throw new ExecutionException(message);
  }

}
