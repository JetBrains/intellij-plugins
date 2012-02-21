package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.ServerShutdownAction;
import com.google.jstestdriver.ServerStartupAction;
import com.google.jstestdriver.idea.icons.JstdIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
* @author Sergey Simonchik
*/
public class ServerStopAction extends AnAction {

  private final JstdServerState myServerState;

  ServerStopAction(JstdServerState serverState) {
    super("Stop the local server", null, JstdIcons.getIcon("stopLocalServer.png"));
    myServerState = serverState;
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setEnabled(myServerState.isServerRunning());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    ServerStartupAction serverStartupAction = ToolPanel.myServerStartupAction;
    if (serverStartupAction != null) {
      new ServerShutdownAction(serverStartupAction).run(null);
      ToolPanel.myServerStartupAction = null;
    }
  }
}
