// This is a generated file. Not intended for manual editing.
package com.intellij.dts.lang.psi.impl;

import static com.intellij.dts.lang.psi.DtsTypes.*;

public class DtsContentImpl extends com.intellij.extapi.psi.ASTWrapperPsiElement implements com.intellij.dts.lang.psi.DtsContent {

  public DtsContentImpl(@org.jetbrains.annotations.NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.lang.psi.DtsEntry> getEntryList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.lang.psi.DtsEntry.class);
  }

}
