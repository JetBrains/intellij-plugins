package com.jetbrains.lang.dart.ide.errorTreeView;

import com.intellij.ide.errorTreeView.NewErrorTreeViewPanel;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;

public class DartProblemsViewPanel extends NewErrorTreeViewPanel {

  public DartProblemsViewPanel(Project project) {
    super(project, null, false, true, null);
    myTree.getEmptyText().setText("No compilation problems found");
  }

  @Override
  protected void fillRightToolbarGroup(DefaultActionGroup group) {
    super.fillRightToolbarGroup(group);
  }

  @Override
  protected void addExtraPopupMenuActions(DefaultActionGroup group) {
  }

  @Override
  protected boolean shouldShowFirstErrorInEditor() {
    return false;
  }

  @Override
  protected boolean canHideWarnings() {
    return false;
  }
}
