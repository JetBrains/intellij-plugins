// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.ui.IconManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

// an element which declared in <cftag name = "CfmlNamedElement" ... >
public class CfmlNamedAttributeImpl extends CfmlAttributeNameImpl implements CfmlVariable {
  public CfmlNamedAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public static Icon getIcon() {
    return IconManager.getInstance().getPlatformIcon(com.intellij.ui.PlatformIcons.Variable);
  }

  @Override
  public PsiType getPsiType() {
    return null;
  }

  @Override
  public boolean isTrulyDeclaration() {
    return true;
  }

  @Override
  @NotNull
  public String getlookUpString() {
    return getName();
  }
}
