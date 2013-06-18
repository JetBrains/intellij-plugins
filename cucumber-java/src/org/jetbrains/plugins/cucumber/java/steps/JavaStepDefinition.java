package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.*;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Andrey.Vokin
 * Date: 7/16/12
 */
public class JavaStepDefinition extends AbstractStepDefinition {
  private String pattern;

  public JavaStepDefinition(PsiMethod method) {
    super(method);

    PsiAnnotation stepAnnotation = CucumberJavaUtil.getCucumberStepAnnotation(method);
    assert stepAnnotation != null;
    final PsiElement annotationValue = CucumberJavaUtil.getAnnotationValue(stepAnnotation);
    if (annotationValue != null) {
      final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(method.getProject()).getConstantEvaluationHelper();
      final Object constantValue = evaluationHelper.computeConstantExpression(annotationValue, false);
      if (constantValue != null) {
        String patternText = constantValue.toString();
        if (patternText.length() > 1) {
          pattern = patternText.replace("\\\\", "\\").replace("\\\"", "\"");
        }
      }
    }
  }

  @Override
  public List<String> getVariableNames() {
    PsiElement element = getElement();
    if (element instanceof PsiMethod) {
      PsiParameter[] parameters = ((PsiMethod)element).getParameterList().getParameters();
      ArrayList<String> result = new ArrayList<String>();
      for (PsiParameter parameter : parameters) {
        result.add(parameter.getName());
      }
      return result;
    }
    return Collections.emptyList();
  }

  @Override
  public String getElementText() {
    return pattern;
  }
}
