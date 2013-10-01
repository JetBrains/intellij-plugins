package com.jetbrains.lang.dart.ide.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.util.PlatformUtils;
import com.jetbrains.lang.dart.DartBundle;
import icons.DartIcons;

/**
 * @author: Fedor.Korotkov
 */
public class ShowDartSettingsAction extends AnAction {
  public void actionPerformed(AnActionEvent anActionEvent) {
    Project project = anActionEvent.getData(CommonDataKeys.PROJECT);
    if (project != null) {
      ShowSettingsUtil.getInstance().showSettingsDialog(project, DartBundle.message("dart.title"));
    }
  }

  @Override
  public void update(AnActionEvent e) {
    super.update(e);
    Presentation presentation = e.getPresentation();
    presentation.setIcon(DartIcons.Dart_16);
    Project project = e.getData(CommonDataKeys.PROJECT);
    boolean active = PlatformUtils.isWebStorm() && project != null;
    presentation.setEnabled(active);
    presentation.setVisible(active);
  }
}
