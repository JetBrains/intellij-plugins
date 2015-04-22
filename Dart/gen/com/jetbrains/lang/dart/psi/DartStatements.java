// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartStatements extends DartExecutionScope {

  @NotNull
  List<DartAssertStatement> getAssertStatementList();

  @NotNull
  List<DartBlock> getBlockList();

  @NotNull
  List<DartBreakStatement> getBreakStatementList();

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

  @NotNull
  List<DartIfStatement> getIfStatementList();

  @NotNull
  List<DartLabel> getLabelList();

  @NotNull
  List<DartRethrowStatement> getRethrowStatementList();

  @NotNull
  List<DartReturnStatement> getReturnStatementList();

  @NotNull
  List<DartSwitchStatement> getSwitchStatementList();

  @NotNull
  List<DartTryStatement> getTryStatementList();

  @NotNull
  List<DartVarDeclarationList> getVarDeclarationListList();

  @NotNull
  List<DartWhileStatement> getWhileStatementList();

  @NotNull
  List<DartYieldEachStatement> getYieldEachStatementList();

  @NotNull
  List<DartYieldStatement> getYieldStatementList();

}
