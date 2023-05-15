// This is a generated file. Not intended for manual editing.
package org.intellij.terraform.hcl.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.terraform.hcl.HCLElementTypes.*;
import org.intellij.terraform.hcl.psi.*;

public class HCLConditionalExpressionImpl extends HCLExpressionImpl implements HCLConditionalExpression {

  public HCLConditionalExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitConditionalExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<HCLExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, HCLExpression.class);
  }

  @Override
  @NotNull
  public HCLExpression getCondition() {
    List<HCLExpression> p1 = getExpressionList();
    return p1.get(0);
  }

  @Override
  @Nullable
  public HCLExpression getThen() {
    List<HCLExpression> p1 = getExpressionList();
    return p1.size() < 2 ? null : p1.get(1);
  }

  @Override
  @Nullable
  public HCLExpression getOtherwise() {
    List<HCLExpression> p1 = getExpressionList();
    return p1.size() < 3 ? null : p1.get(2);
  }

}
