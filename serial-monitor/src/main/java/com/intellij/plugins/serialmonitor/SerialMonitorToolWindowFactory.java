package com.intellij.plugins.serialmonitor;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.plugins.serialmonitor.ui.SerialMonitor;
import com.intellij.plugins.serialmonitor.ui.actions.ConnectActionGroup;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class SerialMonitorToolWindowFactory implements ToolWindowFactory, DumbAware {
  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    toolWindow.setToHideOnEmptyContent(true);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    AnAction action = new DumbAwareAction(AllIcons.General.Add) {
      @Override
      public void actionPerformed(@NotNull AnActionEvent e) {
        @NotNull ListPopup popup = JBPopupFactory.getInstance()
          .createActionGroupPopup(null, new ConnectActionGroup(), e.getDataContext(), false, null, 5);
        popup.showUnderneathOf(e.getInputEvent().getComponent());
      }
    };
    ((ToolWindowEx)toolWindow).setTabActions(action);
  }

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return false;
  }

  public static void addTab(@NotNull Project project,
                            @NotNull ToolWindow toolWindow,
                            @NotNull @NlsSafe String name,
                            @NotNull SerialPortProfile portProfile) {
    SimpleToolWindowPanel panel = new SimpleToolWindowPanel(false, true);
    Content content = ContentFactory.getInstance().createContent(panel, "", true);
    content.putUserData(ToolWindow.SHOW_CONTENT_ICON, Boolean.TRUE);
    SerialMonitor serialMonitor = new SerialMonitor(project, icon -> content.setIcon(icon), name, portProfile);
    panel.setContent(serialMonitor.getComponent());

    content.setDisplayName(name);
    content.setDisposer(serialMonitor);
    content.setCloseable(true);
    toolWindow.getContentManager().addContent(content);
    toolWindow.getContentManager().setSelectedContent(content, true);
    toolWindow.setAvailable(true);
    serialMonitor.connect();
  }
}
