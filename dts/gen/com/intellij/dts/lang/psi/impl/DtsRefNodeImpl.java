// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import com.intellij.dts.lang.stubs.impl.DtsRefNodeStub;

public class DtsRefNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsRefNodeMixin implements com.intellij.dts.lang.psi.DtsRefNode {

  public DtsRefNodeImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  public DtsRefNodeImpl(DtsRefNodeStub stub, com.intellij.psi.stubs.IStubElementType stubType) {
    super(stub, stubType);
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
