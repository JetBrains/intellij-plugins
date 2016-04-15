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

public class DartUserDefinableOperatorImpl extends DartPsiCompositeElementImpl implements DartUserDefinableOperator {

  public DartUserDefinableOperatorImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitUserDefinableOperator(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartAdditiveOperator getAdditiveOperator() {
    return findChildByClass(DartAdditiveOperator.class);
  }

  @Override
  @Nullable
  public DartBitwiseOperator getBitwiseOperator() {
    return findChildByClass(DartBitwiseOperator.class);
  }

  @Override
  @Nullable
  public DartMultiplicativeOperator getMultiplicativeOperator() {
    return findChildByClass(DartMultiplicativeOperator.class);
  }

  @Override
  @Nullable
  public DartRelationalOperator getRelationalOperator() {
    return findChildByClass(DartRelationalOperator.class);
  }

  @Override
  @Nullable
  public DartShiftOperator getShiftOperator() {
    return findChildByClass(DartShiftOperator.class);
  }

}
