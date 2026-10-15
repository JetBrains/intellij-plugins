// This is a generated file. Not intended for manual editing.
package com.intellij.dts.pp.test.impl.psi;

import static com.intellij.dts.pp.test.impl.psi.TestTypes.*;

public class TestSentenceImpl extends com.intellij.extapi.psi.ASTWrapperPsiElement implements com.intellij.dts.pp.test.impl.psi.TestSentence {

  public TestSentenceImpl(@org.jetbrains.annotations.NotNull com.intellij.lang.ASTNode node) {
    super(node);
  }

  @java.lang.Override
  @org.jetbrains.annotations.NotNull
  public java.util.List<com.intellij.dts.pp.test.impl.psi.TestQuote> getQuoteList() {
    return com.intellij.psi.util.PsiTreeUtil.getChildrenOfTypeAsList(this, com.intellij.dts.pp.test.impl.psi.TestQuote.class);
  }

}
