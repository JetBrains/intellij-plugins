// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartConstObjectExpression extends DartExpression {

  @NotNull
  DartArguments getArguments();

  @NotNull
  List<DartReferenceExpression> getReferenceExpressionList();

  @Nullable
  DartTypeArguments getTypeArguments();

}
