package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.plugins.serialmonitor.service.PortStatus;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.console.JeditermSerialMonitorDuplexConsoleView;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas
 */
public class ConnectDisconnectAction extends ToggleAction implements DumbAware, ActionUpdateThreadAware {

  private final @NotNull JeditermSerialMonitorDuplexConsoleView myConsoleView;

  public ConnectDisconnectAction(@NotNull JeditermSerialMonitorDuplexConsoleView consoleView) {
    super(SerialMonitorBundle.messagePointer("connect.title"),
          SerialMonitorBundle.messagePointer("connect.tooltip"), SerialMonitorIcons.ConnectedSerial);
    myConsoleView = consoleView;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.EDT;
  }

  @Override
  public boolean isSelected(@NotNull AnActionEvent e) {
    return myConsoleView.getStatus() == PortStatus.CONNECTED;
  }

  @Override
  public void setSelected(@NotNull AnActionEvent e, boolean doConnect) {
    myConsoleView.connect(doConnect);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();

    if (myConsoleView.isLoading()) {
      presentation.setEnabled(false);
      return;
    }
    Project project = e.getProject();
    if (project == null) {
      presentation.setEnabled(false);
      return;
    }

    presentation.setEnabled(true);

    PortStatus status = myConsoleView.getStatus();
    Icon icon = null;
    @NlsContexts.HintText String text = null;
    boolean enabled = true;
    switch (status) {
      case MISSING:
        icon = PortStatus.MISSING.getStatusIcon();
        text = SerialMonitorBundle.message("connect-invalid-settings.title");
        break;
      case BUSY:
        icon = PortStatus.CONNECTING.getStatusIcon();
        break;
      case DISCONNECTED:
        icon = SerialMonitorIcons.ConnectedSerial;
        text = SerialMonitorBundle.message("connect.title");
        break;
      case CONNECTING:
        icon = PortStatus.CONNECTING.getActionIcon();
        enabled = false;
        break;
      case CONNECTED:
        icon = SerialMonitorIcons.DisconnectedSerial;
        text = SerialMonitorBundle.message("disconnect.title");
    }
    presentation.setIcon(icon);
    presentation.setText(text);
    presentation.setEnabled(enabled);
  }
}
