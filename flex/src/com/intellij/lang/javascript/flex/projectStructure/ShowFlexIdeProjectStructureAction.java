package com.intellij.lang.javascript.flex.projectStructure;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.ProjectStructureConfigurable;
import com.intellij.ui.navigation.Place;

public class ShowFlexIdeProjectStructureAction extends AnAction {
  public void actionPerformed(final AnActionEvent e) {
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    if (project == null) {
      return;
    }

    final ProjectStructureConfigurable configurable = ProjectStructureConfigurable.getInstance(project);
    ShowSettingsUtil.getInstance().editConfigurable(project, configurable, new Runnable() {
      public void run() {
        configurable.navigateTo(new Place().putPath(ProjectStructureConfigurable.CATEGORY, configurable.getModulesConfig()), true);
        configurable.hideSidePanel();
      }
    });
  }

  public void update(final AnActionEvent e) {
    final Project project = PlatformDataKeys.PROJECT.getData(e.getDataContext());
    e.getPresentation().setEnabled(project != null);
    e.getPresentation().setVisible(project != null);
  }
}
