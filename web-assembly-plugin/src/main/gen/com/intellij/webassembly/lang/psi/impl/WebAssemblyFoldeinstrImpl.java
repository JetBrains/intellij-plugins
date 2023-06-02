// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyFoldeinstrImpl extends ASTWrapperPsiElement implements WebAssemblyFoldeinstr {

  public WebAssemblyFoldeinstrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitFoldeinstr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyBlocktype getBlocktype() {
    return findChildByClass(WebAssemblyBlocktype.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyFoldeinstr> getFoldeinstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyFoldeinstr.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyInstr> getInstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyPlaininstr getPlaininstr() {
    return findChildByClass(WebAssemblyPlaininstr.class);
  }

}
