package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
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
    if (stepAnnotation.getParameterList().getAttributes().length > 0) {
      final PsiElement annotationValue = stepAnnotation.getParameterList().getAttributes()[0].getValue();
      if (annotationValue != null) {
        final PsiElement patternLiteral = annotationValue.getFirstChild();
        if (patternLiteral != null) {
          final String patternContainer = patternLiteral.getText();
          pattern =  patternContainer.substring(1, patternContainer.length() - 1).replace("\\\\", "\\").replace("\\\"", "\"");
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
