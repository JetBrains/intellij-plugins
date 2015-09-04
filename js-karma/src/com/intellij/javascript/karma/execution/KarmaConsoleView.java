package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.testframework.*;
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
import com.intellij.javascript.debugger.impl.JSDebugTabLayouter;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.javascript.karma.server.KarmaServerTerminatedListener;
import com.intellij.javascript.karma.util.KarmaUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import com.jetbrains.javascript.debugger.JavaScriptDebugProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.connection.VmConnection;

public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx {

  private static final Logger LOG = Logger.getInstance(KarmaConsoleView.class);

  private final KarmaServer myServer;
  private final KarmaExecutionSession myExecutionSession;

  public KarmaConsoleView(@NotNull TestConsoleProperties consoleProperties,
                          @NotNull KarmaServer server,
                          @NotNull KarmaExecutionSession executionSession) {
    super(consoleProperties);
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
      KarmaUtil.selectAndFocusIfNotDisposed(ui, consoleContent, false, false);
    }
    else {
      myServer.onPortBound(new Runnable() {
        @Override
        public void run() {
          KarmaUtil.selectAndFocusIfNotDisposed(ui, consoleContent, false, false);
          scheduleBrowserCapturingSuggestion(consoleContent);
        }
      });
    }
    myServer.onTerminated(new KarmaServerTerminatedListener() {
      @Override
      public void onTerminated(int exitCode) {
        rootFormatter.onServerProcessTerminated();
        printServerFinishedInfo(exitCode);
      }
    });
    myExecutionSession.getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(ProcessEvent event) {
        rootFormatter.onTestRunProcessTerminated();
      }
    });
    return consoleContent;
  }

  private void scheduleBrowserCapturingSuggestion(@Nullable Disposable parentDisposable) {
    final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, parentDisposable);
    alarm.addRequest(new Runnable() {
      @Override
      public void run() {
        if (!myServer.areBrowsersReady()) {
          printBrowserCapturingSuggestion();
        }
        Disposer.dispose(alarm);
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
      }
    });
  }

  private void printBrowserCapturingSuggestion() {
    SMTestProxy.SMRootTestProxy rootNode = getResultsViewer().getTestsRootNode();
    rootNode.addLast(new Printable() {
      @Override
      public void printOn(Printer printer) {
        printer.print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
        String url = myServer.formatUrl("/");
        printer.printHyperlink(url, new OpenUrlHyperlinkInfo(url));
        printer.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT);
      }
    });
  }

  private void printServerFinishedInfo(int exitCode) {
    SMTestProxy.SMRootTestProxy rootNode = getResultsViewer().getTestsRootNode();
    rootNode.addSystemOutput("Karma server finished with exit code " + exitCode + "\n");
  }

  private void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(getProperties().getProject(), myServer, this);
    logComponent.installOn(ui, true);
  }

  @NotNull
  public KarmaExecutionSession getKarmaExecutionSession() {
    return myExecutionSession;
  }

  public KarmaDebugTabLayouter createDebugLayouter(@NotNull JavaScriptDebugProcess<? extends VmConnection> debugProcess) {
    return new KarmaDebugTabLayouter(debugProcess);
  }

  /**
   * @return null in case of "Import Test Result" action
   */
  @Nullable
  public static KarmaConsoleView get(@NotNull ExecutionResult result, @NotNull RunProfileState state) {
    ExecutionConsole console = result.getExecutionConsole();
    if (console instanceof KarmaConsoleView) {
      return (KarmaConsoleView)console;
    }
    Class consoleClass = console != null ? console.getClass() : null;
    LOG.info("Cannot cast " + consoleClass + " to " + KarmaConsoleView.class.getSimpleName() +
             ", RunProfileState: " + state.getClass().getName());
    return null;
  }

  private static class KarmaRootTestProxyFormatter implements SMRootTestProxyFormatter {

    private final KarmaServer myServer;
    private final TestTreeView myTreeView;
    private final boolean myDebug;
    private boolean myTestRunProcessTerminated = false;
    private boolean myServerProcessTerminated = false;

    private KarmaRootTestProxyFormatter(@NotNull SMTRunnerConsoleView consoleView,
                                        @NotNull KarmaServer server,
                                        boolean debug) {
      myTreeView = consoleView.getResultsViewer().getTreeView();
      myServer = server;
      myDebug = debug;
      if (myTreeView != null) {
        TestTreeRenderer originalRenderer = ObjectUtils.tryCast(myTreeView.getCellRenderer(), TestTreeRenderer.class);
        if (originalRenderer != null) {
          originalRenderer.setAdditionalRootFormatter(this);
        }
      }
    }

    private static void render(@NotNull TestTreeRenderer renderer, @NotNull String msg, boolean error) {
      renderer.clear();
      if (error) {
        renderer.setIcon(PoolOfTestIcons.TERMINATED_ICON);
      }
      renderer.append(msg);
    }

    @Override
    public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
      if (testProxy.isLeaf()) {
        if (myServerProcessTerminated) {
          render(renderer, "Server is dead", true);
        }
        else {
          if (myDebug) {
            render(renderer, "Test tree is not available in a debug session", false);
          }
          else if (myTestRunProcessTerminated) {
            render(renderer, "Aborted", true);
          }
          else if (myServer.isPortBound() && !myServer.areBrowsersReady()) {
            render(renderer, "Waiting for browser capturing...", false);
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

  private class KarmaDebugTabLayouter extends JSDebugTabLayouter {

    public KarmaDebugTabLayouter(@NotNull JavaScriptDebugProcess<? extends VmConnection> debugProcess) {
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
