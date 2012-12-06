// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFactoryConstructorDeclaration extends DartComponent {

  @Nullable
  DartComponentName getComponentName();

  @NotNull
  List<DartExpression> getExpressionList();

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @Nullable
  DartFunctionBody getFunctionBody();

  @Nullable
  DartVarInit getVarInit();

}
