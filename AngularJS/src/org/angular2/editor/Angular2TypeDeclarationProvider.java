// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.TypeDeclarationProvider;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Component;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2TypeDeclarationProvider implements TypeDeclarationProvider {

  @Override
  public PsiElement @Nullable [] getSymbolTypeDeclarations(@NotNull PsiElement symbol) {
    Angular2Component component;
    if (symbol instanceof Angular2DirectiveSelectorPsiElement
        && (component = Angular2EntitiesProvider.findComponent((Angular2DirectiveSelectorPsiElement)symbol)) != null) {
      PsiFile htmlFile = component.getTemplateFile();
      if (htmlFile != null) {
        return new PsiElement[]{htmlFile};
      }
    }
    return null;
  }

  @Override
  public @Nullable String getActionText(@NotNull DataContext context) {
    List<Angular2Directive> directives = Angular2EditorUtils.getDirectivesAtCaret(context);
    if (ContainerUtil.find(directives, Angular2Directive::isComponent) != null) {
      return Angular2Bundle.message("angular.action.goto-type-declaration.component-template");
    }
    return null;
  }
}
