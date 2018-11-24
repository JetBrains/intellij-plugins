// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFunctionDeclarationWithBody extends DartComponent {

  @NotNull
  DartComponentName getComponentName();

  @NotNull
  DartFormalParameterList getFormalParameterList();

  @NotNull
  DartFunctionBody getFunctionBody();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartTypeParameters getTypeParameters();

}
