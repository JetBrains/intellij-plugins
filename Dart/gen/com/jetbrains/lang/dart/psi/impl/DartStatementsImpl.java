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

public class DartStatementsImpl extends DartPsiCompositeElementImpl implements DartStatements {

  public DartStatementsImpl(ASTNode node) {
    super(node);
  }

  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof DartVisitor) ((DartVisitor)visitor).visitStatements(this);
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
  @NotNull
  public List<DartMetadata> getMetadataList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartMetadata.class);
  }

  @Override
  @NotNull
  public List<DartReturnStatement> getReturnStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartReturnStatement.class);
  }

  @Override
  @NotNull
  public List<DartSwitchStatement> getSwitchStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartSwitchStatement.class);
  }

  @Override
  @NotNull
  public List<DartThrowStatement> getThrowStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartThrowStatement.class);
  }

  @Override
  @NotNull
  public List<DartTryStatement> getTryStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartTryStatement.class);
  }

  @Override
  @NotNull
  public List<DartVarDeclarationList> getVarDeclarationListList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartVarDeclarationList.class);
  }

  @Override
  @NotNull
  public List<DartWhileStatement> getWhileStatementList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, DartWhileStatement.class);
  }

}
