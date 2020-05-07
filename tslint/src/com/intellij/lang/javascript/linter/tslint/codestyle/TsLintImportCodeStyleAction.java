// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.linter.tslint.codestyle;

import com.intellij.lang.javascript.linter.tslint.TslintUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

public class TsLintImportCodeStyleAction extends AnAction {
  public static final String ACTION_ID = "TslintImportCodeStyleAction";

  @Override
  public void update(@NotNull AnActionEvent e) {
    final DataContext context = e.getDataContext();
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    final boolean enabledAndVisible = e.getProject() != null
                                      && psiFile != null
                                      && TslintUtil.isConfigFile(psiFile.getVirtualFile());
    e.getPresentation().setEnabledAndVisible(enabledAndVisible);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final Project project = e.getProject();
    final PsiFile configPsi = e.getData(CommonDataKeys.PSI_FILE);
    if (configPsi == null || project == null) {
      return;
    }
    new TsLintCodeStyleImporter(false).importConfigFile(configPsi);
    EditorNotifications.getInstance(project).updateAllNotifications();
  }
}
