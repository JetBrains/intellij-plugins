// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import com.intellij.webassembly.lang.psi.WebAssemblyIdx;
import com.intellij.webassembly.lang.psi.WebAssemblyTableIdxInstr;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyTableIdxInstrImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyTableIdxInstr {

  public WebAssemblyTableIdxInstrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitTableIdxInstr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyIdx getIdx() {
    return findChildByClass(WebAssemblyIdx.class);
  }

}
