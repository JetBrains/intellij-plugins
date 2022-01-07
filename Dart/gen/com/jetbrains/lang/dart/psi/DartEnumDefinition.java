// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartEnumDefinition extends DartClass {

  @NotNull
  DartComponentName getComponentName();

  @NotNull
  List<DartEnumConstantDeclaration> getEnumConstantDeclarationList();

  @NotNull
  List<DartFactoryConstructorDeclaration> getFactoryConstructorDeclarationList();

  @NotNull
  List<DartGetterDeclaration> getGetterDeclarationList();

  @NotNull
  List<DartIncompleteDeclaration> getIncompleteDeclarationList();

  @Nullable
  DartInterfaces getInterfaces();

  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  List<DartMethodDeclaration> getMethodDeclarationList();

  @Nullable
  DartMixins getMixins();

  @NotNull
  List<DartNamedConstructorDeclaration> getNamedConstructorDeclarationList();

  @NotNull
  List<DartSetterDeclaration> getSetterDeclarationList();

  @Nullable
  DartTypeParameters getTypeParameters();

  @NotNull
  List<DartVarDeclarationList> getVarDeclarationListList();

}
