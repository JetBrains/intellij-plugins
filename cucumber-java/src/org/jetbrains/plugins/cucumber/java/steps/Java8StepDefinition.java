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
  public String getStepDefinitionText() {
    PsiElement element = getElement();
    if (!(element instanceof PsiMethodCallExpression)) {
      return null;
    }
    PsiExpressionList argumentList = ((PsiMethodCallExpression)element).getArgumentList();
    if (argumentList.getExpressions().length <= 1) {
      return null;
    }
    PsiExpression stepExpression = argumentList.getExpressions()[0];
    final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
    final Object constantValue = evaluationHelper.computeConstantExpression(stepExpression, false);

    if (constantValue != null) {
      if (constantValue instanceof String) {
        return (String)constantValue;
      }
    }
    return null;
  }
}
