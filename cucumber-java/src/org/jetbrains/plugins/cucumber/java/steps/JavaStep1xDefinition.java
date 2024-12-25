// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaStep1xDefinition extends JavaAnnotatedStepDefinition {
  public JavaStep1xDefinition(@NotNull PsiElement element, @NotNull Module module, @NotNull String annotationValue) {
    super(element, module, annotationValue);
  }

  @Override
  public @Nullable String getCucumberRegex() {
    String result = super.getCucumberRegex();
    if (result != null) {
      if (!result.startsWith("^")) {
        result = "^" + result;
      }
      if (!result.endsWith("$")) {
        result += "$";
      }
    }
    return result;
  }
}
