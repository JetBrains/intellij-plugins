package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerRegistry;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class JstdServerStopAction extends AnAction {

  public JstdServerStopAction() {
    super("Stop the local server", null, AllIcons.Actions.Suspend);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    e.getPresentation().setEnabled(server != null && server.isProcessRunning());
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    JstdServer server = JstdServerRegistry.getInstance().getServer();
    if (server != null) {
      server.shutdownAsync();
    }
  }
}
