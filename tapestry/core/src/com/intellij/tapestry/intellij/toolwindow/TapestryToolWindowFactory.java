package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Alexey Chmutov
 */
public class TapestryToolWindowFactory implements ToolWindowFactory {
  public static final String TAPESTRY_TOOLWINDOW_ID = "Tapestry";
  private static final Key<TapestryToolWindow> TAPESTRY_TOOL_WINDOW_KEY = Key.create("tapestry.toolWindow");

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    toolWindow.setAvailable(true);
    TapestryToolWindow tapestryToolWindow = new TapestryToolWindow(project);
    Content content = ContentFactory.getInstance().createContent(tapestryToolWindow.getMainPanel(), "Tapestry", true);
    toolWindow.getContentManager().addContent(content);
    project.putUserData(TAPESTRY_TOOL_WINDOW_KEY, tapestryToolWindow);
  }

  @Nullable
  public static TapestryToolWindow getToolWindow(Project project) {
    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TAPESTRY_TOOLWINDOW_ID);
    return toolWindow == null ? null : project.getUserData(TAPESTRY_TOOL_WINDOW_KEY);
  }
}
