// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyMemImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyMem {

  public WebAssemblyMemImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitMem(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyInlineData getInlineData() {
    return findChildByClass(WebAssemblyInlineData.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyInlineExport> getInlineExportList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInlineExport.class);
  }

  @Override
  @Nullable
  public WebAssemblyInlineImport getInlineImport() {
    return findChildByClass(WebAssemblyInlineImport.class);
  }

  @Override
  @Nullable
  public WebAssemblyMemtype getMemtype() {
    return findChildByClass(WebAssemblyMemtype.class);
  }

}
