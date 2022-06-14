package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.actionSystem.UpdateInBackground;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.console.SerialMonitorDuplexConsoleView;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry_Cherkas
 */
public class ConnectDisconnectAction extends ToggleAction implements DumbAware, UpdateInBackground {

  private final @NotNull SerialMonitorDuplexConsoleView mySerialConsoleView;

  public ConnectDisconnectAction(@NotNull SerialMonitorDuplexConsoleView serialConsoleView) {
    super(SerialMonitorBundle.messagePointer("connect.title"),
          SerialMonitorBundle.messagePointer("connect.tooltip"), SerialMonitorIcons.ConnectedSerial);
    mySerialConsoleView = serialConsoleView;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return mySerialConsoleView.isConnected();
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean doConnect) {
    mySerialConsoleView.openConnectionTab(doConnect);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    if (mySerialConsoleView.isLoading()) {
      presentation.setEnabled(false);
      return;
    }
    Project project = e.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(true);

    if (mySerialConsoleView.isConnected()) {
      // validate disconnect action
      presentation.setText(SerialMonitorBundle.messagePointer("disconnect.title"));
    }
    else {
      // validate Connect action
      if (mySerialConsoleView.isPortValid()) {
        presentation.setText(SerialMonitorBundle.messagePointer("connect.title"));
      }
      else {
        presentation.setText(SerialMonitorBundle.messagePointer("connect-invalid-settings.title"));
      }
    }
  }
}
