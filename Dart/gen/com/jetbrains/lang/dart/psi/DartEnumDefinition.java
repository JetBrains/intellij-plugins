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
  List<DartMetadata> getMetadataList();

}
