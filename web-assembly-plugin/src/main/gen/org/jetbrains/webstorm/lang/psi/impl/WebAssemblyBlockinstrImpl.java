// This is a generated file. Not intended for manual editing.
package org.jetbrains.webstorm.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.jetbrains.webstorm.lang.psi.WebAssemblyTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import org.jetbrains.webstorm.lang.psi.*;

public class WebAssemblyBlockinstrImpl extends ASTWrapperPsiElement implements WebAssemblyBlockinstr {

  public WebAssemblyBlockinstrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitBlockinstr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyBlocktype getBlocktype() {
    return findChildByClass(WebAssemblyBlocktype.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyFoldeinstr> getFoldeinstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyFoldeinstr.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyInstr> getInstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInstr.class);
  }

}
