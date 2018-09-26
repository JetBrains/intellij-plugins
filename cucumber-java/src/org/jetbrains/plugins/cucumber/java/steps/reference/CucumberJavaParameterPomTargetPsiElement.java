// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.java.steps.reference;

import com.intellij.openapi.util.TextRange;
import com.intellij.pom.PomNamedTarget;
import com.intellij.pom.PsiDeclaredTarget;
import com.intellij.psi.DelegatePsiTarget;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CucumberJavaParameterPomTargetPsiElement extends DelegatePsiTarget implements PomNamedTarget, PsiDeclaredTarget {
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

  @Nullable
  @Override
  public TextRange getNameIdentifierRange() {
    return TextRange.create(1, getNavigationElement().getTextLength() - 1);
  }
}
