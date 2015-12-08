package com.jetbrains.lang.dart.psi;

import com.intellij.psi.PsiNameIdentifierOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface DartComponent extends DartPsiCompositeElement, PsiNameIdentifierOwner {
  @Nullable
  DartComponentName getComponentName();

  boolean isStatic();

  boolean isPublic();

  boolean isConstructor();

  boolean isGetter();

  boolean isSetter();

  boolean isAbstract();

  boolean isUnitMember();

  boolean isOperator();

  DartMetadata getMetadataByName(@NotNull final String name);

  @NotNull
  List<DartMetadata> getMetadataList();
}
