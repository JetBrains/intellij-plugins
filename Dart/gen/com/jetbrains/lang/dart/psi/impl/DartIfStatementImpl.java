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

public class DartIfStatementImpl extends DartPsiCompositeElementImpl implements DartIfStatement {

  public DartIfStatementImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull DartVisitor visitor) {
    visitor.visitIfStatement(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) accept((DartVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<DartAssertStatement> getAssertStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartAssertStatement.class);
  }

  @Override
  @NotNull
  public List<DartBlock> getBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartBlock.class);
  }

  @Override
  @NotNull
  public List<DartBreakStatement> getBreakStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartBreakStatement.class);
  }

  @Override
  @Nullable
  public DartConstantPattern getConstantPattern() {
    return findChildByClass(DartConstantPattern.class);
  }

  @Override
  @NotNull
  public List<DartContinueStatement> getContinueStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartContinueStatement.class);
  }

  @Override
  @NotNull
  public List<DartDoWhileStatement> getDoWhileStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartDoWhileStatement.class);
  }

  @Override
  @NotNull
  public List<DartExpression> getExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartExpression.class);
  }

  @Override
  @NotNull
  public List<DartForStatement> getForStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartForStatement.class);
  }

  @Override
  @NotNull
  public List<DartFunctionDeclarationWithBody> getFunctionDeclarationWithBodyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartFunctionDeclarationWithBody.class);
  }

  @Override
  @Nullable
  public DartIdentifierPattern getIdentifierPattern() {
    return findChildByClass(DartIdentifierPattern.class);
  }

  @Override
  @NotNull
  public List<DartIfStatement> getIfStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartIfStatement.class);
  }

  @Override
  @NotNull
  public List<DartLabel> getLabelList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartLabel.class);
  }

  @Override
  @Nullable
  public DartListPattern getListPattern() {
    return findChildByClass(DartListPattern.class);
  }

  @Override
  @Nullable
  public DartLogicalAndPattern getLogicalAndPattern() {
    return findChildByClass(DartLogicalAndPattern.class);
  }

  @Override
  @Nullable
  public DartLogicalOrPattern getLogicalOrPattern() {
    return findChildByClass(DartLogicalOrPattern.class);
  }

  @Override
  @Nullable
  public DartMapPattern getMapPattern() {
    return findChildByClass(DartMapPattern.class);
  }

  @Override
  @Nullable
  public DartObjectPattern getObjectPattern() {
    return findChildByClass(DartObjectPattern.class);
  }

  @Override
  @Nullable
  public DartParenthesizedPattern getParenthesizedPattern() {
    return findChildByClass(DartParenthesizedPattern.class);
  }

  @Override
  @NotNull
  public List<DartPatternAssignment> getPatternAssignmentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartPatternAssignment.class);
  }

  @Override
  @NotNull
  public List<DartPatternVariableDeclaration> getPatternVariableDeclarationList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartPatternVariableDeclaration.class);
  }

  @Override
  @Nullable
  public DartRecordPattern getRecordPattern() {
    return findChildByClass(DartRecordPattern.class);
  }

  @Override
  @Nullable
  public DartRelationalPattern getRelationalPattern() {
    return findChildByClass(DartRelationalPattern.class);
  }

  @Override
  @NotNull
  public List<DartRethrowStatement> getRethrowStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartRethrowStatement.class);
  }

  @Override
  @NotNull
  public List<DartReturnStatement> getReturnStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartReturnStatement.class);
  }

  @Override
  @NotNull
  public List<DartTryStatement> getTryStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartTryStatement.class);
  }

  @Override
  @Nullable
  public DartUnaryPattern getUnaryPattern() {
    return findChildByClass(DartUnaryPattern.class);
  }

  @Override
  @NotNull
  public List<DartVarDeclarationList> getVarDeclarationListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartVarDeclarationList.class);
  }

  @Override
  @Nullable
  public DartVariablePattern getVariablePattern() {
    return findChildByClass(DartVariablePattern.class);
  }

  @Override
  @NotNull
  public List<DartWhileStatement> getWhileStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartWhileStatement.class);
  }

  @Override
  @NotNull
  public List<DartYieldEachStatement> getYieldEachStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartYieldEachStatement.class);
  }

  @Override
  @NotNull
  public List<DartYieldStatement> getYieldStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartYieldStatement.class);
  }

}
