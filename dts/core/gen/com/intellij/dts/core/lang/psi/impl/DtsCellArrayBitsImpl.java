// This is a generated file. Not intended for manual editing.
package com.intellij.dts.core.lang.psi.impl;

import static com.intellij.dts.core.lang.psi.DtsTypes.*;

public class DtsCellArrayBitsImpl extends com.intellij.dts.core.lang.psi.mixin.DtsCellArrayBitsMixin implements com.intellij.dts.core.lang.psi.DtsCellArrayBits {

  public DtsCellArrayBitsImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.core.lang.psi.DtsInt getInt() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.core.lang.psi.DtsInt.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.core.lang.psi.DtsPpMacro getPpMacro() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.core.lang.psi.DtsPpMacro.class);
  }

}
