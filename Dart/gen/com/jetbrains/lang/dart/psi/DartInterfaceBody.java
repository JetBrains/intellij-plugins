// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartInterfaceBody extends DartExecutionScope {

  @NotNull
  List<DartGetterDeclaration> getGetterDeclarationList();

  @NotNull
  List<DartMetadata> getMetadataList();

  @NotNull
  List<DartMethodPrototypeDeclaration> getMethodPrototypeDeclarationList();

  @NotNull
  List<DartNamedConstructorDeclaration> getNamedConstructorDeclarationList();

  @NotNull
  List<DartOperatorPrototype> getOperatorPrototypeList();

  @NotNull
  List<DartSetterDeclaration> getSetterDeclarationList();

  @NotNull
  List<DartVarDeclarationList> getVarDeclarationListList();

}
