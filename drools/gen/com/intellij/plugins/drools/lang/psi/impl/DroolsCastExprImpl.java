// This is a generated file. Not intended for manual editing.
package com.intellij.plugins.drools.lang.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.intellij.plugins.drools.lang.lexer.DroolsTokenTypes.*;
import com.intellij.plugins.drools.lang.psi.*;

public class DroolsCastExprImpl extends DroolsExpressionImpl implements DroolsCastExpr {

  public DroolsCastExprImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DroolsVisitor visitor) {
    visitor.visitCastExpr(this);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DroolsVisitor) accept((DroolsVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public DroolsExpression getExpression() {
    return findNotNullChildByClass(DroolsExpression.class);
  }

  @Override
  @Nullable
  public DroolsPrimitiveType getPrimitiveType() {
    return findChildByClass(DroolsPrimitiveType.class);
  }

  @Override
  @Nullable
  public DroolsType getType() {
    return findChildByClass(DroolsType.class);
  }

}
