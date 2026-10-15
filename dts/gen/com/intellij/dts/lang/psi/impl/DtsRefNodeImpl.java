// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsRefNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsRefNodeMixin implements com.intellij.dts.lang.psi.DtsRefNode {

  public DtsRefNodeImpl(com.intellij.lang.@org.jetbrains.annotations.NotNull ASTNode node) {
    super(node);
  }

  public DtsRefNodeImpl(com.intellij.dts.lang.stubs.impl.@org.jetbrains.annotations.NotNull DtsRefNodeStub stub, com.intellij.psi.stubs.@org.jetbrains.annotations.NotNull IStubElementType<?,?> type) {
    super(stub, type);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsNodeContent getNodeContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsNodeContent.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public com.intellij.dts.lang.psi.DtsPHandle getPHandle() {
    return notNullChild(com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsPHandle.class));
  }

}
