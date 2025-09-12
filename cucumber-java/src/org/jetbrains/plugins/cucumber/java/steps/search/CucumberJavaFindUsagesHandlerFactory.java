// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.find.findUsages.FindUsagesHandler;
import com.intellij.find.findUsages.FindUsagesHandlerFactory;
import com.intellij.pom.PomTarget;
import com.intellij.pom.PomTargetPsiElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.steps.Java8StepDefinition;

public class CucumberJavaFindUsagesHandlerFactory extends FindUsagesHandlerFactory {
  @Override
  public boolean canFindUsages(@NotNull PsiElement element) {
    return getStepDefinition(element) != null;
  }

  private static PsiElement getStepDefinition(PsiElement element) {
    if (element instanceof PomTargetPsiElement pomTargetPsiElement) {
      final PomTarget target = pomTargetPsiElement.getTarget();
      if (target instanceof Java8StepDefinition stepDefinition) {
        return stepDefinition.getElement();
      }
    }
    // TODO(bartekpacia): Handle finding usages for annotation-based step definitions here too. See IDEA-379408.
    return null;
  }

  @Override
  public @Nullable FindUsagesHandler createFindUsagesHandler(@NotNull PsiElement element, boolean forHighlightUsages) {
    return new FindUsagesHandler(element) {
    };
  }
}
