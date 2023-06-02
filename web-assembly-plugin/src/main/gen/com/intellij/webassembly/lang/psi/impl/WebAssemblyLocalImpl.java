// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.WebAssemblyLocal;
import com.intellij.webassembly.lang.psi.WebAssemblyValtype;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyLocalImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyLocal {

  public WebAssemblyLocalImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitLocal(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<WebAssemblyValtype> getValtypeList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyValtype.class);
  }

}
