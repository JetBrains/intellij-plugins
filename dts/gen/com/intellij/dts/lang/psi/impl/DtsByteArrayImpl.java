// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsByteArrayImpl extends com.intellij.dts.lang.psi.mixin.DtsByteArrayMixin implements com.intellij.dts.lang.psi.DtsByteArray {

  public DtsByteArrayImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsByte> getByteList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsByte.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsPpMacro> getPpMacroList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsPpMacro.class);
  }

}
