package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.SMTestProxy;
import com.intellij.execution.testframework.sm.runner.ui.SMRootTestProxyFormatter;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.testframework.sm.runner.ui.TestTreeRenderer;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.ui.content.Content;
import com.intellij.util.ObjectUtils;
import com.intellij.xdebugger.ui.XDebugLayoutCustomizer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
* @author Sergey Simonchik
*/
public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx, XDebugLayoutCustomizer {

  private final KarmaServer myKarmaServer;
  private final KarmaExecutionSession myExecutionSession;

  public KarmaConsoleView(@NotNull TestConsoleProperties consoleProperties,
                          @NotNull ExecutionEnvironment env,
                          @Nullable String splitterProperty,
                          @NotNull KarmaServer karmaServer,
                          @NotNull KarmaExecutionSession executionSession) {
    super(consoleProperties, env.getRunnerSettings(), env.getConfigurationSettings(), splitterProperty);
    myKarmaServer = karmaServer;
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
    boolean readyToRun = myKarmaServer.isReady() && myKarmaServer.hasCapturedBrowsers();
    final Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                                    getComponent(),
                                                    "Test Run",
                                                    AllIcons.Debugger.Console,
                                                    getPreferredFocusableComponent());
    ui.addContent(consoleContent, 1, PlaceInGrid.bottom, false);

    consoleContent.setCloseable(false);
    if (readyToRun) {
      ui.selectAndFocus(consoleContent, false, false);
    }
    else {
      final TestTreeRenderer testTreeRenderer = ObjectUtils.tryCast(
        getResultsViewer().getTreeView().getCellRenderer(),
        TestTreeRenderer.class
      );
      if (testTreeRenderer != null) {
        testTreeRenderer.setAdditionalRootFormatter(new SMRootTestProxyFormatter() {
          @Override
          public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
            if (testProxy.isLeaf()) {
              renderer.clear();
              renderer.append("Waiting for browser capturing...");
            }
          }
        });
      }
      myKarmaServer.doWhenReadyWithCapturedBrowser(new Runnable() {
        @Override
        public void run() {
          ui.selectAndFocus(consoleContent, false, false);
          if (testTreeRenderer != null) {
            testTreeRenderer.removeAdditionalRootFormatter();
          }
        }
      });
    }
    return consoleContent;
  }

  @Override
  public void registerAdditionalContent(@NotNull RunnerLayoutUi ui) {
    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(getProperties().getProject(), myKarmaServer);
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

}
