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
import com.intellij.javascript.debugger.RemoteDebuggingFileFinder;
import com.intellij.javascript.karma.KarmaConfig;
import com.intellij.javascript.karma.execution.KarmaConsoleView;
import com.intellij.javascript.karma.execution.KarmaRunConfiguration;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.lang.javascript.modules.NodeModuleUtil;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.intellij.util.Url;
import com.intellij.util.Urls;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.xdebugger.XDebugProcess;
import com.intellij.xdebugger.XDebugProcessStarter;
import com.intellij.xdebugger.XDebugSession;
import com.intellij.xdebugger.XDebuggerManager;
import kotlin.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.concurrency.Promise;
import org.jetbrains.debugger.connection.VmConnection;
import org.jetbrains.wip.WipVm;
import org.jetbrains.wip.protocol.runtime.CallFrameValue;
import org.jetbrains.wip.protocol.runtime.ConsoleAPICalledEventData;
import org.jetbrains.wip.protocol.runtime.ConsoleAPICalledEventDataType;
import org.jetbrains.wip.protocol.runtime.StackTraceValue;

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
                listenForCompletedMessage(debugProcess, karmaServer);
                return debugProcess;
              }
            }
          );
          return session.getRunContentDescriptor();
        }
      });
    }
    else {
      return Promise.resolve(new RunProfileStarter() {
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

  private static void listenForCompletedMessage(@NotNull JavaScriptDebugProcess<? extends VmConnection> debugProcess,
                                                @NotNull KarmaServer karmaServer) {
    String debugJsFileUrl = karmaServer.formatUrl("/debug.js");
    debugProcess.getConnection().stateChanged(state -> {
      WipVm vm = ObjectUtils.tryCast(debugProcess.getVm(), WipVm.class);
      if (vm != null) {
        vm.getCommandProcessor().getEventMap().add(ConsoleAPICalledEventData.TYPE, data -> {
          if (data.type() == ConsoleAPICalledEventDataType.LOG) {
            StackTraceValue trace = data.getStackTrace();
            if (trace != null) {
              CallFrameValue frame = ContainerUtil.getFirstItem(trace.callFrames());
              if (frame != null && "window.__karma__.complete".equals(frame.functionName()) &&
                  debugJsFileUrl.equals(frame.url())) {
                // postpone closing connection to let "Skipped <N> test" message be printed in console
                new Alarm(Alarm.ThreadToUse.SWING_THREAD).addRequest(() -> debugProcess.getConnection().detachAndClose(), 500);
              }
            }
          }
          return Unit.INSTANCE;
        });
      }
      return Unit.INSTANCE;
    });
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

  public static Promise<RunProfileStarter> prepareDebugger(@NotNull Project project,
                                                           @NotNull DebuggableWebBrowser debuggableWebBrowser,
                                                           @NotNull final RunProfileStarter starter) {
    return debuggableWebBrowser.getDebugEngine().prepareDebugger(project, debuggableWebBrowser.getWebBrowser())
      .then(aVoid -> starter);
  }
}
