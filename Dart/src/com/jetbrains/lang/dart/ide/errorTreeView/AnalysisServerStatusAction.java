package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.SendFeedbackAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.Extensions;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageDialogBuilder;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class AnalysisServerStatusAction extends DumbAwareAction {
  private static final Icon STATUS_GOOD = AllIcons.Process.State.GreenOK;
  private static final Icon STATUS_UNKNOWN = AllIcons.Process.State.YellowStr;
  private static final Icon STATUS_BAD = AllIcons.Process.State.RedExcl;

  private Presentation myPresentation;

  public AnalysisServerStatusAction() {
    myPresentation = getTemplatePresentation();
    myPresentation.setText(DartBundle.message("analysis.server.status.good.text"));
    myPresentation.setDescription(DartBundle.message("analysis.server.status.good.desc"));
    myPresentation.setIcon(STATUS_GOOD);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    DartFeedbackBuilder[] builders = (DartFeedbackBuilder[])Extensions.getExtensions("Dart.feedbackBuilder");
    if (builders.length == 0) {
      throw new Error(); // Can't happen.
    }
    DartFeedbackBuilder builder = builders[0]; // Use the order attribute to ensure another plugin takes precedence.

    if (MessageDialogBuilder.yesNo(builder.title(), builder.prompt())
          .icon(Messages.getQuestionIcon())
          .yesText(builder.label())
          .show() == Messages.YES) {
      builder.sendFeedback(e.getProject());
    }
  }

  public void update(AnActionEvent e) {
    myPresentation = e.getPresentation();
    if (isApplicable(e.getProject())) {
      myPresentation.setEnabledAndVisible(true);
      updateStatus();
    }
    else {
      myPresentation.setEnabledAndVisible(false);
    }
  }

  public void updateStatus() {
    if (myPresentation == null) return;
    if (!myPresentation.isEnabledAndVisible()) return;
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
    ApplicationManager.getApplication().invokeLater(() -> {
      myPresentation.setIcon(statusIcon);
      myPresentation.setText(statusText);
      myPresentation.setDescription(statusDesc);
    });
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
