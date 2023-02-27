// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hil.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hil.HILElementTypes.*;
import org.intellij.terraform.hil.psi.*;
import com.intellij.psi.PsiReference;

public class ILSelectExpressionImpl extends ILExpressionImpl implements ILSelectExpression {

  public ILSelectExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ILGeneratedVisitor visitor) {
    visitor.visitILSelectExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ILGeneratedVisitor) accept((ILGeneratedVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ILExpression getFrom() {
    List<ILExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, ILExpression.class);
    return p1.get(0);
  }

  @Override
  @Nullable
  public ILExpression getField() {
    List<ILExpression> p1 = PsiTreeUtil.getChildrenOfTypeAsList(this, ILExpression.class);
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  public @Nullable PsiReference getReference() {
    return HILPsiImplUtilJ.getReference(this);
  }

  @Override
  public PsiReference @NotNull [] getReferences() {
    return HILPsiImplUtilJ.getReferences(this);
  }

}
