// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsRootNodeImpl extends com.intellij.dts.lang.psi.mixin.DtsRootNodeMixin implements com.intellij.dts.lang.psi.DtsRootNode {

  public DtsRootNodeImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsNodeContent getNodeContent() {
    return findChildByClass(com.intellij.dts.lang.psi.DtsNodeContent.class);
  }

  @java.lang.Override
  @org.jetbrains.annotations.Nullable
  public com.intellij.dts.lang.psi.DtsPHandle getPHandle() {
    return findChildByClass(com.intellij.dts.lang.psi.DtsPHandle.class);
  }

}
