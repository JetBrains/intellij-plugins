package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.icons.AllIcons;
import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;

public class DartProblemsViewPanel extends NewErrorTreeViewPanel {

  public DartProblemsViewPanel(Project project) {
    super(project, null, false, true, null);
    myTree.getEmptyText().setText("No problems found in Dart code");
  }

  @Override
  protected void fillRightToolbarGroup(DefaultActionGroup group) {
    super.fillRightToolbarGroup(group);
    group.add(new AnAction(DartBundle.message("dart.reanalyze.action.name"), DartBundle.message("dart.reanalyze.action.description"),
                           AllIcons.Actions.ForceRefresh) {
      @Override
      public void actionPerformed(AnActionEvent e) {
        DartAnalysisServerService.getInstance().analysis_reanalyze(null);
      }
    });
  }

  @Override
  protected void addExtraPopupMenuActions(DefaultActionGroup group) {
  }

  @Override
  protected boolean canHideWarnings() {
    return false;
  }
}
