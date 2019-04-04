package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.google.jstestdriver.idea.server.JstdServerSettingsManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import org.jetbrains.annotations.NotNull;

public class JstdServerRestartAction extends AnAction {

  private final JstdToolWindowSession mySession;

  public JstdServerRestartAction(@NotNull JstdToolWindowSession session) {
    mySession = session;
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    JstdServer runningServer = JstdServerRegistry.getInstance().getServer();
    Presentation presentation = e.getPresentation();
    if (runningServer != null && runningServer.isProcessRunning()) {
      presentation.setIcon(AllIcons.Actions.Restart);
      presentation.setText("Rerun local server");
    }
    else {
      presentation.setIcon(AllIcons.Actions.Execute);
      presentation.setText("Start a local server");
    }
    e.getPresentation().setEnabled(true);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    mySession.saveSettings();
    mySession.restart(JstdServerSettingsManager.loadSettings());
  }
}
