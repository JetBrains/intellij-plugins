// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

// an element which declared in <cftag name = "CfmlNamedElement" ... >
public class CfmlNamedAttributeImpl extends CfmlAttributeNameImpl implements CfmlVariable {
  public CfmlNamedAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public static Icon getIcon() {
    return PlatformIcons.VARIABLE_ICON;
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
