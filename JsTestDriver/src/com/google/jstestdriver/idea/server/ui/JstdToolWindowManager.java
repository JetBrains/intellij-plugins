package com.google.jstestdriver.idea.server.ui;

import com.google.jstestdriver.idea.server.JstdServer;
import com.google.jstestdriver.idea.server.JstdServerSettingsManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import icons.JsTestDriverIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.concurrency.Promise;

/**
 * @author Sergey Simonchik
 */
public class JstdToolWindowManager {

  private static final String TOOL_WINDOW_ID = "JsTestDriver Server";

  private final Project myProject;
  private final ToolWindow myToolWindow;
  private final ContentManager myContentManager;
  private JstdToolWindowSession myCurrentSession;

  public JstdToolWindowManager(@NotNull Project project,
                               @NotNull ToolWindowManager toolWindowManager) {
    myProject = project;
    myToolWindow = toolWindowManager.registerToolWindow(TOOL_WINDOW_ID,
                                                        true,
                                                        ToolWindowAnchor.BOTTOM,
                                                        project,
                                                        true);
    myToolWindow.setToHideOnEmptyContent(true);
    myToolWindow.setIcon(JsTestDriverIcons.ToolWindowTestDriver);
    myToolWindow.setAutoHide(true);
    myToolWindow.setSplitMode(true, null);
    myContentManager = myToolWindow.getContentManager();
  }

  public static JstdToolWindowManager getInstance(@NotNull Project project) {
    return ServiceManager.getService(project, JstdToolWindowManager.class);
  }

  public void setAvailable(boolean available) {
    if (available) {
      if (myContentManager.getContentCount() == 0) {
        JstdToolWindowSession session = new JstdToolWindowSession(myProject);
        myCurrentSession = session;
        Content content = myContentManager.getFactory().createContent(session.getComponent(), null, true);
        content.setCloseable(true);
        myContentManager.addContent(content);
      }
    }
    else {
      myContentManager.removeAllContents(true);
      myCurrentSession = null;
    }
    myToolWindow.setAvailable(available, null);
  }

  public void show() {
    if (myToolWindow.isAvailable()) {
      myToolWindow.show(null);
    }
  }

  @NotNull
  public Promise<JstdServer> restartServer() {
    JstdToolWindowSession session = myCurrentSession;
    if (session == null) {
      throw new RuntimeException("JsTestDriver Server toolwindow isn't available");
    }
    return session.restart(JstdServerSettingsManager.loadSettings());
  }
}
