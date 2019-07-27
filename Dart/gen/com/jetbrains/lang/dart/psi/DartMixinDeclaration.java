// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartMixinDeclaration extends DartClass {

  @NotNull
  DartClassBody getClassBody();

  @NotNull
  DartComponentName getComponentName();

  @Nullable
  DartInterfaces getInterfaces();

  @NotNull
  List<DartMetadata> getMetadataList();

  @Nullable
  DartOnMixins getOnMixins();

  @Nullable
  DartTypeParameters getTypeParameters();

}
