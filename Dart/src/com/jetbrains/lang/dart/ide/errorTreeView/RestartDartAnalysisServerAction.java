package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import icons.DartIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RestartDartAnalysisServerAction extends DumbAwareAction {
  public RestartDartAnalysisServerAction() {
    super(DartBundle.message("dart.restart.server.action.name"),
          DartBundle.message("dart.restart.server.action.name"),
          DartIcons.Restart_server);
  }

  @Override
  public void actionPerformed(AnActionEvent e) {
    DartAnalysisServerService.getInstance().restartServer();
  }

  @Override
  public void update(@NotNull final AnActionEvent e) {
    e.getPresentation().setEnabledAndVisible(isApplicable(e.getProject()));
  }

  private static boolean isApplicable(@Nullable final Project project) {
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsView.TOOLWINDOW_ID) != null;
  }
}
