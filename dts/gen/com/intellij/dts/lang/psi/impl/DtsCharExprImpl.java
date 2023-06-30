// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsCharExprImpl extends com.intellij.dts.lang.psi.impl.DtsExprImpl implements com.intellij.dts.lang.psi.DtsCharExpr {

  public DtsCharExprImpl(@org.jetbrains.annotations.NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public com.intellij.dts.lang.psi.DtsChar getChar() {
    return notNullChild(com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsChar.class));
  }

}
