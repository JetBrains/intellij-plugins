// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartType extends DartPsiCompositeElement {

  @Nullable
  DartSimpleType getSimpleType();

  @Nullable
  DartTypedFunctionType getTypedFunctionType();

  @Nullable
  DartUntypedFunctionType getUntypedFunctionType();

  @Nullable
  PsiElement resolveReference();

  @Nullable
  DartReferenceExpression getReferenceExpression();

  @Nullable
  DartTypeArguments getTypeArguments();

}
