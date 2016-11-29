package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RestartDartAnalysisServerAction extends DumbAwareAction {
  public RestartDartAnalysisServerAction() {
    super(DartBundle.message("dart.restart.server.action.name"),
          DartBundle.message("dart.restart.server.action.name"),
          AllIcons.Actions.Restart); // AllIcons.Actions.Restart, AllIcons.Actions.ForceRefresh
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    DartAnalysisServerService.getInstance().restartServer();
    // The list of projects was probably lost when the server crashed. Prime it with the current project to get the server restarted.
    if (e.getProject() != null) {
      DartAnalysisServerService.getInstance().serverReadyForRequest(e.getProject());
    }
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(isApplicable(e.getProject()));
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
