package com.intellij.tapestry.intellij.lang.descriptor;

import com.intellij.psi.PsiElement;

/**
 * @author Fedor.Korotkov
 */
public class TapestryMixinDescriptor extends BasicTapestryAttributeDescriptor {
  @Override
  public PsiElement getDeclaration() {
    return null;
  }

  @Override
  public String getName() {
    return "mixin";
  }
}
