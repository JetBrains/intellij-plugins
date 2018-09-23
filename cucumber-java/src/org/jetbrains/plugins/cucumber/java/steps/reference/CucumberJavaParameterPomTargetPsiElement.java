// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.pom.PomNamedTarget;
import com.intellij.psi.DelegatePsiTarget;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class CucumberJavaParameterPomTargetPsiElement extends DelegatePsiTarget implements PomNamedTarget {
  @NotNull
  private final String myName;

  public CucumberJavaParameterPomTargetPsiElement(@NotNull PsiElement element, @NotNull String name) {
    super(element);
    myName = name;
  }

  @NotNull
  @Override
  public String getName() {
    return myName;
  }
}
