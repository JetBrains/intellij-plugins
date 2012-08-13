package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.ServerShutdownAction;
import com.google.jstestdriver.ServerStartupAction;
import com.google.jstestdriver.idea.icons.JstdIcons;
import com.google.jstestdriver.idea.server.JstdServerState;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

/**
* @author Sergey Simonchik
*/
public class ServerStopAction extends AnAction {

  private static final Logger LOG = Logger.getInstance(ServerStopAction.class);

  ServerStopAction() {
    super("Stop the local server", null, JstdIcons.getIcon("stopLocalServer.png"));
  }

  @Override
  public void update(AnActionEvent e) {
    JstdServerState jstdServerState = JstdServerState.getInstance();
    e.getPresentation().setEnabled(jstdServerState.isServerRunning());
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    ServerStartupAction serverStartupAction = ServerStartAction.ACTIVE_SERVER_STARTUP_ACTION;
    if (serverStartupAction != null) {
      runStopAction(serverStartupAction);
      ServerStartAction.ACTIVE_SERVER_STARTUP_ACTION = null;
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
