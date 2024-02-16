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

public class ILTemplateIfBlockExpressionImpl extends ILExpressionImpl implements ILTemplateIfBlockExpression {

  public ILTemplateIfBlockExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ILGeneratedVisitor visitor) {
    visitor.visitILTemplateIfBlockExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ILGeneratedVisitor) accept((ILGeneratedVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ElseBranch getElseBranch() {
    return findChildByClass(ElseBranch.class);
  }

  @Override
  @Nullable
  public EndIfBranch getEndIfBranch() {
    return findChildByClass(EndIfBranch.class);
  }

  @Override
  @NotNull
  public IfBranch getIfBranch() {
    return findNotNullChildByClass(IfBranch.class);
  }

}
