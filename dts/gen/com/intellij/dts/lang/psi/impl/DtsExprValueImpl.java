// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsExprValueImpl extends com.intellij.dts.lang.psi.mixin.DtsExpressionMixin implements com.intellij.dts.lang.psi.DtsExprValue {

  public DtsExprValueImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsExpr getExpr() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsExpr.class);
  }

}
