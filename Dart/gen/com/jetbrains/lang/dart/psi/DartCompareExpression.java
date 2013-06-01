// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartCompareExpression extends DartExpression, DartReference {

  @Nullable
  DartEqualityOperator getEqualityOperator();

  @NotNull
  List<DartExpression> getExpressionList();

  @Nullable
  DartRelationalOperator getRelationalOperator();

}
