// This is a generated file. Not intended for manual editing.
package org.intellij.prisma.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static org.intellij.prisma.lang.psi.PrismaElementTypes.*;
import org.intellij.prisma.lang.psi.*;

public class PrismaFunctionCallImpl extends PrismaExpressionImpl implements PrismaFunctionCall {

  public PrismaFunctionCallImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull PrismaVisitor visitor) {
    visitor.visitFunctionCall(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof PrismaVisitor) accept((PrismaVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PrismaArgumentsList getArgumentsList() {
    return findNotNullChildByClass(PrismaArgumentsList.class);
  }

  @Override
  @NotNull
  public PrismaPathExpression getPathExpression() {
    return findNotNullChildByClass(PrismaPathExpression.class);
  }

}
