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

public class HCLForObjectExpressionImpl extends HCLForExpressionImpl implements HCLForObjectExpression {

  public HCLForObjectExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitForObjectExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof HCLElementVisitor) accept((HCLElementVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull HCLExpression getKey() {
    return HCLPsiImplUtilJ.getKey(this);
  }

  @Override
  public @NotNull HCLExpression getValue() {
    return HCLPsiImplUtilJ.getValue(this);
  }

  @Override
  public boolean isGrouping() {
    return HCLPsiImplUtilJ.isGrouping(this);
  }

}
