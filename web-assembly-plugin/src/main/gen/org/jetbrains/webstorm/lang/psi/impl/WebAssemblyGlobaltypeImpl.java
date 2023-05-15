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
