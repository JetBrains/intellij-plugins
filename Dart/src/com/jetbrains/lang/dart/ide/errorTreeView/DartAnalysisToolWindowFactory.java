// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class DartAnalysisToolWindowFactory implements ToolWindowFactory, DumbAware {
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
    ((ToolWindowEx)toolWindow).setTitleActions(new AnalysisServerFeedbackAction());

    toolWindow.setAvailable(true, null);
  }
}
