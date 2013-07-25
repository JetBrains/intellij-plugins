package com.intellij.coldFusion.model.psi.impl;

import com.intellij.coldFusion.model.psi.CfmlVariable;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiType;
import com.intellij.util.PlatformIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: vnikolaenko
 * Date: 29.04.2009
 */
// an element which declared in <cftag name = "CfmlNamedElement" ... >
public class CfmlNamedAttributeImpl extends CfmlAttributeNameImpl implements CfmlVariable {
  public CfmlNamedAttributeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public static Icon getIcon() {
    return PlatformIcons.VARIABLE_ICON;
  }

  public PsiType getPsiType() {
    return null;
  }

  public boolean isTrulyDeclaration() {
    return true;
  }

  @NotNull
  public String getlookUpString() {
    return getName();
  }
}
