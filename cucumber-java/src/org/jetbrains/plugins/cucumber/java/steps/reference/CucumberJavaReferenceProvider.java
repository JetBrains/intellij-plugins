// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.PomTargetPsiElementImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberParameterTypeSelfReference;

import java.util.ArrayList;
import java.util.List;

public class CucumberJavaReferenceProvider extends PsiReferenceProvider {
  public static final String PARAMETER_TYPE_CLASS = "io.cucumber.cucumberexpressions.ParameterType";

  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    if (!(element instanceof PsiLiteralExpression)) {
      return PsiReference.EMPTY_ARRAY;
    }
    Object value = ((PsiLiteralExpression)element).getValue();
    if (!(value instanceof String)) {
      return PsiReference.EMPTY_ARRAY;
    }
    String stringValue = (String)value;

    PsiNewExpression newExp = PsiTreeUtil.getParentOfType(element, PsiNewExpression.class);
    if (newExp != null) {
      if (!isFirstConstructorArgument(element, newExp)) {
        return PsiReference.EMPTY_ARRAY;
      }
      PsiJavaCodeReferenceElement classReference = newExp.getClassReference();
      if (classReference != null) {
        String constructorName = classReference.getQualifiedName();
        if (constructorName != null && constructorName.equals(PARAMETER_TYPE_CLASS)) {
          CucumberJavaParameterPomTargetPsiElement target = new CucumberJavaParameterPomTargetPsiElement(element, stringValue);
          PsiElement pomTargetPsiElement = new PomTargetPsiElementImpl(element.getProject(), target) {
            @Nullable
            @Override
            public String getText() {
              return element.getText();
            }

            @Override
            public int getTextLength() {
              return element.getTextLength();
            }

            @Nullable
            @Override
            public TextRange getTextRange() {
              return element.getTextRange();
            }
          };
          return new PsiReference[] {new CucumberParameterTypeSelfReference(pomTargetPsiElement)};
        }
      }
    }

    PsiMethod method = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
    if (method != null) {
      String cucumberExpression = CucumberJavaUtil.getStepAnnotationValue(method, null);
      if (cucumberExpression == null) {
        return PsiReference.EMPTY_ARRAY;
      }

      List<CucumberJavaParameterTypeReference> result = new ArrayList<>();
      CucumberUtil.processParameterTypesInCucumberExpression(cucumberExpression, range -> {
        range = range.shiftRight(1); // Skip " in the begin of the String Literal

        result.add(new CucumberJavaParameterTypeReference(element, range));
        return true;
      });
      return result.toArray(new CucumberJavaParameterTypeReference[0]);
    }

    return PsiReference.EMPTY_ARRAY;
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
