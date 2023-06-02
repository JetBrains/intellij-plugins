// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyInstrImpl extends ASTWrapperPsiElement implements WebAssemblyInstr {

  public WebAssemblyInstrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitInstr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyBlockinstr getBlockinstr() {
    return findChildByClass(WebAssemblyBlockinstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyFoldeinstr getFoldeinstr() {
    return findChildByClass(WebAssemblyFoldeinstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyPlaininstr getPlaininstr() {
    return findChildByClass(WebAssemblyPlaininstr.class);
  }

}
