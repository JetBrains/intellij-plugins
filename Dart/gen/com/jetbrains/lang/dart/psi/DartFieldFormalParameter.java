// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartFieldFormalParameter extends DartPsiCompositeElement {

  @Nullable
  DartFormalParameterList getFormalParameterList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  DartReferenceExpression getReferenceExpression();

  @Nullable
  DartType getType();

  @Nullable
  DartTypeParameters getTypeParameters();

}
