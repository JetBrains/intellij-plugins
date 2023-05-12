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

public class WebAssemblyImportdescImpl extends ASTWrapperPsiElement implements WebAssemblyImportdesc {

  public WebAssemblyImportdescImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitImportdesc(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyGlobaltype getGlobaltype() {
    return findChildByClass(WebAssemblyGlobaltype.class);
  }

  @Override
  @Nullable
  public WebAssemblyMemtype getMemtype() {
    return findChildByClass(WebAssemblyMemtype.class);
  }

  @Override
  @Nullable
  public WebAssemblyTabletype getTabletype() {
    return findChildByClass(WebAssemblyTabletype.class);
  }

  @Override
  @Nullable
  public WebAssemblyTypeuse getTypeuse() {
    return findChildByClass(WebAssemblyTypeuse.class);
  }

}
