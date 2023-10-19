package com.intellij.plugins.serialmonitor;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi;
import com.intellij.plugins.serialmonitor.ui.ConnectPanel;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class SerialMonitorToolWindowFactory implements ToolWindowFactory, DumbAware {
  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    toolWindow.getComponent().putClientProperty(ToolWindowContentUi.ALLOW_DND_FOR_TABS, true);
    toolWindow.setToHideOnEmptyContent(false);
    toolWindow.setStripeTitle(SerialMonitorBundle.message("tab.title.serial.connections"));
    toolWindow.setAvailable(true);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentManager manager = toolWindow.getContentManager();
    JPanel portPanel = new ConnectPanel(toolWindow);
    Content content = manager.getFactory().createContent(portPanel, SerialMonitorBundle.message("tab.title.connect"), true);
    content.setCloseable(false);
    manager.addContent(content);
  }

}
