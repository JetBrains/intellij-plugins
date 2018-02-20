package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;
import org.jetbrains.annotations.Nullable;

public class AnalysisServerFeedbackAction extends DumbAwareAction {
  public AnalysisServerFeedbackAction() {
    super(DartBundle.message("analysis.server.status.good.text"), null, DartIcons.Feedback);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    DartFeedbackBuilder builder = DartFeedbackBuilder.getFeedbackBuilder();
    builder.sendFeedback(project, null, null);
  }

  public void update(AnActionEvent e) {
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
