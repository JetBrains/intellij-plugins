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

public class WebAssemblyGlobalImpl extends WebAssemblyNamedReferencedElementImpl implements WebAssemblyGlobal {

  public WebAssemblyGlobalImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitGlobal(this);
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

}
