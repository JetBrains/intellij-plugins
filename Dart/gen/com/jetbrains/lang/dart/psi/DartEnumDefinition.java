// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartEnumDefinition extends DartClass {

  @Nullable
  DartClassMembers getClassMembers();

  @NotNull
  DartComponentName getComponentName();

  @NotNull
  List<DartEnumConstantDeclaration> getEnumConstantDeclarationList();

  @Nullable
  DartInterfaces getInterfaces();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartMixins getMixins();

  @Nullable
  DartTypeParameters getTypeParameters();

}
