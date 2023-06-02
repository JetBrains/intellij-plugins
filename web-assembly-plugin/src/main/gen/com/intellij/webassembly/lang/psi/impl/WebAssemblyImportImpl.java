// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyImport;
import com.intellij.webassembly.lang.psi.WebAssemblyImportdesc;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyImportImpl extends WebAssemblyNamedImportImpl implements WebAssemblyImport {

  public WebAssemblyImportImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitImport(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyImportdesc getImportdesc() {
    return findChildByClass(WebAssemblyImportdesc.class);
  }

}
