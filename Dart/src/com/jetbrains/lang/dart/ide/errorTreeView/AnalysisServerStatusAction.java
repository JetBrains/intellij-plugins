package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
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
    DartFeedbackBuilder builder = DartFeedbackBuilder.getFeedbackBuilder();

    if (MessageDialogBuilder.yesNo(builder.title(), builder.prompt())
          .icon(Messages.getQuestionIcon())
          .yesText(builder.label())
          .show() == Messages.YES) {
      builder.sendFeedback(e.getProject());
    }
  }

  public void update(AnActionEvent e) {
    Presentation presentation = e.getPresentation();
    if (isApplicable(e.getProject())) {
      presentation.setEnabledAndVisible(true);
      updateStatus(presentation);
    }
    else {
      presentation.setEnabledAndVisible(false);
    }
  }

  private static void updateStatus(@NotNull final Presentation presentation) {
    DartAnalysisServerService das = DartAnalysisServerService.getInstance();
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
