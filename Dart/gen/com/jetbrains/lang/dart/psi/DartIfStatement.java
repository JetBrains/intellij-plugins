// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartIfStatement extends DartPsiCompositeElement {

  @NotNull
  List<DartAssertStatement> getAssertStatementList();

  @NotNull
  List<DartBlock> getBlockList();

  @NotNull
  List<DartBreakStatement> getBreakStatementList();

  @Nullable
  DartConstantPattern getConstantPattern();

  @NotNull
  List<DartContinueStatement> getContinueStatementList();

  @NotNull
  List<DartDoWhileStatement> getDoWhileStatementList();

  @NotNull
  List<DartExpression> getExpressionList();

  @NotNull
  List<DartForStatement> getForStatementList();

  @NotNull
  List<DartFunctionDeclarationWithBody> getFunctionDeclarationWithBodyList();

  @Nullable
  DartIdentifierPattern getIdentifierPattern();

  @NotNull
  List<DartIfStatement> getIfStatementList();

  @NotNull
  List<DartLabel> getLabelList();

  @Nullable
  DartListPattern getListPattern();

  @Nullable
  DartLogicalAndPattern getLogicalAndPattern();

  @Nullable
  DartLogicalOrPattern getLogicalOrPattern();

  @Nullable
  DartMapPattern getMapPattern();

  @Nullable
  DartObjectPattern getObjectPattern();

  @Nullable
  DartParenthesizedPattern getParenthesizedPattern();

  @NotNull
  List<DartPatternAssignment> getPatternAssignmentList();

  @NotNull
  List<DartPatternVariableDeclaration> getPatternVariableDeclarationList();

  @Nullable
  DartRecordPattern getRecordPattern();

  @Nullable
  DartRelationalPattern getRelationalPattern();

  @NotNull
  List<DartRethrowStatement> getRethrowStatementList();

  @NotNull
  List<DartReturnStatement> getReturnStatementList();

  @NotNull
  List<DartTryStatement> getTryStatementList();

  @Nullable
  DartUnaryPattern getUnaryPattern();

  @NotNull
  List<DartVarDeclarationList> getVarDeclarationListList();

  @Nullable
  DartVariablePattern getVariablePattern();

  @NotNull
  List<DartWhileStatement> getWhileStatementList();

  @NotNull
  List<DartYieldEachStatement> getYieldEachStatementList();

  @NotNull
  List<DartYieldStatement> getYieldStatementList();

}
