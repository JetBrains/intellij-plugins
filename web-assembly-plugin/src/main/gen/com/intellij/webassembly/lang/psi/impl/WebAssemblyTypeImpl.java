// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyFunctype;
import com.intellij.webassembly.lang.psi.WebAssemblyType;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyTypeImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyType {

  public WebAssemblyTypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitType(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyFunctype getFunctype() {
    return findChildByClass(WebAssemblyFunctype.class);
  }

}
