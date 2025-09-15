// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.factory;

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.java.steps.AbstractJavaStepDefinition;
import org.jetbrains.plugins.cucumber.java.steps.JavaStep1xDefinition;

@NotNullByDefault
public class JavaStep1xDefinitionFactory extends JavaStepDefinitionFactory {
  @Override
  public AbstractJavaStepDefinition buildStepDefinition(PsiElement element,
                                                        String annotationClassName) {
    return new JavaStep1xDefinition(element, annotationClassName);
  }
}
