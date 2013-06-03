package com.intellij.javascript.karma.execution;

import com.intellij.execution.ExecutionResult;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.testframework.TestConsoleProperties;
import com.intellij.execution.testframework.sm.runner.ui.SMTRunnerConsoleView;
import com.intellij.execution.ui.ExecutionConsole;
import com.intellij.execution.ui.ExecutionConsoleEx;
import com.intellij.execution.ui.RunnerLayoutUi;
import com.intellij.execution.ui.layout.PlaceInGrid;
import com.intellij.icons.AllIcons;
import com.intellij.javascript.karma.server.KarmaServer;
import com.intellij.javascript.karma.server.KarmaServerAdapter;
import com.intellij.javascript.karma.server.KarmaServerLogComponent;
import com.intellij.ui.ColorUtil;
import com.intellij.ui.content.Content;
import com.intellij.util.ObjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

/**
* @author Sergey Simonchik
*/
public class KarmaConsoleView extends SMTRunnerConsoleView implements ExecutionConsoleEx {

  private final KarmaServer myKarmaServer;
  private final KarmaExecutionSession myRunSession;

  public KarmaConsoleView(@NotNull TestConsoleProperties consoleProperties,
                          @NotNull ExecutionEnvironment env,
                          @Nullable String splitterProperty,
                          @NotNull KarmaServer karmaServer,
                          @NotNull KarmaExecutionSession runSession) {
    super(consoleProperties, env.getRunnerSettings(), env.getConfigurationSettings(), splitterProperty);
    myKarmaServer = karmaServer;
    myRunSession = runSession;
  }

  @Override
  public void buildUi(final RunnerLayoutUi ui) {
    boolean serverReady = myKarmaServer.isReady();
    final JComponent component, preferredFocusableComponent;
    if (serverReady) {
      component = getComponent();
      preferredFocusableComponent = getPreferredFocusableComponent();
    }
    else {
      component = createStubComponent();
      preferredFocusableComponent = null;
    }
    final Content consoleContent = ui.createContent(ExecutionConsole.CONSOLE_CONTENT_ID,
                                                    component,
                                                    "Test Run",
                                                    AllIcons.Debugger.Console,
                                                    preferredFocusableComponent);
    consoleContent.setCloseable(false);
    ui.addContent(consoleContent, 0, PlaceInGrid.bottom, false);
    if (serverReady) {
      ui.selectAndFocus(consoleContent, false, false);
    }

    if (!serverReady) {
      myKarmaServer.addListener(new KarmaServerAdapter() {
        @Override
        public void onReady(int webServerPort, int runnerPort) {
          ui.removeContent(consoleContent, false);
          consoleContent.setComponent(getComponent());
          consoleContent.setPreferredFocusableComponent(getPreferredFocusableComponent());
          ui.addContent(consoleContent, 0, PlaceInGrid.bottom, false);
          ui.selectAndFocus(consoleContent, false, false);
        }
      });
    }

    KarmaServerLogComponent logComponent = new KarmaServerLogComponent(getProperties().getProject(), myKarmaServer);
    logComponent.installOn(ui);
  }

  @Nullable
  @Override
  public String getExecutionConsoleId() {
    return null;
  }

  @NotNull
  private static JComponent createStubComponent() {
    JLabel label = new JLabel("Karma server is not ready", SwingConstants.CENTER);
    label.setOpaque(true);
    Color treeBg = UIManager.getColor("Tree.background");
    label.setBackground(ColorUtil.toAlpha(treeBg, 180));
    return label;
  }

  @NotNull
  public KarmaExecutionSession getKarmaRunSession() {
    return myRunSession;
  }

  @Nullable
  public static KarmaConsoleView get(@NotNull ExecutionResult result) {
    return ObjectUtils.tryCast(result.getExecutionConsole(), KarmaConsoleView.class);
  }
}
