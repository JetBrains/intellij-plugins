// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2InjectionUtils;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2GotoDeclarationHandler implements GotoDeclarationHandler {

  @Nullable
  @Override
  public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
    return null;
  }

  @Nullable
  @Override
  public String getActionText(@NotNull DataContext context) {
    PsiElement symbol = Angular2InjectionUtils.getTargetElementFromContext(context);
    List<Angular2Directive> directives;
    if (symbol instanceof Angular2DirectiveSelectorPsiElement
        && !(directives = Angular2EntitiesProvider.findDirectives((Angular2DirectiveSelectorPsiElement)symbol)).isEmpty()) {
      return ContainerUtil.all(directives,Angular2Directive::isComponent) ? "Component &Declaration" : "Directive &Declaration";
    }
    return null;
  }
}
