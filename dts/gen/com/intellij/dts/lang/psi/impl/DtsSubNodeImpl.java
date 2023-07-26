// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import com.intellij.dts.lang.stubs.impl.DtsSubNodeStub;

public class DtsSubNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsSubNodeMixin implements com.intellij.dts.lang.psi.DtsSubNode {

  public DtsSubNodeImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  public DtsSubNodeImpl(DtsSubNodeStub stub, com.intellij.psi.stubs.IStubElementType stubType) {
    super(stub, stubType);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsNodeContent getNodeContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsNodeContent.class);
  }

}
