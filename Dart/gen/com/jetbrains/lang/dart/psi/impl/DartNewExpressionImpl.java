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

public class DartNewExpressionImpl extends DartReferenceImpl implements DartNewExpression {

  public DartNewExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitNewExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartReferenceExpression> getReferenceExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartReferenceExpression.class);
  }

  @Override
  @Nullable
  public DartType getType() {
    return findChildByClass(DartType.class);
  }

  @Override
  @Nullable
  public DartTypeArguments getTypeArguments() {
    return findChildByClass(DartTypeArguments.class);
  }

  @Override
  public boolean isConstantObjectExpression() {
    return DartPsiImplUtil.isConstantObjectExpression(this);
  }

  @Override
  public @Nullable DartArguments getArguments() {
    return DartPsiImplUtil.getArguments(this);
  }

}
