package com.jetbrains.lang.dart.pubServer;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;

public class StopPubServerAction extends DumbAwareAction {

  public StopPubServerAction() {
    super(DartBundle.message("stop.pub.server.action.text"),
          DartBundle.message("stop.pub.server.action.description"),
          AllIcons.Actions.Suspend);
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabled(e.getProject() != null && PubServerManager.getInstance(e.getProject()).hasAlivePubServerProcesses());
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    if (e.getProject() == null) return;

    // todo instead of killing the process just close webSocket connection to the Pub Serve admin port
    PubServerManager.getInstance(e.getProject()).stopAllPubServerProcesses();
  }
}
