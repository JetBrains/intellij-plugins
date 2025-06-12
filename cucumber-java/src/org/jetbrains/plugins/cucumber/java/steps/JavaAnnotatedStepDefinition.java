// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

public class JavaAnnotatedStepDefinition extends AbstractJavaStepDefinition {
  private final @NotNull String myAnnotationValue;

  public JavaAnnotatedStepDefinition(@NotNull PsiElement stepDef, @NotNull Module module, @NotNull String annotationValue) {
    super(stepDef, module);
    myAnnotationValue = annotationValue;
  }

  @Override
  protected @Nullable String getCucumberRegexFromElement(PsiElement element) {
    if (element == null) {
      return null;
    }

    if (!(element instanceof PsiMethod)) {
      return null;
    }
    if (myAnnotationValue.length() > 1) {
      return CucumberJavaUtil.escapeCucumberRegex(myAnnotationValue);
    }
    return null;
  }

  @Override
  public String toString() {
    return "JavaAnnotatedStepDefinition{stepDef: " + getElement() + "}";
  }
}
