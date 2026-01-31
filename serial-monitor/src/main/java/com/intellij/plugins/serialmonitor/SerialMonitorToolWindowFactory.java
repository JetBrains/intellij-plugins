package com.intellij.plugins.serialmonitor;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.plugins.serialmonitor.ui.ConnectPanel;
import com.intellij.plugins.serialmonitor.ui.SerialMonitorBundle;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;

/**
 * @author Dmitry_Cherkas, Ilia Motornyi
 */
public class SerialMonitorToolWindowFactory implements ToolWindowFactory, DumbAware {
  @Override
  public void init(@NotNull ToolWindow toolWindow) {
    toolWindow.setTabsSplittingAllowed(true);
    toolWindow.setToHideOnEmptyContent(false);
    toolWindow.setStripeTitle(SerialMonitorBundle.message("toolwindow.stripe.title"));
    toolWindow.setAvailable(true);
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    ContentManager manager = toolWindow.getContentManager();
    JPanel portPanel = new ConnectPanel(toolWindow);
    Content content = manager.getFactory().createContent(portPanel, SerialMonitorBundle.message("toolwindow.port.tab.title"), true);
    content.setCloseable(false);
    manager.addContent(content);
  }

}
