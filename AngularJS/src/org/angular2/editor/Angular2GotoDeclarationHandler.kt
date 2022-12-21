// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.entities.Angular2Directive;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Angular2GotoDeclarationHandler implements GotoDeclarationHandler {

  @Override
  public PsiElement @Nullable [] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
    return null;
  }

  @Override
  public @Nullable String getActionText(@NotNull DataContext context) {
    List<Angular2Directive> directives = Angular2EditorUtils.getDirectivesAtCaret(context);
    if (!directives.isEmpty()) {
      return ContainerUtil.all(directives, Angular2Directive::isComponent)
             ? Angular2Bundle.message("angular.action.goto-declaration.component")
             : Angular2Bundle.message("angular.action.goto-declaration.directive");
    }
    return null;
  }
}
