package com.intellij.plugins.serialmonitor.ui.actions;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.plugins.serialmonitor.SerialMonitorToolWindowFactory;
import com.intellij.plugins.serialmonitor.SerialPortProfile;
import com.intellij.plugins.serialmonitor.SerialProfileService;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.plugins.serialmonitor.ui.SerialSettingsConfigurable;
import com.intellij.ui.content.Content;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ConnectActionGroup extends ActionGroup implements DumbAware {

  @Override
  public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
    Project project = getEventProject(e);
    if (project == null) return EMPTY_ARRAY;
    return
      Stream.concat(
          Stream.of(new OpenSettingsAction(),
                    new Separator()),
          SerialProfileService
            .getInstance()
            .getProfiles()
            .entrySet()
            .stream()
            .map(entry -> {
              return new OpenConnection(project, entry.getKey(), entry.getValue());
            }))
        .toArray(AnAction[]::new);
  }

  public static void openWindow(Project project,
                                @NotNull @NlsSafe String name,
                                @NotNull SerialPortProfile portProfile) {
    ToolWindow monitor = ToolWindowManager.getInstance(project).getToolWindow("Serial Monitor");
    if (monitor != null) {
      monitor.setToHideOnEmptyContent(true);
      Content content = monitor.getContentManager().findContent(name);
      if (content != null) {
        monitor.getContentManager().setSelectedContent(content);
      }
      else {
        SerialMonitorToolWindowFactory.addTab(project, monitor, name, portProfile);
      }
      monitor.show();
      monitor.activate(null, true);
    }
  }

  private static class OpenConnection extends DumbAwareAction {

    private final Project project;
    private final SerialPortProfile myPortProfile;
    private final @NotNull @NlsSafe String myName;

    private OpenConnection(Project project, @NotNull @NlsSafe String name, @NotNull SerialPortProfile portProfile) {
      super(name);
      myPortProfile = portProfile;
      myName = name;
      this.project = project;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      openWindow(project, myName, myPortProfile);
    }
  }

  private static class OpenSettingsAction extends DumbAwareAction {
    private OpenSettingsAction() {
      super(SerialMonitorBundle.messagePointer("action.settings.text"), AllIcons.General.Settings);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
      ShowSettingsUtil.getInstance().showSettingsDialog(e.getProject(), SerialSettingsConfigurable.class);
    }
  }
}
