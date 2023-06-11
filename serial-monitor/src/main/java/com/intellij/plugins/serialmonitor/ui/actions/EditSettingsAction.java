package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.plugins.serialmonitor.ui.SerialProfileConfigurable;
import com.intellij.plugins.serialmonitor.ui.console.SerialConnectable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle.message;

/**
 * @author Dmitry_Cherkas
 */
public class EditSettingsAction extends DumbAwareAction {

  private final @NotNull String myName;
  private final SerialConnectable<?> myConnectable;

  public EditSettingsAction(@NotNull String name, SerialConnectable<?> connectable) {
    super(message("edit-settings.title"), message("edit-settings.tooltip"), AllIcons.General.Settings);
    myName = name;
    myConnectable = connectable;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {

    boolean okClicked = new SettingsDialog(e).showAndGet();
    if (okClicked) {
      myConnectable.reconnect();
    }
  }

  private class SettingsDialog extends DialogWrapper {
    private SettingsDialog(AnActionEvent e) {
      super(e.getProject());
      setTitle(message("dialog.title.serial.port.settings", myName));
      init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
      return SerialProfileConfigurable.Companion.createSettingsPanel(myConnectable.getPortProfile(), false, false);
    }
  }
}
