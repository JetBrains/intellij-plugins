// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyOffseteq;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyOffseteqImpl extends ASTWrapperPsiElement implements WebAssemblyOffseteq {

  public WebAssemblyOffseteqImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitOffseteq(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

}
