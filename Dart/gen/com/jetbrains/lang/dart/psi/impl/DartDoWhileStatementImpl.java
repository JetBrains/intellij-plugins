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

public class DartDoWhileStatementImpl extends DartPsiCompositeElementImpl implements DartDoWhileStatement {

  public DartDoWhileStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitDoWhileStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public DartAssertStatement getAssertStatement() {
    return findChildByClass(DartAssertStatement.class);
  }

  @Override
  @Nullable
  public DartBlock getBlock() {
    return findChildByClass(DartBlock.class);
  }

  @Override
  @Nullable
  public DartBreakStatement getBreakStatement() {
    return findChildByClass(DartBreakStatement.class);
  }

  @Override
  @Nullable
  public DartContinueStatement getContinueStatement() {
    return findChildByClass(DartContinueStatement.class);
  }

  @Override
  @Nullable
  public DartDoWhileStatement getDoWhileStatement() {
    return findChildByClass(DartDoWhileStatement.class);
  }

  @Override
  @NotNull
  public List<DartExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartExpression.class);
  }

  @Override
  @Nullable
  public DartForStatement getForStatement() {
    return findChildByClass(DartForStatement.class);
  }

  @Override
  @Nullable
  public DartFunctionDeclarationWithBody getFunctionDeclarationWithBody() {
    return findChildByClass(DartFunctionDeclarationWithBody.class);
  }

  @Override
  @Nullable
  public DartIfStatement getIfStatement() {
    return findChildByClass(DartIfStatement.class);
  }

  @Override
  @NotNull
  public List<DartLabel> getLabelList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartLabel.class);
  }

  @Override
  @Nullable
  public DartRethrowStatement getRethrowStatement() {
    return findChildByClass(DartRethrowStatement.class);
  }

  @Override
  @Nullable
  public DartReturnStatement getReturnStatement() {
    return findChildByClass(DartReturnStatement.class);
  }

  @Override
  @Nullable
  public DartSwitchStatement getSwitchStatement() {
    return findChildByClass(DartSwitchStatement.class);
  }

  @Override
  @Nullable
  public DartTryStatement getTryStatement() {
    return findChildByClass(DartTryStatement.class);
  }

  @Override
  @Nullable
  public DartVarDeclarationList getVarDeclarationList() {
    return findChildByClass(DartVarDeclarationList.class);
  }

  @Override
  @Nullable
  public DartWhileStatement getWhileStatement() {
    return findChildByClass(DartWhileStatement.class);
  }

  @Override
  @Nullable
  public DartYieldEachStatement getYieldEachStatement() {
    return findChildByClass(DartYieldEachStatement.class);
  }

  @Override
  @Nullable
  public DartYieldStatement getYieldStatement() {
    return findChildByClass(DartYieldStatement.class);
  }

}
