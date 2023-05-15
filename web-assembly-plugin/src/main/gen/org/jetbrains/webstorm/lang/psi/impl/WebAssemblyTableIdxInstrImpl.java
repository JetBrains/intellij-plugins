// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*;
import org.jetbrains.webstorm.lang.psi.*;
import com.intellij.psi.PsiReference;

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
