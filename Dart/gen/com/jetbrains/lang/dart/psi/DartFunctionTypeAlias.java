// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFunctionTypeAlias extends DartComponent {

  @NotNull
  DartComponentName getComponentName();

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartType getType();

  @Nullable
  DartTypeParameters getTypeParameters();

}
