package com.intellij.flexIde;

import com.intellij.ide.actions.ShowStructureSettingsAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;

/**
 * User: ksafonov
 */
public class FlexIdeShowStructureSettingsAction extends ShowStructureSettingsAction {

  @Override
  public void update(AnActionEvent e) {
    Project project = getEventProject(e);
    e.getPresentation().setEnabled(project != null && !project.isDefault());
  }
}
