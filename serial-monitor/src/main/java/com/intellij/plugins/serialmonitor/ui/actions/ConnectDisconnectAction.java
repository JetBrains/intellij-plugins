package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.console.SerialConnectable;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry_Cherkas
 */
public class ConnectDisconnectAction extends ToggleAction implements DumbAware, ActionUpdateThreadAware {

  private final @NotNull SerialConnectable<?> myConnectable;

  public ConnectDisconnectAction(@NotNull SerialConnectable<?> serialConnectable) {
    super(SerialMonitorBundle.messagePointer("connect.title"),
          SerialMonitorBundle.messagePointer("connect.tooltip"), SerialMonitorIcons.ConnectedSerial);
    myConnectable = serialConnectable;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return myConnectable.isConnected();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean doConnect) {
    myConnectable.openConnectionTab(doConnect);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();

    if (myConnectable.isLoading()) {
      presentation.setEnabled(false);
      return;
    }
    Project project = e.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(true);

    if (myConnectable.isConnected()) {
      // validate disconnect action
      presentation.setText(SerialMonitorBundle.messagePointer("disconnect.title"));
      presentation.setIcon(SerialMonitorIcons.ConnectedSerial);
    }
    else {
      presentation.setIcon(SerialMonitorIcons.DisconnectedSerial);
      // validate Connect action
      if (myConnectable.isPortValid()) {
        presentation.setText(SerialMonitorBundle.messagePointer("connect.title"));
      }
      else {
        presentation.setText(SerialMonitorBundle.messagePointer("connect-invalid-settings.title"));
      }
    }
  }
}
