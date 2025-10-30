// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaLambdaStepPomTarget;

@NotNullByDefault
public final class CucumberJavaFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(PsiElement element) {
    return getStepDefinition(element) != null;
  }

  private static @Nullable PsiElement getStepDefinition(PsiElement element) {
    if (element instanceof PomTargetPsiElement pomTargetPsiElement) {
      final PomTarget target = pomTargetPsiElement.getTarget();
      if (target instanceof CucumberJavaLambdaStepPomTarget pomTarget) {
        return pomTarget.getNavigationElement();
      }
    }

    if (element instanceof PsiMethod method) {
      return CucumberJavaUtil.isAnnotationStepDefinition(method) ? method : null;
    }

    return null;
  }

  @Override
  public FindUsagesHandler createFindUsagesHandler(PsiElement element, boolean forHighlightUsages) {
    return new FindUsagesHandler(element) {
    };
  }
}
