package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class AbstractJavaStepDefinition extends AbstractStepDefinition {
  public AbstractJavaStepDefinition(@NotNull PsiElement element) {
    super(element);
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

  @Override
  public boolean matches(@NotNull String stepName) {
    Pattern pattern = getPattern();
    if (pattern != null) {
        Matcher m = pattern.matcher(stepName);
        return m.matches();
    }
    return false;
  }
}
