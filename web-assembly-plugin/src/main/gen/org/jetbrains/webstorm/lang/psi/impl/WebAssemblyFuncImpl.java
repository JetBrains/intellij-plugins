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

public class WebAssemblyFuncImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyFunc {

  public WebAssemblyFuncImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitFunc(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
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
  @NotNull
  public List<WebAssemblyInstr> getInstrList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyInstr.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyLocal> getLocalList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyLocal.class);
  }

  @Override
  @Nullable
  public WebAssemblyTypeuse getTypeuse() {
    return findChildByClass(WebAssemblyTypeuse.class);
  }

}
