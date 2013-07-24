package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.TestTreeView;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMRootTestProxyFormatter;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.TestTreeRenderer;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.javascript.karma.server.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.intellij.xdebugger.ui.XDebugLayoutCustomizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Sergey Simonchik
*/
public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx, XDebugLayoutCustomizer {

  private final KarmaServer myServer;
  private final KarmaExecutionSession myExecutionSession;

  public KarmaConsoleView(@NotNull TestConsoleProperties consoleProperties,
                          @NotNull ExecutionEnvironment env,
                          @Nullable String splitterProperty,
                          @NotNull KarmaServer server,
                          @NotNull KarmaExecutionSession executionSession) {
    super(consoleProperties, env, splitterProperty);
    myServer = server;
    myExecutionSession = executionSession;
  }

  @Override
  public void buildUi(final RunnerLayoutUi ui) {
    registerConsoleContent(this, ui);
    registerAdditionalContent(ui);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @NotNull
  @Override
  public Content registerConsoleContent(@NotNull ExecutionConsole console, @NotNull final RunnerLayoutUi ui) {
    ui.getOptions().setMinimizeActionEnabled(false);
    boolean readyToRun = myServer.isReady() && myServer.hasCapturedBrowsers();
    final Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                                    getComponent(),
                                                    "Test Run",
                                                    AllIcons.Debugger.Console,
                                                    getPreferredFocusableComponent());
    ui.addContent(consoleContent, 1, PlaceInGrid.bottom, false);

    consoleContent.setCloseable(false);
    final KarmaRootTestProxyFormatter rootFormatter = new KarmaRootTestProxyFormatter(this,
                                                                                      myServer,
                                                                                      myExecutionSession.isDebug());
    if (readyToRun) {
      ui.selectAndFocus(consoleContent, false, false);
    }
    else {
      myServer.doWhenReady(new KarmaServerReadyListener() {
        @Override
        public void onReady(int webServerPort, int runnerPort) {
          ui.selectAndFocus(consoleContent, false, false);
        }
      });
      final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, consoleContent);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          if (myServer.isReady() && !myServer.hasCapturedBrowsers()) {
            print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
            String url = "http://localhost:" + myServer.getWebServerPort();
            printHyperlink(url + "\n", new OpenUrlHyperlinkInfo(url));
          }
        }
      }, 1000, ModalityState.any());
      myServer.doWhenReadyWithCapturedBrowser(new Runnable() {
        @Override
        public void run() {
          alarm.cancelAllRequests();
        }
      });
      myServer.doWhenTerminated(new KarmaServerTerminatedListener() {
        @Override
        public void onTerminated(int exitCode) {
          alarm.cancelAllRequests();
          rootFormatter.onServerProcessTerminated();
          print("Karma server finished with exited code " + exitCode + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        }
      });
    }
    myExecutionSession.getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        rootFormatter.onTestRunProcessTerminated();
      }
    });
    return consoleContent;
  }

  @Override
  public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(getProperties().getProject(), myServer);
    logComponent.installOn(ui);
  }

  @NotNull
  public KarmaExecutionSession getKarmaExecutionSession() {
    return myExecutionSession;
  }

  @Nullable
  public static KarmaConsoleView get(@NotNull ExecutionResult result) {
    return ObjectUtils.tryCast(result.getExecutionConsole(), KarmaConsoleView.class);
  }

  private static class KarmaRootTestProxyFormatter implements SMRootTestProxyFormatter {

    private final TestTreeRenderer myRenderer;
    private final KarmaServer myServer;
    private final TestTreeView myTreeView;
    private final boolean myDebug;
    private boolean myTestRunProcessTerminated = false;
    private boolean myServerProcessTerminated = false;

    private KarmaRootTestProxyFormatter(@NotNull SMTRunnerConsoleView consoleView,
                                        @NotNull KarmaServer server,
                                        boolean debug) {
      myTreeView = consoleView.getResultsViewer().getTreeView();
      myRenderer = ObjectUtils.tryCast(myTreeView.getCellRenderer(), TestTreeRenderer.class);
      if (myRenderer != null) {
        myRenderer.setAdditionalRootFormatter(this);
      }
      myServer = server;
      myDebug = debug;
    }

    @Override
    public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
      if (testProxy.isLeaf()) {
        if (myDebug) {
          renderer.clear();
          renderer.append("Test tree is not available in a debug session");
        }
        if (!myTestRunProcessTerminated && !myServerProcessTerminated) {
          if (myServer.isReady() && !myServer.hasCapturedBrowsers()) {
            renderer.clear();
            renderer.append("Waiting for browser capturing...");
          }
        }
      }
    }

    private void onTestRunProcessTerminated() {
      myTestRunProcessTerminated = true;
      myTreeView.repaint();
    }

    private void onServerProcessTerminated() {
      myServerProcessTerminated = true;
      myTreeView.repaint();
    }
  }

}
