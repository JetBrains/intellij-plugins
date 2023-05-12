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

public class WebAssemblyModulefieldImpl extends ASTWrapperPsiElement implements WebAssemblyModulefield {

  public WebAssemblyModulefieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitModulefield(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyData getData() {
    return findChildByClass(WebAssemblyData.class);
  }

  @Override
  @Nullable
  public WebAssemblyElem getElem() {
    return findChildByClass(WebAssemblyElem.class);
  }

  @Override
  @Nullable
  public WebAssemblyExport getExport() {
    return findChildByClass(WebAssemblyExport.class);
  }

  @Override
  @Nullable
  public WebAssemblyFunc getFunc() {
    return findChildByClass(WebAssemblyFunc.class);
  }

  @Override
  @Nullable
  public WebAssemblyGlobal getGlobal() {
    return findChildByClass(WebAssemblyGlobal.class);
  }

  @Override
  @Nullable
  public WebAssemblyImport getImport() {
    return findChildByClass(WebAssemblyImport.class);
  }

  @Override
  @Nullable
  public WebAssemblyMem getMem() {
    return findChildByClass(WebAssemblyMem.class);
  }

  @Override
  @Nullable
  public WebAssemblyStart getStart() {
    return findChildByClass(WebAssemblyStart.class);
  }

  @Override
  @Nullable
  public WebAssemblyTable getTable() {
    return findChildByClass(WebAssemblyTable.class);
  }

  @Override
  @Nullable
  public WebAssemblyType getType() {
    return findChildByClass(WebAssemblyType.class);
  }

}
