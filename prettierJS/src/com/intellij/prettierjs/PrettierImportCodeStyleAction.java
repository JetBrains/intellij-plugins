// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PrettierImportCodeStyleAction extends AnAction implements DumbAware {
  public static final String ACTION_ID = "PrettierImportCodeStyleAction";

  @Override
  public void update(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile contextFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    boolean enabled = project != null && contextFile != null &&
                      (getFileWithPrettierConfiguration(project, contextFile) != null ||
                       isPackageJsonWithDependencyOnPrettier(contextFile));
    e.getPresentation().setEnabledAndVisible(enabled);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @RequiresBackgroundThread
  @RequiresReadLock
  static @Nullable VirtualFile getFileWithPrettierConfiguration(@NotNull Project project, @NotNull VirtualFile contextFile) {
    if (!PrettierUtil.isConfigFileOrPackageJson(contextFile)) return null;
    if (!ProjectFileIndex.getInstance(project).isInContent(contextFile)) return null;

    if (PrettierUtil.isConfigFile(contextFile)) return contextFile;

    // Just having a dependency in package.json but no config is not enough for this method.
    // See also isPackageJsonWithDependencyOnPrettier()
    PackageJsonData data = PackageJsonData.getOrCreate(contextFile);
    if (data.getTopLevelProperties().contains(PrettierUtil.CONFIG_SECTION_NAME)) return contextFile;

    return PrettierUtil.findSingleConfigInDirectory(contextFile.getParent());
  }

  private static boolean isPackageJsonWithDependencyOnPrettier(@NotNull VirtualFile file) {
    if (!PackageJsonUtil.isPackageJsonFile(file)) return false;

    PackageJsonData data = PackageJsonData.getOrCreate(file);
    return data.isDependencyOfAnyType(PrettierUtil.PACKAGE_NAME);
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    VirtualFile contextFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
    if (project == null || contextFile == null) return;

    VirtualFile file = getFileWithPrettierConfiguration(project, contextFile);
    if (file == null && isPackageJsonWithDependencyOnPrettier(contextFile)) {
      file = contextFile;
    }
    PsiFile psiFile = file != null ? PsiManager.getInstance(project).findFile(file) : null;
    if (psiFile == null) return;

    new PrettierCodeStyleImporter(false).importConfigFile(psiFile);
  }
}
