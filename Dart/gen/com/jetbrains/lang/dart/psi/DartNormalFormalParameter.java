// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartNormalFormalParameter extends DartPsiCompositeElement {

  @Nullable
  DartFieldFormalParameter getFieldFormalParameter();

  @Nullable
  DartFunctionSignature getFunctionSignature();

  @Nullable
  DartSimpleFormalParameter getSimpleFormalParameter();

  @Nullable
  DartComponentName findComponentName();

}
