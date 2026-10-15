// This is a generated file. Not intended for manual editing.
package com.intellij.dts.core.lang.psi.impl;

import static com.intellij.dts.core.lang.psi.DtsTypes.*;

public class DtsLeqExprImpl extends com.intellij.dts.core.lang.psi.impl.DtsExprImpl implements com.intellij.dts.core.lang.psi.DtsLeqExpr {

  public DtsLeqExprImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.core.lang.psi.DtsExpr> getExprList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.core.lang.psi.DtsExpr.class);
  }

}
