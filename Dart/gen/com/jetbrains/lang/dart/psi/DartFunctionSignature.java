// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFunctionSignature extends DartComponent {

  @NotNull
  DartComponentName getComponentName();

  @NotNull
  DartFormalParameterList getFormalParameterList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartTypeParameters getTypeParameters();

}
