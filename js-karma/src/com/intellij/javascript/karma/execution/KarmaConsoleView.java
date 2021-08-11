// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
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
import com.intellij.execution.ui.layout.LayoutAttractionPolicy;
import com.intellij.execution.ui.layout.LayoutViewOptions;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.javascript.debugger.JSDebugTabLayouter;
import com.intellij.javascript.debugger.JavaScriptDebugProcess;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.Disposer;
import com.intellij.ui.content.Content;
import com.intellij.util.Alarm;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.debugger.connection.VmConnection;

public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx {

  private static final Logger LOG = Logger.getInstance(KarmaConsoleView.class);
  private static final String TEST_RUN_CONTENT_ID = ExecutionConsole.CONSOLE_CONTENT_ID;

  private final KarmaServer myServer;
  private final KarmaExecutionType myExecutionType;
  private final ProcessHandler myProcessHandler;

  public KarmaConsoleView(@NotNull TestConsoleProperties consoleProperties,
                          @NotNull KarmaServer server,
                          @NotNull KarmaExecutionType executionType,
                          @NotNull ProcessHandler processHandler) {
    super(consoleProperties);
    myServer = server;
    myExecutionType = executionType;
    myProcessHandler = processHandler;
  }

  @Override
  public void buildUi(@NotNull RunnerLayoutUi ui) {
    registerConsoleContent(ui);
    registerKarmaServerTab(ui);
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
                                                    KarmaBundle.message("console.test_run_tab.name"),
                                                    null,
                                                    getPreferredFocusableComponent());
    ui.addContent(consoleContent, 1, PlaceInGrid.bottom, false);

    consoleContent.setCloseable(false);
    if (!myServer.areBrowsersReady()) {
      registerPrintingBrowserCapturingSuggestion();
    }
    if (myProcessHandler instanceof NopProcessHandler) {
      KarmaRootTestProxyFormatter rootFormatter = new KarmaRootTestProxyFormatter(this);
      myProcessHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, KarmaConsoleView.this);
          alarm.addRequest(() -> {
            rootFormatter.onTestRunProcessTerminated();
            Disposer.dispose(alarm);
          }, 200);
        }
      }, this);
    }
    myServer.getProcessHandler().addProcessListener(new ProcessAdapter() {
      @Override
      public void processTerminated(@NotNull ProcessEvent event) {
        if (!myProcessHandler.isProcessTerminated()) {
          print("Karma server terminated\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        }
      }
    }, this);
    return consoleContent;
  }

  private void registerPrintingBrowserCapturingSuggestion() {
    if (myExecutionType == KarmaExecutionType.DEBUG) {
      return;
    }
    myServer.onPortBound(() -> {
      if (Disposer.isDisposed(this)) {
        return;
      }
      Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, this);
      alarm.addRequest(() -> {
        if (!myProcessHandler.isProcessTerminated()) {
          getResultsViewer().getTestsRootNode().addLast(printer -> {
            printer.print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
            String url = myServer.formatUrl("/");
            printer.printHyperlink(url, new OpenUrlHyperlinkInfo(url));
            printer.print("\n", ConsoleViewContentType.SYSTEM_OUTPUT);
          });
        }
        Disposer.dispose(alarm);
      }, 1000, ModalityState.any());
      myServer.onBrowsersReady(() -> Disposer.dispose(alarm));
    });
  }

  private void registerKarmaServerTab(@NotNull RunnerLayoutUi ui) {
    KarmaServerLogComponent.register(getProperties().getProject(), myServer, ui);
    if (myServer.areBrowsersReady()) {
      selectContentId(ui, TEST_RUN_CONTENT_ID);
    }
    else {
      selectContentId(ui, KarmaServerLogComponent.KARMA_SERVER_CONTENT_ID);
      myServer.onBrowsersReady(() -> selectContentId(ui, TEST_RUN_CONTENT_ID));
    }
  }

  private static void selectContentId(@NotNull RunnerLayoutUi ui, @NotNull String contentId) {
    if (!ui.isDisposed()) {
      Content content = ui.findContent(contentId);
      if (content != null) {
        ui.selectAndFocus(content, false, false);
      }
      else {
        LOG.warn("Cannot find content for " + contentId);
      }
    }
  }

  @NotNull
  public KarmaServer getKarmaServer() {
    return myServer;
  }

  public JSDebugTabLayouter createDebugLayouter(@NotNull JavaScriptDebugProcess<?> debugProcess) {
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
    Class<?> consoleClass = console != null ? console.getClass() : null;
    LOG.info("Cannot cast " + consoleClass + " to " + KarmaConsoleView.class.getSimpleName() +
             ", RunProfileState: " + state.getClass().getName());
    return null;
  }

  private static final class KarmaRootTestProxyFormatter implements SMRootTestProxyFormatter {

    private final TestTreeView myTreeView;
    private boolean myTestRunProcessTerminated = false;

    private KarmaRootTestProxyFormatter(@NotNull SMTRunnerConsoleView consoleView) {
      myTreeView = consoleView.getResultsViewer().getTreeView();
      if (myTreeView != null) {
        TestTreeRenderer originalRenderer = ObjectUtils.tryCast(myTreeView.getCellRenderer(), TestTreeRenderer.class);
        if (originalRenderer != null) {
          originalRenderer.setAdditionalRootFormatter(this);
        }
      }
    }

    private static void render(@NotNull TestTreeRenderer renderer, @NotNull @Nls String msg) {
      renderer.clear();
      renderer.append(msg);
    }

    @Override
    public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
      if (testProxy.isLeaf()) {
        render(renderer, myTestRunProcessTerminated ? KarmaBundle.message("test.run.process_terminated.text")
                                                    : KarmaBundle.message("test.run.waiting_for_browser_capturing.text"));
      }
    }

    private void onTestRunProcessTerminated() {
      myTestRunProcessTerminated = true;
      myTreeView.repaint();
    }
  }

  private class KarmaDebugTabLayouter extends JSDebugTabLayouter {

    KarmaDebugTabLayouter(@NotNull JavaScriptDebugProcess<? extends VmConnection> debugProcess) {
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
      registerKarmaServerTab(ui);
      // Overwrite "initFocusContent(DebuggerContentInfo.CONSOLE_CONTENT, LayoutViewOptions.STARTUP, ...)
      // from com.intellij.xdebugger.impl.ui.DebuggerSessionTabBase()
      ui.getDefaults().initContentAttraction(myServer.areBrowsersReady() ? ExecutionConsole.CONSOLE_CONTENT_ID
                                                                         : KarmaServerLogComponent.KARMA_SERVER_CONTENT_ID,
                                             LayoutViewOptions.STARTUP,
                                             new LayoutAttractionPolicy.FocusOnce(false));
    }
  }
}
