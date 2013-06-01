package com.jetbrains.lang.dart.ide.settings;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * @author: Fedor.Korotkov
 */
public class ShowDartSettingsAction extends AnAction {

  public void actionPerformed(AnActionEvent anActionEvent) {
    Project project = anActionEvent.getData(PlatformDataKeys.PROJECT);
    if (project != null) {
      ShowSettingsUtil settingsUtil = ShowSettingsUtil.getInstance();
      settingsUtil.editConfigurable(project, new DartSettingsConfigurable(project));
    }
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    e.getPresentation().setIcon(icons.DartIcons.Dart_16);
    Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null);
  }
}
