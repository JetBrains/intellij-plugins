// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.DelegatePsiTarget;
import com.intellij.psi.PsiMethodCallExpression;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.java.CucumberJavaUtil;

@NotNullByDefault
public final class CucumberJavaLambdaStepPomTarget extends DelegatePsiTarget implements PomNamedTarget {
  public CucumberJavaLambdaStepPomTarget(PsiMethodCallExpression element) {
    super(element);
  }

  @Override
  public @Nullable String getName() {
    if (!(getNavigationElement() instanceof PsiMethodCallExpression methodCallExpression)) return null;
    return CucumberJavaUtil.getJava8StepName(methodCallExpression);
  }
}
