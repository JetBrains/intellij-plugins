/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.intentions.code;

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction;
import com.intellij.codeInsight.navigation.NavigationUtil;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.move.moveClassesOrPackages.MoveClassesOrPackagesUtil;
import com.intellij.refactoring.util.RefactoringUtil;
import com.intellij.struts2.StrutsFileTemplateGroupDescriptorFactory;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Creates empty {@code validation.xml} for Action class.
 *
 * @author Yann C&eacute;bron
 */
public class CreateValidationXmlIntention extends PsiElementBaseIntentionAction {

  @NotNull
  @Override
  public String getText() {
    return "Create validation.xml";
  }

  @Override
  public boolean isAvailable(@NotNull final Project project,
                             final Editor editor,
                             @NotNull final PsiElement psiElement) {
    final PsiClass clazz = findActionClass(psiElement);
    if (clazz == null) {
      return false;
    }

    // short exit if Struts Facet not present
    final Module module = ModuleUtil.findModuleForPsiElement(clazz);
    if (module == null ||
        StrutsFacet.getInstance(module) == null) {
      return false;
    }

    final List<PsiElement> files = ValidatorManager.getInstance(psiElement.getProject()).findValidationFilesFor(clazz);
    return files.isEmpty();
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public void invoke(final Project project, final Editor editor, final PsiElement element) throws
      IncorrectOperationException {
    final PsiClass actionClass = findActionClass(element);
    assert actionClass != null : element;

    final PsiManager manager = PsiManager.getInstance(project);
    final PackageWrapper targetPackage =
        new PackageWrapper(manager, StringUtil.getPackageName(actionClass.getQualifiedName()));

    final Module module = ModuleUtil.findModuleForPsiElement(element);
    final VirtualFile[] sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(false);
    final VirtualFile sourceRoot = sourceRoots.length == 1 ? sourceRoots[0] :
        MoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage,
                                                   sourceRoots,
                                                   manager.findDirectory(sourceRoots[0]));
    if (sourceRoot == null) {
      return;
    }

    final PsiDirectory directory = manager.findDirectory(sourceRoot);
    assert directory != null : sourceRoot.getPresentableUrl();

    final FileTemplateManager templateManager = FileTemplateManager.getInstance();
    final FileTemplate validationTemplate = templateManager.getJ2eeTemplate(StrutsFileTemplateGroupDescriptorFactory.VALIDATOR_XML);

    final PsiDirectory packageDirectoryInSourceRoot = RefactoringUtil.createPackageDirectoryInSourceRoot(targetPackage,
                                                                                                         sourceRoot);
    try {
      final PsiElement psiElement = FileTemplateUtil.createFromTemplate(validationTemplate,
                                                                        actionClass.getName() + "-validation.xml",
                                                                        null,
                                                                        packageDirectoryInSourceRoot);
      NavigationUtil.activateFileWithPsiElement(psiElement, true);
    } catch (Exception e) {
      throw new IncorrectOperationException("error creating validation.xml", e);
    }
  }

  @Nullable
  private static PsiClass findActionClass(final PsiElement psiElement) {
    if (!(psiElement instanceof PsiIdentifier)) {
      return null;
    }

    final PsiElement parent = psiElement.getParent();
    if (!(parent instanceof PsiClass)) {
      return null;
    }

    final PsiClass clazz = (PsiClass) parent;
    if (clazz.getNameIdentifier() != psiElement) {
      return null;
    }

    // do not run on non-public, abstract classes or interfaces
    if (clazz.isInterface() ||
        clazz.isAnnotationType() ||
        !clazz.hasModifierProperty(PsiModifier.PUBLIC) ||
        clazz.hasModifierProperty(PsiModifier.ABSTRACT)) {
      return null;
    }

    return clazz;
  }

}