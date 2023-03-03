// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartSwitchExpressionCase extends DartPsiCompositeElement {

  @Nullable
  DartConstantPattern getConstantPattern();

  @NotNull
  List<DartExpression> getExpressionList();

  @Nullable
  DartIdentifierPattern getIdentifierPattern();

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

  @Nullable
  DartRecordPattern getRecordPattern();

  @Nullable
  DartRelationalPattern getRelationalPattern();

  @Nullable
  DartUnaryPattern getUnaryPattern();

  @Nullable
  DartVariablePattern getVariablePattern();

}
