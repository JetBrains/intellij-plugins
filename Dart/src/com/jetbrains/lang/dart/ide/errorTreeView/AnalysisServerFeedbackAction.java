// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AnalysisServerFeedbackAction extends DumbAwareAction {
  public AnalysisServerFeedbackAction() {
    super(DartBundle.message("analysis.server.status.good.text"), null, AllIcons.General.Balloon);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    DartFeedbackBuilder builder = DartFeedbackBuilder.getFeedbackBuilder();
    builder.sendFeedback(project, null, null);
  }

  @Override
  public void update(@NotNull AnActionEvent e) {
    final Presentation presentation = e.getPresentation();
    final Project project = e.getProject();
    if (isApplicable(project)) {
      presentation.setEnabledAndVisible(true);
    }
    else {
      presentation.setEnabledAndVisible(false);
    }
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
