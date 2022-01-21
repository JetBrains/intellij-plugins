/*
 * Copyright 2013 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.struts2.actions.create;

import com.intellij.ide.actions.CreateFileAction;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.ide.highlighter.XmlFileType;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformCoreDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.struts2.StrutsBundle;
import com.intellij.struts2.StrutsFileTemplateProvider;
import com.intellij.struts2.StrutsIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
final class CreateStrutsXmlAction extends CreateFileAction {
  public CreateStrutsXmlAction() {
    super(StrutsBundle.messagePointer("create.config.new.file"),
          StrutsBundle.messagePointer("create.config.new.file.description"),
          StrutsIcons.STRUTS_CONFIG_FILE);
  }

  @Override
  protected boolean isAvailable(final DataContext dataContext) {
    if (!super.isAvailable(dataContext)) {
      return false;
    }

    final Module module = PlatformCoreDataKeys.MODULE.getData(dataContext);
    return module != null && JavaPsiFacade.getInstance(module.getProject()).findPackage("org.apache.struts2") != null;
  }

  @Override
  protected PsiElement @NotNull [] create(@NotNull final String newName, final @NotNull PsiDirectory directory) throws Exception {
    @NonNls final String fileName = getFileName(newName);

    final Module module = ModuleUtilCore.findModuleForPsiElement(directory);
    StrutsFileTemplateProvider templateProvider = new StrutsFileTemplateProvider(module);
    final FileTemplate strutsXmlTemplate = templateProvider.determineFileTemplate(directory.getProject());
    final PsiElement file = FileTemplateUtil.createFromTemplate(strutsXmlTemplate,
                                                                fileName,
                                                                null,
                                                                directory);
    return new PsiElement[]{file};
  }

  @Override
  protected String getDefaultExtension() {
    return XmlFileType.DEFAULT_EXTENSION;
  }
}