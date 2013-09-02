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

public class DartParenthesizedExpressionImpl extends DartClassReferenceImpl implements DartParenthesizedExpression {

  public DartParenthesizedExpressionImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitParenthesizedExpression(this);
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
  @Nullable
  public DartExpression getExpression() {
    return findChildByClass(DartExpression.class);
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
  @NotNull
  public List<DartMetadata> getMetadataList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMetadata.class);
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
  public DartThrowStatement getThrowStatement() {
    return findChildByClass(DartThrowStatement.class);
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

}
