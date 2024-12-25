// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
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
import static org.jetbrains.plugins.cucumber.java.CucumberJavaUtil.getAllParameterTypes;

public abstract class AbstractJavaStepDefinition extends AbstractStepDefinition {
  private final Module myModule;

  public AbstractJavaStepDefinition(@NotNull PsiElement element,
                                    @NotNull Module module) {
    super(element);
    myModule = module;
  }

  protected Module getModule() {
    return myModule;
  }

  @Override
  public @Nullable String getCucumberRegex() {
    String definitionText = getExpression();
    if (definitionText == null) {
      return null;
    }
    PsiElement element = getElement();
    if (element == null) {
      return null;
    }

    if (!CucumberJavaVersionUtil.isCucumber3OrMore(myModule)) {
      return definitionText;
    }

    if (CucumberJavaUtil.isCucumberExpression(definitionText)) {
      ParameterTypeManager parameterTypes = getAllParameterTypes(myModule);
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
