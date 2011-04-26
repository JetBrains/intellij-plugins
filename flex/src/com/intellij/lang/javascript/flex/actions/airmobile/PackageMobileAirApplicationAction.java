package com.intellij.lang.javascript.flex.actions.airmobile;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.Project;

public class PackageMobileAirApplicationAction extends DumbAwareAction {

  public void actionPerformed(AnActionEvent e) {
    new PackageMobileAirApplicationDialog(e.getData(PlatformDataKeys.PROJECT)).show();
  }

  public void update(final AnActionEvent e) {
    final Project project = e.getData(PlatformDataKeys.PROJECT);
    e.getPresentation().setEnabled(project != null && ModuleManager.getInstance(project).getModules().length > 0);
  }
}
