// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsRootNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsRootNodeMixin implements com.intellij.dts.lang.psi.DtsRootNode {

  public DtsRootNodeImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  public DtsRootNodeImpl(com.intellij.dts.lang.stubs.DtsRootNodeStub stub, com.intellij.psi.stubs.IStubElementType stubType) {
    super(stub, stubType);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsNodeContent getNodeContent() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsNodeContent.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsPHandle getPHandle() {
    return com.intellij.psi.util.PsiTreeUtil.getChildOfType(this, com.intellij.dts.lang.psi.DtsPHandle.class);
  }

}
