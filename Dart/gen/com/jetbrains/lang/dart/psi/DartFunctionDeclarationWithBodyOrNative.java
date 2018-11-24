// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFunctionDeclarationWithBodyOrNative extends DartComponent {

  @NotNull
  DartComponentName getComponentName();

  @NotNull
  DartFormalParameterList getFormalParameterList();

  @Nullable
  DartFunctionBody getFunctionBody();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartReturnType getReturnType();

  @Nullable
  DartStringLiteralExpression getStringLiteralExpression();

  @Nullable
  DartTypeParameters getTypeParameters();

}
