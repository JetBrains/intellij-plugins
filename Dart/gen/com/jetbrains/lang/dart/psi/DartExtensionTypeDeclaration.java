// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartExtensionTypeDeclaration extends DartPsiCompositeElement {

  @Nullable
  DartClassBody getClassBody();

  @NotNull
  List<DartComponentName> getComponentNameList();

  @Nullable
  DartInterfaces getInterfaces();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartType getType();

  @Nullable
  DartTypeParameters getTypeParameters();

}
