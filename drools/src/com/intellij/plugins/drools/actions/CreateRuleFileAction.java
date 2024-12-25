// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.actions;

import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.plugins.drools.DroolsBundle;
import com.intellij.plugins.drools.DroolsFileType;
import com.intellij.plugins.drools.JbossDroolsIcons;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class CreateRuleFileAction extends CreateFileAction {
  public CreateRuleFileAction() {
    super(DroolsBundle.messagePointer("rule.new.file"),
          DroolsBundle.messagePointer("rule.new.file.description"),
          () -> JbossDroolsIcons.Drools_16);
  }

  @Override
  protected boolean isAvailable(final DataContext dataContext) {
    if (!super.isAvailable(dataContext)) {
      return false;
    }
    final Module module = PlatformCoreDataKeys.MODULE.getData(dataContext);
    return module != null && JavaPsiFacade.getInstance(module.getProject()).findPackage("org.drools") != null;
  }

  @Override
  protected PsiElement @NotNull [] create(final @NotNull String newName, final @NotNull PsiDirectory directory) throws Exception {
    final Module module = ModuleUtilCore.findModuleForPsiElement(directory);
    assert module != null : directory;


    final FileTemplate template = FileTemplateManager.getInstance(module.getProject()).getJ2eeTemplate("drools.rule.drl");
    final @NonNls String fileName = getFileName(newName);

    Map<String, Object> proprs = new HashMap<>();
    proprs.put("RULE_NAME", FileUtilRt.getNameWithoutExtension(newName));
    final PsiElement psiElement = FileTemplateUtil.createFromTemplate(template, fileName, proprs, directory, null);
    return new PsiElement[]{psiElement};
  }

  @Override
  protected String getDefaultExtension() {
    return DroolsFileType.DEFAULT_EXTENSION;
  }
}