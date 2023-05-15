// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartForStatement extends DartPsiCompositeElement {

  @Nullable
  DartAssertStatement getAssertStatement();

  @Nullable
  DartBlock getBlock();

  @Nullable
  DartBreakStatement getBreakStatement();

  @Nullable
  DartContinueStatement getContinueStatement();

  @Nullable
  DartDoWhileStatement getDoWhileStatement();

  @Nullable
  DartExpression getExpression();

  @Nullable
  DartForLoopPartsInBraces getForLoopPartsInBraces();

  @Nullable
  DartForStatement getForStatement();

  @Nullable
  DartFunctionDeclarationWithBody getFunctionDeclarationWithBody();

  @Nullable
  DartIfStatement getIfStatement();

  @NotNull
  List<DartLabel> getLabelList();

  @Nullable
  DartPatternAssignment getPatternAssignment();

  @Nullable
  DartPatternVariableDeclaration getPatternVariableDeclaration();

  @Nullable
  DartRethrowStatement getRethrowStatement();

  @Nullable
  DartReturnStatement getReturnStatement();

  @Nullable
  DartTryStatement getTryStatement();

  @Nullable
  DartVarDeclarationList getVarDeclarationList();

  @Nullable
  DartWhileStatement getWhileStatement();

  @Nullable
  DartYieldEachStatement getYieldEachStatement();

  @Nullable
  DartYieldStatement getYieldStatement();

}
