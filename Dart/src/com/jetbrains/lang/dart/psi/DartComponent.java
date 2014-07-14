package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

public interface DartComponent extends DartPsiCompositeElement, PsiNameIdentifierOwner {
  @Nullable
  DartComponentName getComponentName();

  boolean isStatic();

  boolean isPublic();

  boolean isConstructor();

  boolean isGetter();

  boolean isSetter();

  boolean isAbstract();

  boolean isDeprecated();
}
