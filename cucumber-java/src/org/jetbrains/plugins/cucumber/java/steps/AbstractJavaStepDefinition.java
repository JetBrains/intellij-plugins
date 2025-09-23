// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberUtil;
import org.jetbrains.plugins.cucumber.ParameterTypeManager;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractJavaStepDefinition extends AbstractStepDefinition {
  public AbstractJavaStepDefinition(@NotNull PsiElement element) {
    super(element);
  }

  @Override
  public @Nullable String getCucumberRegex() {
    String definitionText = getExpression();
    if (definitionText == null) return null;
    PsiElement element = getElement();
    if (element == null) return null;

    if (CucumberUtil.isCucumberExpression(definitionText)) {
      Module module = ModuleUtilCore.findModuleForPsiElement(element);
      ParameterTypeManager parameterTypes = CucumberJavaUtil.getAllParameterTypes(module);
      return CucumberUtil.buildRegexpFromCucumberExpression(definitionText, parameterTypes);
    }

    return definitionText;
  }
}
