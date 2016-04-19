// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.jetbrains.lang.dart.DartTokenTypes.*;
import com.jetbrains.lang.dart.psi.*;
import com.jetbrains.lang.dart.util.DartPsiImplUtil;

public class DartNormalFormalParameterImpl extends DartPsiCompositeElementImpl implements DartNormalFormalParameter {

  public DartNormalFormalParameterImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitNormalFormalParameter(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartFieldFormalParameter getFieldFormalParameter() {
    return findChildByClass(DartFieldFormalParameter.class);
  }

  @Override
  @Nullable
  public DartFunctionSignature getFunctionSignature() {
    return findChildByClass(DartFunctionSignature.class);
  }

  @Override
  @Nullable
  public DartSimpleFormalParameter getSimpleFormalParameter() {
    return findChildByClass(DartSimpleFormalParameter.class);
  }

  @Nullable
  public DartComponentName findComponentName() {
    return DartPsiImplUtil.findComponentName(this);
  }

}
