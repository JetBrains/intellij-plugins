package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.Nullable;

/**
 * @author: Fedor.Korotkov
 */
public interface DartComponent extends DartPsiCompositeElement, PsiNameIdentifierOwner {
  @Nullable
  DartComponentName getComponentName();

  boolean isStatic();

  boolean isPublic();

  boolean isGetter();

  boolean isSetter();

  boolean isAbstract();
}
