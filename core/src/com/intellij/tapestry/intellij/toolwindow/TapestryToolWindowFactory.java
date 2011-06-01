package com.intellij.tapestry.intellij.toolwindow;

import com.intellij.facet.ProjectWideFacetAdapter;
import com.intellij.facet.ProjectWideFacetListenersRegistry;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.tapestry.intellij.facet.TapestryFacet;
import com.intellij.tapestry.intellij.facet.TapestryFacetType;
import com.intellij.tapestry.intellij.util.Icons;
import com.intellij.tapestry.intellij.util.TapestryUtils;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * @author Alexey Chmutov
 *         Date: Jun 19, 2009
 *         Time: 12:57:05 PM
 */
public class TapestryToolWindowFactory implements ToolWindowFactory, Condition<Project> {
  public static final String TAPESTRY_TOOLWINDOW_ID = "Tapestry";
  private static final Key<TapestryToolWindow> TAPESTRY_TOOL_WINDOW_KEY = Key.create("tapestry.toolWindow");

  public void createToolWindowContent(Project project, ToolWindow toolWindow) {
    toolWindow.setAvailable(true, null);
    TapestryToolWindow tapestryToolWindow = new TapestryToolWindow(project);
    Content content = ContentFactory.SERVICE.getInstance().createContent(tapestryToolWindow.getMainPanel(), "Tapestry", true);
    toolWindow.getContentManager().addContent(content);
    toolWindow.setIcon(Icons.TAPESTRY_LOGO_SMALL);
    project.putUserData(TAPESTRY_TOOL_WINDOW_KEY, tapestryToolWindow);
  }

  public boolean value(Project project) {
    return TapestryUtils.getAllTapestryModules(project).length > 0;
  }

  public void configureToolWindow(final Project project) {
    ProjectWideFacetListenersRegistry.getInstance(project)
        .registerListener(TapestryFacetType.ID, new ProjectWideFacetAdapter<TapestryFacet>() {
          @Override
          public void firstFacetAdded() {
            final ToolWindowManager manager = ToolWindowManager.getInstance(project);
            final ToolWindow toolWindow = manager.getToolWindow(TAPESTRY_TOOLWINDOW_ID);
            if (toolWindow != null) return;
            final ToolWindow window = manager.registerToolWindow(TAPESTRY_TOOLWINDOW_ID, false, ToolWindowAnchor.BOTTOM, project);
            window.setSplitMode(true, null);
            createToolWindowContent(project, window);
          }

          @Override
          public void allFacetsRemoved() {
            final ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TAPESTRY_TOOLWINDOW_ID);
            if (toolWindow == null) return;
            ToolWindowManager.getInstance(project).unregisterToolWindow(TAPESTRY_TOOLWINDOW_ID);
            Disposer.dispose(toolWindow.getContentManager());
          }
        });
  }

  public static TapestryToolWindow getToolWindow(Project project) {
    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow(TAPESTRY_TOOLWINDOW_ID);
    return toolWindow == null ? null : project.getUserData(TAPESTRY_TOOL_WINDOW_KEY);
  }
}
