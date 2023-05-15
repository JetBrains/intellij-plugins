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

public class DartCallExpressionImpl extends DartReferenceImpl implements DartCallExpression {

  public DartCallExpressionImpl(ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitCallExpression(this);
  }

  @Override
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
  public DartSwitchExpressionWrapper getSwitchExpressionWrapper() {
    return findChildByClass(DartSwitchExpressionWrapper.class);
  }

  @Override
  @NotNull
  public List<DartTypeArguments> getTypeArgumentsList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartTypeArguments.class);
  }

  @Override
  public @Nullable DartArguments getArguments() {
    return DartPsiImplUtil.getArguments(this);
  }

}
