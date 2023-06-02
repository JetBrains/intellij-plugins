// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyGlobaltype;
import com.intellij.webassembly.lang.psi.WebAssemblyValtype;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyGlobaltypeImpl extends ASTWrapperPsiElement implements WebAssemblyGlobaltype {

  public WebAssemblyGlobaltypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitGlobaltype(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyValtype getValtype() {
    return findChildByClass(WebAssemblyValtype.class);
  }

}
