// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor;

import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.util.containers.ContainerUtil;
import org.angular2.Angular2InjectionUtils;
import org.angular2.entities.Angular2Directive;
import org.angular2.entities.Angular2DirectiveSelectorPsiElement;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.intellij.openapi.actionSystem.CommonDataKeys.PROJECT;

public class Angular2GotoDeclarationHandler implements GotoDeclarationHandler {

  @Nullable
  @Override
  public PsiElement[] getGotoDeclarationTargets(@Nullable PsiElement sourceElement, int offset, Editor editor) {
    return null;
  }

  @Nullable
  @Override
  public String getActionText(@NotNull DataContext context) {
    Project project = context.getData(PROJECT);
    if (project == null || DumbService.isDumb(project)) {
      return null;
    }
    Future<PsiElement> symbolFuture = ApplicationManager.getApplication().executeOnPooledThread(() ->
      ReadAction.compute(() -> Angular2InjectionUtils.getTargetElementFromContext(context))
    );
    PsiElement symbol;
    try {
      symbol = symbolFuture.get(200, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException | ExecutionException | TimeoutException e) {
      return null;
    }
    List<Angular2Directive> directives;
    if (symbol instanceof Angular2DirectiveSelectorPsiElement
        && !(directives = Angular2EntitiesProvider.findDirectives((Angular2DirectiveSelectorPsiElement)symbol)).isEmpty()) {
      return ContainerUtil.all(directives, Angular2Directive::isComponent)
             ? Angular2Bundle.message("angular.action.goto-declaration.component")
             : Angular2Bundle.message("angular.action.goto-declaration.directive");
    }
    return null;
  }
}
