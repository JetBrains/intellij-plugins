package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Java8StepDefinition extends AbstractJavaStepDefinition {
  public Java8StepDefinition(@NotNull PsiElement element) {
    super(element);
  }

  @Nullable
  @Override
  protected String getCucumberRegexFromElement(PsiElement element) {
    if (element instanceof PsiMethodCallExpression) {
      PsiExpressionList argumentList = ((PsiMethodCallExpression)element).getArgumentList();
      if (argumentList.getExpressions().length > 1) {
        PsiExpression stepExpression = argumentList.getExpressions()[0];
        if (stepExpression instanceof PsiLiteralExpression) {
          Object value = ((PsiLiteralExpression)stepExpression).getValue();
          if (value instanceof String) {
            return (String)value;
          }
        }
      }
    }
    return null;
  }
}
