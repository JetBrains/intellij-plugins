// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.search;

import com.intellij.pom.PomDeclarationSearcher;
import com.intellij.pom.PomTarget;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.steps.reference.CucumberJavaParameterPomTarget;

public class CucumberJavaPomDeclarationSearcher extends PomDeclarationSearcher {
  @Override
  public void findDeclarationsAt(@NotNull PsiElement element, int offsetInElement, @NotNull Consumer<? super PomTarget> consumer) {
    if (!(element instanceof PsiLiteralExpression)) {
      return;
    }

    Object value = ((PsiLiteralExpression)element).getValue();
    if (!(value instanceof String stringValue)) {
      return;
    }

    PsiNewExpression newExp = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class);
    if (newExp != null) {
      if (!isFirstConstructorArgument(element, newExp)) {
        return;
      }
      PsiJavaCodeReferenceElement classReference = newExp.getClassReference();
      if (classReference != null) {
        String fqn = classReference.getQualifiedName();
        if (CucumberJavaUtil.PARAMETER_TYPE_CLASS.equals(fqn)) {
          consumer.consume(new CucumberJavaParameterPomTarget(element, stringValue));
        }
      }
    }
  }

  private static boolean isFirstConstructorArgument(@NotNull PsiElement element, @NotNull PsiNewExpression newExp) {
    PsiExpressionList argumentList = newExp.getArgumentList();
    if (argumentList == null) {
      return false;
    }

    if (argumentList.getExpressionCount() == 0) {
      return false;
    }

    if (argumentList.getExpressions()[0] != element) {
      return false;
    }
    return true;
  }
}
