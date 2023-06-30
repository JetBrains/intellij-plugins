// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsIncludeStatementImpl extends com.intellij.dts.lang.psi.mixin.DtsIncludeStatementMixin implements com.intellij.dts.lang.psi.DtsIncludeStatement {

  public DtsIncludeStatementImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsString getString() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsString.class);
  }

}
