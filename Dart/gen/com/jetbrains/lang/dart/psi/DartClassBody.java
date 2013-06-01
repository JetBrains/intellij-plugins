// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartClassBody extends DartExecutionScope {

  @NotNull
  List<DartAbstractOperatorDeclaration> getAbstractOperatorDeclarationList();

  @NotNull
  List<DartFactoryConstructorDeclaration> getFactoryConstructorDeclarationList();

  @NotNull
  List<DartGetterDeclaration> getGetterDeclarationList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  List<DartMethodDeclaration> getMethodDeclarationList();

  @NotNull
  List<DartNamedConstructorDeclaration> getNamedConstructorDeclarationList();

  @NotNull
  List<DartOperatorDeclaration> getOperatorDeclarationList();

  @NotNull
  List<DartSetterDeclaration> getSetterDeclarationList();

  @NotNull
  List<DartVarDeclarationList> getVarDeclarationListList();

}
