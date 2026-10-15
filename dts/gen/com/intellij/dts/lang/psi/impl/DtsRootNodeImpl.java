// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsRootNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsRootNodeMixin implements com.intellij.dts.lang.psi.DtsRootNode {

  public DtsRootNodeImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  public DtsRootNodeImpl(com.intellij.dts.lang.stubs.impl.@org.jetbrains.annotations.NotNull DtsRootNodeStub stub, com.intellij.psi.stubs.@org.jetbrains.annotations.NotNull IStubElementType<?,?> type) {
    super(stub, type);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsNodeContent getNodeContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsNodeContent.class);
  }

}
