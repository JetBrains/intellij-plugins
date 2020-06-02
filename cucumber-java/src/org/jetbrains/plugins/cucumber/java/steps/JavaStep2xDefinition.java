// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class JavaStep2xDefinition extends JavaAnnotatedStepDefinition {
  public JavaStep2xDefinition(@NotNull PsiElement element, @NotNull String annotationValue) {
    super(element, annotationValue);
  }
}
