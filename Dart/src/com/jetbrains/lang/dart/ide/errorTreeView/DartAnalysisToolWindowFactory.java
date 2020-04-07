// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

final class DartAnalysisToolWindowFactory implements ToolWindowFactory, DumbAware {
  private static final String TOOL_WINDOW_VISIBLE_PROPERTY = "dart.analysis.tool.window.visible";

  @Override
  public boolean shouldBeAvailable(@NotNull Project project) {
    return false; // Dart Analysis tool window shouldn't show up in non-Dart projects. Will be made available explicitly if needed.
  }

  @Override
  public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
    DartProblemsViewPanel panel = new DartProblemsViewPanel(project, DartProblemsView.getInstance(project).getPresentationHelper());
    Content content = ContentFactory.SERVICE.getInstance().createContent(panel, "", false);
    toolWindow.getContentManager().addContent(content);

    toolWindow.setHelpId("reference.toolWindow.DartAnalysis");
    toolWindow.setTitleActions(Collections.singletonList(new AnalysisServerFeedbackAction()));

    toolWindow.setAvailable(true);

    if (PropertiesComponent.getInstance(project).getBoolean(TOOL_WINDOW_VISIBLE_PROPERTY, true)) {
      toolWindow.activate(null, false);
    }
  }

  /**
   * Helps to initialize Dart Analysis tool window with the same visibility state as it was when the project was previously closed.
   * Standard tool window layout serialization in ToolWindowManager doesn't work because Dart Analysis tool window is not available by default.
   */
  public static class DartToolWindowManagerListener implements ToolWindowManagerListener {
    private final @NotNull Project myProject;

    public DartToolWindowManagerListener(@NotNull Project project) {
      myProject = project;
    }

    @Override
    public void stateChanged(@NotNull ToolWindowManager toolWindowManager) {
      ToolWindow toolWindow = toolWindowManager.getToolWindow(DartProblemsView.TOOLWINDOW_ID);
      if (toolWindow != null) {
        PropertiesComponent.getInstance(myProject).setValue(TOOL_WINDOW_VISIBLE_PROPERTY, toolWindow.isVisible(), true);
      }
    }
  }
}
