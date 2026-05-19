package com.intellij.lang.javascript.linter.eslint.importer;

import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.lang.javascript.linter.eslint.EslintUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotifications;
import org.jetbrains.annotations.NotNull;

public class EslintImportCodeStyleAction extends AnAction {
  public static final String ACTION_ID = "EslintImportCodeStyle";

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
    final boolean enabledAndVisible = project != null &&
                                      file != null &&
                                      (EslintUtil.isFlatOrLegacyConfigFile(file) || isPackageJsonWithEslintConfigSection(file));
    e.getPresentation().setEnabledAndVisible(enabledAndVisible);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private static boolean isPackageJsonWithEslintConfigSection(@NotNull VirtualFile file) {
    return PackageJsonUtil.isPackageJsonWithTopLevelProperty(file, EslintUtil.CONFIG_SECTION_NAME);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    final DataContext context = e.getDataContext();
    final PsiFile psiFile = CommonDataKeys.PSI_FILE.getData(context);
    assert psiFile != null;

    new EslintCodeStyleImporter(false).importConfigFile(psiFile);
    EditorNotifications.getInstance(psiFile.getProject()).updateAllNotifications();
  }
}
