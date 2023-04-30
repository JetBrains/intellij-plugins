// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsCellImpl extends com.intellij.extapi.psi.ASTWrapperPsiElement implements com.intellij.dts.lang.psi.DtsCell {

  public DtsCellImpl(@org.jetbrains.annotations.NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsChar getChar() {
    return findChildByClass(com.intellij.dts.lang.psi.DtsChar.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsExpr getExpr() {
    return findChildByClass(com.intellij.dts.lang.psi.DtsExpr.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsPpMacro getPpMacro() {
    return findChildByClass(com.intellij.dts.lang.psi.DtsPpMacro.class);
  }

}
