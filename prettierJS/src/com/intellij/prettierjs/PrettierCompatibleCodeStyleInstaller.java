// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.prettierjs;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.platform.DirectoryProjectConfigurator;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class PrettierCompatibleCodeStyleInstaller implements DirectoryProjectConfigurator {

  @Override
  public void configureProject(@NotNull Project project, @NotNull VirtualFile baseDir, @NotNull Ref<Module> moduleRef) {
    if (project.isDefault() || project.getBaseDir() == null || project.isDisposed()) return;
    StartupManager.getInstance(project).runWhenProjectIsInitialized((DumbAwareRunnable)() -> installCodeStyle(project));
  }

  private static void installCodeStyle(@NotNull Project project) {
    VirtualFile configVFile = PrettierUtil.findSingleConfigInContentRoots(project);
    final PsiFile configPsiFile = configVFile != null ? PsiManager.getInstance(project).findFile(configVFile) : null;
    if (configPsiFile != null) {
      new PrettierCodeStyleImporter(true).importConfigFileWhenToolInstalled(configPsiFile);
    }
  }
}
