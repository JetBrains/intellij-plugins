// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

final class PrettierCompatibleCodeStyleInstaller implements DirectoryProjectConfigurator {
  @Override
  public boolean isEdtRequired() {
    return false;
  }

  @Override
  public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef, boolean isProjectCreatedWithWizard) {
    if (project.isDefault() || project.getBasePath() == null || project.isDisposed()) {
      return;
    }
    StartupManager.getInstance(project).runAfterOpened(() -> {
      ApplicationManager.getApplication().runReadAction(() -> installCodeStyle(project));
    });
  }

  private static void installCodeStyle(@NotNull Project project) {
    VirtualFile configVFile = PrettierUtil.findSingleConfigInContentRoots(project);
    PsiFile configPsiFile = configVFile != null ? PsiManager.getInstance(project).findFile(configVFile) : null;
    if (configPsiFile != null) {
      new PrettierCodeStyleImporter(true).importConfigFileWhenToolInstalled(configPsiFile);
    }
  }
}
