// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.javascript.nodejs.PackageJsonData;
import com.intellij.lang.javascript.buildTools.npm.PackageJsonUtil;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.ui.EditorNotificationProvider;
import com.intellij.ui.EditorNotifications;
import com.intellij.util.concurrency.annotations.RequiresBackgroundThread;
import com.intellij.util.concurrency.annotations.RequiresReadLock;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;

public class PrettierImportCodeStyleAction extends AnAction {
  public static final String ACTION_ID = "PrettierImportCodeStyleAction";

  @Override
  public void update(@NotNull AnActionEvent e) {
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    VirtualFile file = psiFile != null ? psiFile.getVirtualFile() : null;
    e.getPresentation().setEnabledAndVisible(file != null && canImportPrettierConfigurationFromThisFile(psiFile.getProject(), file));
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  @RequiresBackgroundThread
  @RequiresReadLock
  static boolean canImportPrettierConfigurationFromThisFile(@NotNull Project project, @NotNull VirtualFile file) {
    if (!PrettierUtil.isConfigFileOrPackageJson(file)) {
      return false;
    }

    if (!ProjectFileIndex.getInstance(project).isInContent(file)) {
      return false;
    }

    if (!PackageJsonUtil.isPackageJsonFile(file)) {
      return true; // prettier config file
    }

    PackageJsonData data = PackageJsonData.getOrCreate(file);
    if (data.getTopLevelProperties().contains(PrettierUtil.PACKAGE_NAME)) {
      return true;
    }

    if (data.containsOneOfDependencyOfAnyType(PrettierUtil.PACKAGE_NAME)) {
      // This package.json file contains a dependency on the 'prettier' package but doesn't contain a customized prettier configuration.
      // Still, it makes sense to import the default prettier configuration, but only if there's no other config file nearby.
      boolean prettierRcFileExistsNearby = ContainerUtil.exists(file.getParent().getChildren(), f -> PrettierUtil.isConfigFile(f));
      if (!prettierRcFileExistsNearby) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    PsiFile psiFile = e.getData(CommonDataKeys.PSI_FILE);
    VirtualFile virtualFile = psiFile != null ? psiFile.getVirtualFile() : null;
    if (virtualFile == null) {
      return;
    }

    new PrettierCodeStyleImporter(false).importConfigFile(psiFile);
    updateEditorNotifications(psiFile.getProject());
  }

  private static void updateEditorNotifications(@NotNull Project project) {
    PrettierCodeStyleEditorNotificationProvider provider =
      EditorNotificationProvider.EP_NAME.findExtension(PrettierCodeStyleEditorNotificationProvider.class, project);
    if (provider != null) {
      EditorNotifications.getInstance(project).updateNotifications(provider);
    }
  }
}
