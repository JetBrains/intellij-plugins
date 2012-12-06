// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartInterfaceDefinition extends DartClass {

  @NotNull
  DartComponentName getComponentName();

  @Nullable
  DartDefaultFactroy getDefaultFactroy();

  @Nullable
  DartFactorySpecification getFactorySpecification();

  @Nullable
  DartInterfaceBody getInterfaceBody();

  @Nullable
  DartSuperinterfaces getSuperinterfaces();

  @Nullable
  DartTypeParameters getTypeParameters();

}
