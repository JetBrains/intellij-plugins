// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.factory;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.steps.AbstractJavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStep2xDefinition;

public class JavaStep2xDefinitionFactory extends JavaStepDefinitionFactory {
  @Override
  public AbstractJavaStepDefinition buildStepDefinition(@NotNull PsiElement element,
                                                        @NotNull Module module,
                                                        @NotNull String annotationClassName) {
    return new JavaStep2xDefinition(element, module, annotationClassName);
  }
}
