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

public class DartForLoopPartsImpl extends DartPsiCompositeElementImpl implements DartForLoopParts {

  public DartForLoopPartsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitForLoopParts(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
  }

  @Override
  @Nullable
  public DartExpressionList getExpressionList() {
    return findChildByClass(DartExpressionList.class);
  }

  @Override
  @Nullable
  public DartForInPart getForInPart() {
    return findChildByClass(DartForInPart.class);
  }

  @Override
  @Nullable
  public DartVarDeclarationList getVarDeclarationList() {
    return findChildByClass(DartVarDeclarationList.class);
  }

}
