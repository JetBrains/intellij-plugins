package com.google.jstestdriver.idea.execution;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerLifeCycleAdapter;
import com.google.jstestdriver.idea.server.ui.JstdToolWindowManager;
import com.intellij.execution.process.NopProcessHandler;
import com.intellij.execution.process.ProcessAdapter;
import com.intellij.execution.process.ProcessEvent;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.PoolOfTestIcons;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.TestTreeView;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMRootTestProxyFormatter;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.TestTreeRenderer;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.ide.browsers.OpenUrlHyperlinkInfo;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JstdConsoleView extends SMTRunnerConsoleView {

  private final JstdServer myServer;
  private JstdRootTestProxyFormatter myFormatter;

  public JstdConsoleView(@NotNull TestConsoleProperties consoleProperties,
                         @NotNull ExecutionEnvironment env,
                         @Nullable String splitterProperty,
                         @Nullable JstdServer server) {
    super(consoleProperties);
    myServer = server;
  }

  @Override
  public void initUI() {
    super.initUI();
    if (myServer == null) {
      return;
    }
    TestTreeView treeView = this.getResultsViewer().getTreeView();
    TestTreeRenderer originalRenderer = ObjectUtils.tryCast(treeView.getCellRenderer(), TestTreeRenderer.class);
    if (originalRenderer != null) {
      myFormatter = new JstdRootTestProxyFormatter(myServer, treeView);
      originalRenderer.setAdditionalRootFormatter(myFormatter);
    }
    if (!myServer.isStopped() && myServer.getCapturedBrowsers().isEmpty()) {
      myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
        @Override
        public void onServerStarted() {
          print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
          String url = myServer.getServerUrl() + "/capture";
          printHyperlink(url, new OpenUrlHyperlinkInfo(url));
          print("\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        }
      }, this);
    }
    myServer.addLifeCycleListener(new JstdServerLifeCycleAdapter() {
      @Override
      public void onServerTerminated(int exitCode) {
        print("JsTestDriver server finished with exit code " + exitCode + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
        JstdToolWindowManager.getInstance(getProperties().getProject()).show();
      }
    }, this);

  }

  @Override
  public void attachToProcess(ProcessHandler processHandler) {
    super.attachToProcess(processHandler);
    if (processHandler instanceof NopProcessHandler) {
      processHandler.addProcessListener(new ProcessAdapter() {
        @Override
        public void processTerminated(@NotNull ProcessEvent event) {
          if (myFormatter != null) {
            myFormatter.onTestRunProcessTerminated();
          }
        }
      });
    }
  }

  private static class JstdRootTestProxyFormatter implements SMRootTestProxyFormatter {

    private final TestTreeView myTestTreeView;
    private final JstdServer myServer;
    private boolean myTestRunProcessTerminated = false;

    private JstdRootTestProxyFormatter(@NotNull JstdServer server, @NotNull TestTreeView testTreeView) {
      myServer = server;
      myTestTreeView = testTreeView;
    }

    @Override
    public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
      if (!testProxy.isLeaf()) {
        return;
      }
      if (myTestRunProcessTerminated) {
        render(renderer, "Aborted", true);
      }
      else if (myServer.isProcessRunning()) {
        if (!myServer.isReadyForCapturing()) {
          if (!myServer.isStopped()) {
            render(renderer, "Starting up server...", false);
          }
        }
        else {
          render(renderer, "Waiting for captured browser...", false);
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

    private void onTestRunProcessTerminated() {
      myTestRunProcessTerminated = true;
      myTestTreeView.repaint();
    }
  }
}
