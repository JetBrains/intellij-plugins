package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
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
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerAdapter;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
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
    final KarmaRootTestProxyFormatter rootFormatter = new KarmaRootTestProxyFormatter(this);
    if (readyToRun) {
      ui.selectAndFocus(consoleContent, false, false);
    }
    else {
      myKarmaServer.addListener(new KarmaServerAdapter() {
        @Override
        public void onReady(int webServerPort, int runnerPort) {
          ui.selectAndFocus(consoleContent, false, false);
        }
      });
      final Alarm alarm = new Alarm(Alarm.ThreadToUse.SWING_THREAD, consoleContent);
      alarm.addRequest(new Runnable() {
        @Override
        public void run() {
          if (myKarmaServer.isReady() && !myKarmaServer.hasCapturedBrowsers()) {
            print("To capture a browser open ", ConsoleViewContentType.SYSTEM_OUTPUT);
            String url = "http://localhost:" + myKarmaServer.getWebServerPort();
            printHyperlink(url + "\n", new OpenUrlHyperlinkInfo(url));
          }
        }
      }, 1000, ModalityState.any());
      myKarmaServer.doWhenReadyWithCapturedBrowser(new Runnable() {
        @Override
        public void run() {
          alarm.cancelAllRequests();
        }
      });
      myKarmaServer.addListener(new KarmaServerAdapter() {
        @Override
        public void onTerminated(int exitCode) {
          alarm.cancelAllRequests();
          rootFormatter.uninstall();
          print("Karma server finished with exited code " + exitCode + "\n", ConsoleViewContentType.SYSTEM_OUTPUT);
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

  private static class KarmaRootTestProxyFormatter implements SMRootTestProxyFormatter {

    private final TestTreeRenderer myTestTreeRenderer;

    private KarmaRootTestProxyFormatter(@NotNull SMTRunnerConsoleView consoleView) {
      myTestTreeRenderer = ObjectUtils.tryCast(
        consoleView.getResultsViewer().getTreeView().getCellRenderer(),
        TestTreeRenderer.class
      );
      if (myTestTreeRenderer != null) {
        myTestTreeRenderer.setAdditionalRootFormatter(this);
      }
    }

    @Override
    public void format(@NotNull SMTestProxy.SMRootTestProxy testProxy, @NotNull TestTreeRenderer renderer) {
      if (testProxy.isLeaf()) {
        renderer.clear();
        renderer.append("Waiting for browser capturing...");
      }
    }

    public void uninstall() {
      if (myTestTreeRenderer != null) {
        myTestTreeRenderer.removeAdditionalRootFormatter();
      }
    }
  }

}
