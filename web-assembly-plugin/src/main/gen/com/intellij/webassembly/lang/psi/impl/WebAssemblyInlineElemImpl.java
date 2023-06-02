// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.WebAssemblyElemlist;
import com.intellij.webassembly.lang.psi.WebAssemblyInlineElem;
import com.intellij.webassembly.lang.psi.WebAssemblyInstr;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyInlineElemImpl extends ASTWrapperPsiElement implements WebAssemblyInlineElem {

  public WebAssemblyInlineElemImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitInlineElem(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyElemlist getElemlist() {
    return findChildByClass(WebAssemblyElemlist.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyInstr> getInstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInstr.class);
  }

}
