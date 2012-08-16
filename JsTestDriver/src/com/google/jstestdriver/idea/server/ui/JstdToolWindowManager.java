package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.execution.JstdSettingsUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowEP;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class JstdToolWindowManager {

  private static final String TOOL_WINDOW_ID = "JsTestDriver Server";

  private final Project myProject;

  public JstdToolWindowManager(@NotNull Project project) {
    myProject = project;
  }

  public static JstdToolWindowManager getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, JstdToolWindowManager.class);
  }

  @Nullable
  public ToolWindow registerToolWindowIfNeeded() {
    ToolWindowManagerEx toolWindowManagerEx = ToolWindowManagerEx.getInstanceEx(myProject);
    ToolWindow toolWindow = toolWindowManagerEx.getToolWindow(TOOL_WINDOW_ID);
    if (toolWindow == null) {
      ToolWindowEP toolWindowEP = getJstdToolWindowEP();
      toolWindowManagerEx.initToolWindow(toolWindowEP);
      toolWindow = toolWindowManagerEx.getToolWindow(TOOL_WINDOW_ID);
    }
    return toolWindow;
  }

  @Nullable
  private static ToolWindowEP getJstdToolWindowEP() {
    for (ToolWindowEP ep : ToolWindowEP.EP_NAME.getExtensions()) {
      if (ep.id.equals(TOOL_WINDOW_ID)) {
        return ep;
      }
    }
    return null;
  }

  public static class ToolWindowCondition implements Condition<Project> {
    @Override
    public boolean value(Project project) {
      return JstdSettingsUtil.areJstdConfigFilesInProject(project);
    }
  }

  public static class Factory implements ToolWindowFactory {
    @Override
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
      toolWindow.setAvailable(true, null);
      toolWindow.setToHideOnEmptyContent(true);

      final ContentManager contentManager = toolWindow.getContentManager();
      JstdToolWindowPanel component = new JstdToolWindowPanel();
      final Content content = contentManager.getFactory().createContent(component, null, false);
      content.setDisposer(project);
      content.setCloseable(false);

      content.setPreferredFocusableComponent(component.getPreferredFocusedComponent());
      contentManager.addContent(content);

      contentManager.setSelectedContent(content, true);
    }
  }

}
