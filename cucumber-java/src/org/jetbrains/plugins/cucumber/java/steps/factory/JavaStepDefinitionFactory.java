// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.factory;

import com.intellij.openapi.module.Module;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.java.CucumberJavaVersionUtil;
import org.jetbrains.plugins.cucumber.java.steps.AbstractJavaStepDefinition;

public abstract class JavaStepDefinitionFactory {
  public static JavaStepDefinitionFactory getInstance(@NotNull Module module) {
    if (!CucumberJavaVersionUtil.isCucumber2OrMore(module)) {
      return new JavaStep1xDefinitionFactory();
    }
    return new JavaStep2xDefinitionFactory();
  }
  
  public abstract AbstractJavaStepDefinition buildStepDefinition(@NotNull PsiElement element,
                                                                 @NotNull Module module,
                                                                 @NotNull String annotationClassName);
}
