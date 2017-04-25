// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartTypedFunctionType extends DartPsiCompositeElement {

  @NotNull
  DartParameterTypeList getParameterTypeList();

  @Nullable
  DartSimpleType getSimpleType();

  @Nullable
  DartTypeParameters getTypeParameters();

  @Nullable
  DartTypedFunctionType getTypedFunctionType();

  @Nullable
  DartUntypedFunctionType getUntypedFunctionType();

  @Nullable
  DartVoidTypeFunctionType getVoidTypeFunctionType();

}
