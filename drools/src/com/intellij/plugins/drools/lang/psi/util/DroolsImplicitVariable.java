// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.util;


import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.light.LightVariableBuilder;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class DroolsImplicitVariable extends LightVariableBuilder implements ItemPresentation {

  private final @NotNull String myName;
  private final @NotNull PsiType myType;
  private final @NotNull PsiElement myElement;

  public DroolsImplicitVariable(@NotNull String name, @NotNull PsiType type, @NotNull PsiElement navigationElement) {
    super(name, type, navigationElement);
    myName = name;
    myType = type;
    myElement = navigationElement;
  }

  @Override
  public String getPresentableText() {
    return getName();
  }

  @Override
  public @Nullable Icon getIcon(boolean open) {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DroolsImplicitVariable variable)) return false;

    if (!myName.equals(variable.myName)) return false;
    if (!myType.equals(variable.myType)) return false;
    if (!myElement.equals(variable.myElement)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = myName.hashCode();
    result = 31 * result + myType.hashCode();
    result = 31 * result + myElement.hashCode();
    return result;
  }
}
