// This is a generated file. Not intended for manual editing.
package com.intellij.dts.core.lang.psi.impl;

import static com.intellij.dts.core.lang.psi.DtsTypes.*;

public class DtsPropertyImpl extends com.intellij.dts.core.lang.psi.mixin.DtsPropertyMixin implements com.intellij.dts.core.lang.psi.DtsProperty {

  public DtsPropertyImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.core.lang.psi.DtsPropertyContent getPropertyContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.core.lang.psi.DtsPropertyContent.class);
  }

}
