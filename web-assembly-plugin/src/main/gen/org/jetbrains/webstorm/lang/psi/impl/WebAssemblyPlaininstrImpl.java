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

public class WebAssemblyPlaininstrImpl extends ASTWrapperPsiElement implements WebAssemblyPlaininstr {

  public WebAssemblyPlaininstrImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull WebAssemblyVisitor visitor) {
    visitor.visitPlaininstr(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof WebAssemblyVisitor) accept((WebAssemblyVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public WebAssemblyAligneq getAligneq() {
    return findChildByClass(WebAssemblyAligneq.class);
  }

  @Override
  @Nullable
  public WebAssemblyCallIndirectInstr getCallIndirectInstr() {
    return findChildByClass(WebAssemblyCallIndirectInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyCallInstr getCallInstr() {
    return findChildByClass(WebAssemblyCallInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyElemDropInstr getElemDropInstr() {
    return findChildByClass(WebAssemblyElemDropInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyGlobalInstr getGlobalInstr() {
    return findChildByClass(WebAssemblyGlobalInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyIdx getIdx() {
    return findChildByClass(WebAssemblyIdx.class);
  }

  @Override
  @Nullable
  public WebAssemblyLocalInstr getLocalInstr() {
    return findChildByClass(WebAssemblyLocalInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyMemoryIdxInstr getMemoryIdxInstr() {
    return findChildByClass(WebAssemblyMemoryIdxInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyOffseteq getOffseteq() {
    return findChildByClass(WebAssemblyOffseteq.class);
  }

  @Override
  @Nullable
  public WebAssemblyRefFuncInstr getRefFuncInstr() {
    return findChildByClass(WebAssemblyRefFuncInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyTableCopyInstr getTableCopyInstr() {
    return findChildByClass(WebAssemblyTableCopyInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyTableIdxInstr getTableIdxInstr() {
    return findChildByClass(WebAssemblyTableIdxInstr.class);
  }

  @Override
  @Nullable
  public WebAssemblyTableInitInstr getTableInitInstr() {
    return findChildByClass(WebAssemblyTableInitInstr.class);
  }

}
