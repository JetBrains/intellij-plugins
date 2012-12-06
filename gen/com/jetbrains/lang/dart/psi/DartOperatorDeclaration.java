// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartOperatorDeclaration extends DartOperator {

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @Nullable
  DartFunctionBody getFunctionBody();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartStringLiteralExpression getStringLiteralExpression();

  @NotNull
  DartUserDefinableOperator getUserDefinableOperator();

}
