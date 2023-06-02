// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.WebAssemblyElemlist;
import com.intellij.webassembly.lang.psi.WebAssemblyIdx;
import com.intellij.webassembly.lang.psi.WebAssemblyInstr;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyElemlistImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyElemlist {

  public WebAssemblyElemlistImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitElemlist(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<WebAssemblyIdx> getIdxList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyIdx.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyInstr> getInstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInstr.class);
  }

}
