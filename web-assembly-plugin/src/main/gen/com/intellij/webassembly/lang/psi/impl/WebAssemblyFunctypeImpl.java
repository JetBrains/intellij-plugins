// This is a generated file. Not intended for manual editing.
package com.intellij.webassembly.lang.psi.impl;

import java.util.List;

import com.intellij.webassembly.lang.psi.WebAssemblyFunctype;
import com.intellij.webassembly.lang.psi.WebAssemblyParam;
import com.intellij.webassembly.lang.psi.WebAssemblyResult;
import com.intellij.webassembly.lang.psi.WebAssemblyVisitor;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.webassembly.lang.psi.*;

public class WebAssemblyFunctypeImpl extends ASTWrapperPsiElement implements WebAssemblyFunctype {

  public WebAssemblyFunctypeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitFunctype(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<WebAssemblyParam> getParamList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyParam.class);
  }

  @Override
  @NotNull
  public List<WebAssemblyResult> getResultList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, WebAssemblyResult.class);
  }

}
