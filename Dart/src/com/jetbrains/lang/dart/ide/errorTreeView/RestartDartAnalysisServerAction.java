package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.LayeredIcon;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RestartDartAnalysisServerAction extends DumbAwareAction {
  public RestartDartAnalysisServerAction() {
    super(DartBundle.message("dart.restart.server.action.name"),
          DartBundle.message("dart.restart.server.action.name"),
          new LayeredIcon(AllIcons.Debugger.KillProcess, AllIcons.Nodes.RunnableMark));
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
    return project != null && ToolWindowManager.getInstance(project).getToolWindow(DartProblemsViewImpl.TOOLWINDOW_ID) != null;
  }
}
