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
import com.intellij.javascript.debugger.impl.BrowserConnection;
import com.intellij.javascript.debugger.impl.JSDebugLayouter;
import com.intellij.javascript.debugger.impl.JSDebugProcess;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.openapi.application.ModalityState;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Sergey Simonchik
*/
public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx {

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
    registerConsoleContent(ui);
    registerAdditionalContent(ui);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @NotNull
  private Content registerConsoleContent(@NotNull final RunnerLayoutUi ui) {
    ui.getOptions().setMinimizeActionEnabled(false);
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
    if (myServer.areBrowsersReady()) {
      ui.selectAndFocus(consoleContent, false, false);
    }
    else {
      myServer.onPortBound(new Runnable() {
        @Override
        public void run() {
          ui.selectAndFocus(consoleContent, false, false);
        }
      });
      final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, consoleContent);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          if (myServer.isPortBound() && !myServer.areBrowsersReady()) {
            print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
            String url = myServer.formatUrl("/");
            printHyperlink(url + "\n", new OpenUrlHyperlinkInfo(url));
          }
        }
      }, 1000, ModalityState.any());
      myServer.onBrowsersReady(new Runnable() {
        @Override
        public void run() {
          alarm.cancelAllRequests();
        }
      });
      myServer.onTerminated(new KarmaServerTerminatedListener() {
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

  private void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(getProperties().getProject(), myServer, this);
    logComponent.installOn(ui, true);
  }

  @NotNull
  public KarmaExecutionSession getKarmaExecutionSession() {
    return myExecutionSession;
  }

  public <C extends BrowserConnection> KarmaDebugLayouter<C> createDebugLayouter(@NotNull JSDebugProcess<C> debugProcess) {
    return new KarmaDebugLayouter<C>(debugProcess);
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
          if (myServer.isPortBound() && !myServer.areBrowsersReady()) {
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

  private class KarmaDebugLayouter<C extends BrowserConnection> extends JSDebugLayouter<C> {

    public KarmaDebugLayouter(@NotNull JSDebugProcess<C> debugProcess) {
      super(debugProcess);
    }

    @NotNull
    @Override
    public Content registerConsoleContent(@NotNull RunnerLayoutUi ui, @NotNull ExecutionConsole console) {
      return KarmaConsoleView.this.registerConsoleContent(ui);
    }

    @Override
    public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
      super.registerAdditionalContent(ui);
      KarmaConsoleView.this.registerAdditionalContent(ui);
    }
  }

}
