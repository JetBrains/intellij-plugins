package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AnalysisServerStatusAction extends DumbAwareAction {
  private static final Icon STATUS_GOOD = AllIcons.Process.State.GreenOK;
  private static final Icon STATUS_UNKNOWN = AllIcons.Process.State.YellowStr;
  private static final Icon STATUS_BAD = AllIcons.Process.State.RedExcl;

  public AnalysisServerStatusAction() {
    super(DartBundle.message("analysis.server.status.good.text"), DartBundle.message("analysis.server.status.good.desc"), STATUS_GOOD);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    final Project project = e.getProject();
    if (project == null) return;

    DartFeedbackBuilder builder = DartFeedbackBuilder.getFeedbackBuilder();
    if (builder.showQuery(null)) {
      builder.sendFeedback(project, null, null);
    }
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    final Project project = e.getProject();
    if (isApplicable(project)) {
      presentation.setEnabledAndVisible(true);
      updateStatus(project, presentation);
    }
    else {
      presentation.setEnabledAndVisible(false);
    }
  }

  private static void updateStatus(@NotNull final Project project, @NotNull final Presentation presentation) {
    DartAnalysisServerService das = DartAnalysisServerService.getInstance(project);
    Icon statusIcon;
    String statusText;
    String statusDesc;
    if (das.isServerProcessActive()) {
      if (das.isServerResponsive()) {
        statusIcon = STATUS_GOOD;
        statusText = DartBundle.message("analysis.server.status.good.text");
        statusDesc = DartBundle.message("analysis.server.status.good.desc");
      }
      else {
        statusIcon = STATUS_UNKNOWN;
        statusText = DartBundle.message("analysis.server.status.unknown.text");
        statusDesc = DartBundle.message("analysis.server.status.unknown.desc");
      }
    }
    else {
      statusIcon = STATUS_BAD;
      statusText = DartBundle.message("analysis.server.status.bad.text");
      statusDesc = DartBundle.message("analysis.server.status.bad.desc");
    }

    presentation.setIcon(statusIcon);
    presentation.setText(statusText);
    presentation.setDescription(statusDesc);
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
