// This is a generated file. Not intended for manual editing.
package com.jetbrains.lang.dart.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface DartClassDefinition extends DartClass {

  @Nullable
  DartClassBody getClassBody();

  @NotNull
  DartComponentName getComponentName();

  @Nullable
  DartInterfaces getInterfaces();

  @Nullable
  DartMixins getMixins();

  @Nullable
  DartStringLiteralExpression getStringLiteralExpression();

  @Nullable
  DartSuperclass getSuperclass();

  @Nullable
  DartTypeParameters getTypeParameters();

}
