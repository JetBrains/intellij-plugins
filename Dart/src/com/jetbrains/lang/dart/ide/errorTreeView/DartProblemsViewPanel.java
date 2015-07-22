package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;

public class DartProblemsViewPanel extends NewErrorTreeViewPanel {

  public DartProblemsViewPanel(Project project) {
    super(project, null, false, true, null);
    myTree.getEmptyText().setText("No problems found in Dart code");
  }

  @Override
  protected void fillRightToolbarGroup(DefaultActionGroup group) {
    super.fillRightToolbarGroup(group);
    final AnAction reanalyzeAction = ActionManager.getInstance().getAction("Dart.Reanalyze");
    if (reanalyzeAction != null) {
      group.add(reanalyzeAction);
    }
  }

  @Override
  protected void addExtraPopupMenuActions(DefaultActionGroup group) {
  }

  @Override
  protected boolean canHideWarnings() {
    return false;
  }
}
