package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.*;
import org.apache.oro.text.regex.Pattern;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.PatternSyntaxException;

public class JavaStepDefinition extends AbstractStepDefinition {
  @Nullable
  private final PsiClass annotationClass;

  public JavaStepDefinition(PsiElement stepDef) {
    super(stepDef);
    this.annotationClass = null;
  }

  public JavaStepDefinition(PsiElement stepDef, @Nullable PsiClass annotationClass) {
    super(stepDef);
    this.annotationClass = annotationClass;
  }

  @Override
  public List<String> getVariableNames() {
    PsiElement element = getElement();
    if (element instanceof PsiMethod) {
      PsiParameter[] parameters = ((PsiMethod)element).getParameterList().getParameters();
      ArrayList<String> result = new ArrayList<>();
      for (PsiParameter parameter : parameters) {
        result.add(parameter.getName());
      }
      return result;
    }
    return Collections.emptyList();
  }

  @Nullable
  @Override
  protected String getCucumberRegexFromElement(PsiElement element) {
    if (element instanceof PsiMethod && annotationClass != null) {
      final String annotationName = annotationClass.getQualifiedName();
      if (annotationName == null) {
        return null;
      }
      final PsiAnnotation stepAnnotation = ((PsiMethod) element).getModifierList().findAnnotation(annotationName);
      if (stepAnnotation == null) {
        return null;
      }
      final PsiElement annotationValue = CucumberJavaUtil.getAnnotationValue(stepAnnotation);
      if (annotationValue != null) {
        final PsiConstantEvaluationHelper evaluationHelper = JavaPsiFacade.getInstance(element.getProject()).getConstantEvaluationHelper();
        final Object constantValue = evaluationHelper.computeConstantExpression(annotationValue, false);
        if (constantValue != null) {
          String patternText = constantValue.toString();
          if (patternText.length() > 1) {
            return patternText.replace("\\\\", "\\").replace("\\\"", "\"");
          }
        }
      }
    } else if (element instanceof PsiMethodCallExpression) {
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

  @Override
  public boolean matches(String stepName) {
    Pattern perlPattern = getPattern();

    if (perlPattern != null) {
      try {
        final java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(perlPattern.getPattern());
        Matcher m = pattern.matcher(stepName);
        return m.matches();
      }
      catch (PatternSyntaxException ignored) {
      }
    }
    return false;
  }
}
