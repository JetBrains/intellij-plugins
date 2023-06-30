// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsPropertyImpl extends com.intellij.dts.lang.psi.mixin.DtsPropertyMixin implements com.intellij.dts.lang.psi.DtsProperty {

  public DtsPropertyImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsPropertyContent getPropertyContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsPropertyContent.class);
  }

}
