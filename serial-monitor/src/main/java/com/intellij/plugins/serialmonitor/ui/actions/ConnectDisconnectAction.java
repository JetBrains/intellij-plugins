package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.util.NlsActions;
import com.intellij.plugins.serialmonitor.service.PortStatus;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.console.JeditermSerialMonitorDuplexConsoleView;
import icons.SerialMonitorIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class ConnectDisconnectAction extends ToggleAction implements DumbAware {

  private final @NotNull JeditermSerialMonitorDuplexConsoleView myConsoleView;

  public ConnectDisconnectAction(@NotNull JeditermSerialMonitorDuplexConsoleView consoleView) {
    super(SerialMonitorBundle.messagePointer("connect.title"),
          SerialMonitorBundle.messagePointer("connect.tooltip"), SerialMonitorIcons.ConnectActive);
    myConsoleView = consoleView;
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
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

    PortStatus status = myConsoleView.getStatus();
    Icon icon = null;
    @NlsActions.ActionText String text = null;
    boolean enabled = false;
    switch (status) {
      case UNAVAILABLE_DISCONNECTED:
      case UNAVAILABLE:
        icon = SerialMonitorIcons.Invalid;
        text = SerialMonitorBundle.message("connect-invalid-settings.title");
        break;
      case BUSY:
        icon = SerialMonitorIcons.Invalid;
        break;
      case READY:
      case DISCONNECTED:
        icon = SerialMonitorIcons.ConnectActive;
        text = SerialMonitorBundle.message("connect.title");
        enabled = true;
        break;
      case CONNECTING:
        icon = PortStatus.BUSY.getIcon();
        break;
      case CONNECTED:
        icon = SerialMonitorIcons.ConnectActive;
        text = SerialMonitorBundle.message("disconnect.title");
        enabled = true;
    }
    presentation.setIcon(icon);
    presentation.setText(text);
    presentation.setEnabled(enabled);
  }

}
