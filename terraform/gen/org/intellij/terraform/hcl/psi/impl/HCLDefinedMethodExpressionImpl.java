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

public class HCLDefinedMethodExpressionImpl extends HCLExpressionImpl implements HCLDefinedMethodExpression {

  public HCLDefinedMethodExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull HCLElementVisitor visitor) {
    visitor.visitDefinedMethodExpression(this);
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

}
