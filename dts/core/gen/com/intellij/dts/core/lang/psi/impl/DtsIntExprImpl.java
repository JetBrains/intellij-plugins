// This is a generated file. Not intended for manual editing.
package com.intellij.dts.core.lang.psi.impl;

import static com.intellij.dts.core.lang.psi.DtsTypes.*;

public class DtsIntExprImpl extends com.intellij.dts.core.lang.psi.impl.DtsExprImpl implements com.intellij.dts.core.lang.psi.DtsIntExpr {

  public DtsIntExprImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public com.intellij.dts.core.lang.psi.DtsInt getInt() {
    return notNullChild(com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.core.lang.psi.DtsInt.class));
  }

}
