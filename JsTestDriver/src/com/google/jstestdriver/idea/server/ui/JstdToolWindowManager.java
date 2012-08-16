package com.google.jstestdriver.idea.server.ui;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowEP;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
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
      ToolWindowEP toolWindowEP = new ToolWindowEP() {
        @Override
        public ToolWindowFactory getToolWindowFactory() {
          return new JstdToolWindow();
        }
      };
      toolWindowEP.id = TOOL_WINDOW_ID;
      toolWindowEP.anchor = ToolWindowAnchor.BOTTOM.toString();
      toolWindowEP.secondary = false;
      toolWindowEP.icon = "/com/google/jstestdriver/idea/icons/toolWindowTestDriver.png";
      toolWindowEP.canCloseContents = false;
      toolWindowManagerEx.initToolWindow(toolWindowEP);
      toolWindow = toolWindowManagerEx.getToolWindow(TOOL_WINDOW_ID);
    }
    return toolWindow;
  }

}
