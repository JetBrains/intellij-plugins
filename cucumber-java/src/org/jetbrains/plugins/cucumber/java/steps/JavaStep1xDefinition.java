// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class JavaStep1xDefinition extends JavaAnnotatedStepDefinition {
  public JavaStep1xDefinition(@NotNull PsiElement element, @NotNull Module module, @NotNull String annotationValue) {
    super(element, module, annotationValue);
  }

  @Nullable
  @Override
  public String getCucumberRegex() {
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
