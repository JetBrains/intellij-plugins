package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.ServerShutdownAction;
import com.google.jstestdriver.ServerStartupAction;
import com.google.jstestdriver.idea.icons.JstdIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
* @author Sergey Simonchik
*/
public class ServerStopAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ServerStopAction.class);

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
      runStopAction(serverStartupAction);
      ToolPanel.myServerStartupAction = null;
    }
  }

  public static void runStopAction(@NotNull ServerStartupAction serverStartupAction) {
    try {
      new ServerShutdownAction(serverStartupAction).run(null);
    } catch (Exception e) {
      LOG.warn(e);
    }
  }
}
