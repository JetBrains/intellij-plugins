// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade;

import com.intellij.ide.actions.CreateFileFromTemplateAction;
import com.intellij.ide.actions.CreateFileFromTemplateDialog;
import com.intellij.javascript.web.JSWebUtil;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.jetbrains.plugins.jade.psi.JadeFileType;
import org.jetbrains.annotations.NotNull;

public class CreatePugOrJadeFileAction extends CreateFileFromTemplateAction implements DumbAware {

  @Override
  protected boolean isAvailable(DataContext dataContext) {
    final Project project = CommonDataKeys.PROJECT.getData(dataContext);
    if (!super.isAvailable(dataContext) || project == null) return false;
    return JSWebUtil.hasFilesOfType(project, JadeFileType.INSTANCE);
  }

  @Override
  protected void buildDialog(@NotNull Project project, @NotNull PsiDirectory directory, CreateFileFromTemplateDialog.Builder builder) {
    builder.setTitle(JadeBundle.message("pug.action.new-file.dialog.title"))
      .addKind(JadeBundle.message("pug.action.new-file.pug"), JadeIcons.Pug, "Pug File")
      .addKind(JadeBundle.message("pug.action.new-file.jade"), JadeIcons.Jade, "Jade File");
  }

  @Override
  protected String getActionName(PsiDirectory directory, @NotNull String newName, String templateName) {
    boolean isJade = newName.endsWith(".jade");
    return JadeBundle.message("pug.action.new-file.name", isJade ? 0 : 1, newName);
  }
}
