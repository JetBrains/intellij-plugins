// This is a generated file. Not intended for manual editing.
package com.intellij.dts.core.lang.psi.impl;

import static com.intellij.dts.core.lang.psi.DtsTypes.*;

public class DtsSubNodeImpl extends com.intellij.dts.core.lang.psi.mixin.DtsSubNodeMixin implements com.intellij.dts.core.lang.psi.DtsSubNode {

  public DtsSubNodeImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  public DtsSubNodeImpl(com.intellij.dts.core.lang.stubs.impl.@org.jetbrains.annotations.NotNull DtsSubNodeStub stub, com.intellij.psi.stubs.@org.jetbrains.annotations.NotNull IStubElementType<?,?> type) {
    super(stub, type);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.core.lang.psi.DtsNodeContent getNodeContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.core.lang.psi.DtsNodeContent.class);
  }

}
