// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartNewExpression extends DartExpression, DartReference {

  @NotNull
  List<DartReferenceExpression> getReferenceExpressionList();

  @Nullable
  DartType getType();

  @Nullable
  DartTypeArguments getTypeArguments();

  boolean isConstantObjectExpression();

  @Nullable
  DartArguments getArguments();

}
