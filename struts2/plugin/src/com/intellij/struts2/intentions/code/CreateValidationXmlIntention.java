/*
 * Copyright 2014 The authors
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
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopupStep;
import com.intellij.openapi.ui.popup.PopupStep;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.xml.XmlFile;
import com.intellij.refactoring.PackageWrapper;
import com.intellij.refactoring.util.CommonMoveClassesOrPackagesUtil;
import com.intellij.struts2.Struts2Icons;
import com.intellij.struts2.StrutsFileTemplateGroupDescriptorFactory;
import com.intellij.struts2.dom.struts.action.Action;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.struts2.dom.validator.ValidatorManager;
import com.intellij.struts2.facet.StrutsFacet;
import com.intellij.util.CommonJavaRefactoringUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;

import javax.swing.*;
import java.util.Collections;
import java.util.List;

/**
 * Creates empty {@code validation.xml} for Action class.
 *
 * @author Yann C&eacute;bron
 */
public class CreateValidationXmlIntention extends PsiElementBaseIntentionAction implements Iconable {

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
    final Module module = ModuleUtilCore.findModuleForPsiElement(clazz);
    if (module == null ||
        StrutsFacet.getInstance(module) == null) {
      return false;
    }

    final List<Action> actions = getActionsForClazz(project, clazz, module);
    if (actions.isEmpty()) {
      return false;
    }

    final List<XmlFile> files = ValidatorManager.getInstance(psiElement.getProject()).findValidationFilesFor(clazz);
    return files.isEmpty() ||
           files.size() != actions.size();
  }

  private static List<Action> getActionsForClazz(final Project project, final PsiClass clazz, final Module module) {
    final StrutsModel model = StrutsManager.getInstance(project).getCombinedModel(module);
    if (model == null || !model.isActionClass(clazz)) {
      return Collections.emptyList();
    }

    return model.findActionsByClass(clazz);
  }

  @NotNull
  @Override
  public String getFamilyName() {
    return getText();
  }

  @Override
  public Icon getIcon(final int flags) {
    return Struts2Icons.Action;
  }

  @Override
  public void invoke(@NotNull final Project project,
                     final Editor editor,
                     @NotNull final PsiElement element) throws IncorrectOperationException {
    final PsiClass actionClass = findActionClass(element);
    assert actionClass != null : element;

    final List<Action> filteredActions = getActionsWithoutValidation(actionClass);
    if (filteredActions.size() > 1) {
      final ListPopupStep<Action> step =
        new BaseListPopupStep<>("Choose action mapping", filteredActions) {

          @Override
          public Icon getIconFor(final Action value) {
            return Struts2Icons.Action;
          }

          @NotNull
          @Override
          public String getTextFor(final Action value) {
            return value.getName().getStringValue() + " (" + value.getMethod().getStringValue() + ")";
          }

          @Override
          public PopupStep onChosen(final Action selectedValue, final boolean finalChoice) {
            final String path = selectedValue.getName().getStringValue();
            WriteCommandAction.writeCommandAction(project).run(() -> createValidationXml(project, actionClass, path));
            return FINAL_CHOICE;
          }
        };
      JBPopupFactory.getInstance().createListPopup(step).showInBestPositionFor(editor);
      return;
    }

    createValidationXml(project, actionClass, filteredActions.get(0).getName().getStringValue());
  }

  private static void createValidationXml(final Project project,
                                          final PsiClass actionClass,
                                          @Nullable final String path) {
    final PsiManager manager = PsiManager.getInstance(project);
    final String actionClassQualifiedName = actionClass.getQualifiedName();
    assert actionClassQualifiedName != null;

    final PackageWrapper targetPackage =
      new PackageWrapper(manager, StringUtil.getPackageName(actionClassQualifiedName));

    final Module module = ModuleUtilCore.findModuleForPsiElement(actionClass);
    assert module != null;
    final List<VirtualFile> sourceRoots = ModuleRootManager.getInstance(module).getSourceRoots(JavaModuleSourceRootTypes.PRODUCTION);
    final VirtualFile sourceRoot = sourceRoots.size() == 1 ? sourceRoots.get(0) :
                                   CommonMoveClassesOrPackagesUtil.chooseSourceRoot(targetPackage,
                                                                                    sourceRoots,
                                                                                    manager.findDirectory(sourceRoots.get(0)));
    if (sourceRoot == null) {
      return;
    }

    final PsiDirectory directory = manager.findDirectory(sourceRoot);
    assert directory != null : sourceRoot.getPresentableUrl();

    final FileTemplateManager templateManager = FileTemplateManager.getInstance(project);
    final FileTemplate validationTemplate = templateManager.getJ2eeTemplate(StrutsFileTemplateGroupDescriptorFactory.VALIDATION_XML);

    final PsiDirectory packageDirectoryInSourceRoot = CommonJavaRefactoringUtil.createPackageDirectoryInSourceRoot(targetPackage, sourceRoot);
    try {
      final String filename =
        path == null ? actionClass.getName() + "-validation.xml" : actionClass.getName() + "-" + path + "-validation.xml";
      final PsiElement psiElement = FileTemplateUtil.createFromTemplate(validationTemplate, filename, null, packageDirectoryInSourceRoot);
      NavigationUtil.activateFileWithPsiElement(psiElement, true);
    }
    catch (Exception e) {
      throw new IncorrectOperationException("error creating validation.xml", (Throwable)e);
    }
  }

  private static List<Action> getActionsWithoutValidation(final PsiClass actionClass) {
    final Project project = actionClass.getProject();
    final List<Action> actions = getActionsForClazz(project,
                                                    actionClass,
                                                    ModuleUtilCore.findModuleForPsiElement(actionClass));

    final List<XmlFile> files = ValidatorManager.getInstance(project).findValidationFilesFor(actionClass);
    return ContainerUtil.filter(actions, action -> {
      final String path = action.getName().getStringValue();
      for (final XmlFile file : files) {
        if (file.getName().contains(path)) {
          return false;
        }
      }
      return true;
    });
  }

  @Nullable
  private static PsiClass findActionClass(final PsiElement psiElement) {
    if (!(psiElement instanceof PsiIdentifier)) {
      return null;
    }

    final PsiElement parent = psiElement.getParent();
    if (!(parent instanceof PsiClass clazz)) {
      return null;
    }

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
