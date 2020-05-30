package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jetbrains.plugins.cucumber.CucumberUtil.buildRegexpFromCucumberExpression;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.JAVA_DEFAULT_PARAMETER_TYPE_MANAGER;
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.getAllParameterTypes;

public abstract class AbstractJavaStepDefinition extends AbstractStepDefinition {
  public AbstractJavaStepDefinition(@NotNull PsiElement element) {
    super(element);
  }

  @Nullable
  @Override
  public String getCucumberRegex() {
    String definitionText = getExpression();
    if (definitionText == null) {
      return null;
    }
    PsiElement element = getElement();
    if (element == null) {
      return null;
    }
    final Module module = ModuleUtilCore.findModuleForPsiElement(element);
    if (module != null) {
      if (!CucumberJavaVersionUtil.isCucumber3OrMore(element)) {
        return definitionText;
      }
    }

    if (CucumberJavaUtil.isCucumberExpression(definitionText)) {
      ParameterTypeManager parameterTypes = module != null ? getAllParameterTypes(module) : JAVA_DEFAULT_PARAMETER_TYPE_MANAGER;
      return buildRegexpFromCucumberExpression(definitionText, parameterTypes);
    }

    return definitionText;
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
}
