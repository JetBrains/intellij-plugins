// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartArrayAccessExpression extends DartExpression, DartReference {

  @NotNull
  List<DartExpression> getExpressionList();

  @Nullable
  DartSwitchExpressionWrapper getSwitchExpressionWrapper();

  @Nullable
  DartTypeArguments getTypeArguments();

}
