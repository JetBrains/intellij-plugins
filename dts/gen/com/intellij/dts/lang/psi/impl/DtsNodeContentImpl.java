// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsNodeContentImpl extends com.intellij.dts.lang.psi.mixin.DtsNodeContentMixin implements com.intellij.dts.lang.psi.DtsNodeContent {

  public DtsNodeContentImpl(com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsEntry> getEntryList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsEntry.class);
  }

}
