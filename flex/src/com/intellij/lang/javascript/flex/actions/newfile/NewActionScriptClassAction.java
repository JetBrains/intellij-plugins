// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.ide.IdeView;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDirectory;
import org.jetbrains.annotations.NotNull;

public class NewActionScriptClassAction extends AnAction {

  @Override
  public void update(@NotNull final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();
    final Presentation presentation = e.getPresentation();

    final boolean enabled = isAvailable(dataContext);

    presentation.setEnabledAndVisible(enabled);
  }

  @Override
  public @NotNull ActionUpdateThread getActionUpdateThread() {
    return ActionUpdateThread.BGT;
  }

  private boolean isAvailable(DataContext dataContext) {
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (project == null || project.isDisposed() || view == null) return false;

    ProjectFileIndex projectFileIndex = ProjectRootManager.getInstance(project).getFileIndex();
    for (PsiDirectory dir : view.getDirectories()) {
      if (projectFileIndex.isInSourceContent(dir.getVirtualFile()) &&
          projectFileIndex.getPackageNameByDirectory(dir.getVirtualFile()) != null) {
        Module module = ModuleUtilCore.findModuleForPsiElement(dir);
        if (module != null && isAvailableIn(module)) {
          return true;
        }
      }
    }

    return false;
  }

  protected boolean isAvailableIn(final Module module) {
    return ModuleType.get(module) == FlexModuleType.getInstance();
  }

  @Override
  public void actionPerformed(@NotNull final AnActionEvent e) {
    final DataContext dataContext = e.getDataContext();

    final IdeView view = LangDataKeys.IDE_VIEW.getData(dataContext);
    if (view == null) {
      return;
    }

    final Project project = CommonDataKeys.PROJECT.getData(dataContext);

    final PsiDirectory dir = view.getOrChooseDirectory();
    if (dir == null || project == null) return;

    CommandProcessor.getInstance().executeCommand(project, () -> createAction(dir).execute(), getCommandName(), null);

  }

  protected String getCommandName() {
    return FlexBundle.message("new.actionscript.class.command.name");
  }

  protected ActionScriptCreateClassOrInterfaceFix createAction(final PsiDirectory dir) {
    return new ActionScriptCreateClassOrInterfaceFix(dir);
  }
}
