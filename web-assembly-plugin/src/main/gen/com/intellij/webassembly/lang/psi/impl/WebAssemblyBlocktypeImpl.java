// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyBlocktype;
import com.intellij.webassembly.lang.psi.WebAssemblyResult;
import com.intellij.webassembly.lang.psi.WebAssemblyTypeuse;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyBlocktypeImpl extends ASTWrapperPsiElement implements WebAssemblyBlocktype {

  public WebAssemblyBlocktypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitBlocktype(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyResult getResult() {
    return findChildByClass(WebAssemblyResult.class);
  }

  @Override
  @Nullable
  public WebAssemblyTypeuse getTypeuse() {
    return findChildByClass(WebAssemblyTypeuse.class);
  }

}
